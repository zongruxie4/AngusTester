package cloud.xcan.sdf.core.angustester.infra.job;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.TASK;
import static cloud.xcan.sdf.api.commonlink.EventUtils.assembleAngusTesterUserNoticeEvent;
import static cloud.xcan.sdf.core.angustester.domain.TesterEventMessage.TaskWillOverdue;
import static cloud.xcan.sdf.core.angustester.domain.TesterEventMessage.TaskWillOverdueCode;
import static cloud.xcan.sdf.core.utils.AppEnvUtils.APP_INIT_READY;
import static cloud.xcan.sdf.spec.experimental.BizConstant.XCAN_TENANT_PLATFORM_CODE;
import static cloud.xcan.sdf.spec.locale.MessageHolder.message;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.api.commonlink.tenant.Tenant;
import cloud.xcan.sdf.api.commonlink.tenant.TenantRepo;
import cloud.xcan.sdf.api.commonlink.user.UserBase;
import cloud.xcan.sdf.api.enums.NoticeType;
import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfoRepo;
import cloud.xcan.sdf.core.event.EventSender;
import cloud.xcan.sdf.core.event.source.EventContent;
import cloud.xcan.sdf.core.pojo.principal.Principal;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import cloud.xcan.sdf.core.spring.boot.ApplicationInfo;
import cloud.xcan.sdf.lettucex.distlock.RedisLock;
import cloud.xcan.sdf.spec.annotations.DoInFuture;
import cloud.xcan.sdf.spec.locale.SupportedLanguage;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskWillOverdueEventJob {

  private static final String LOCK_KEY = "job:angustester:TaskWillOverdueEventJob";

  /**
   * Remind every three hours.
   */
  private final static int EXPIRATION_REMINDER_INTERVAL_MILLIS = 3 * 60 * 60 * 1000;

  @DoInFuture("The prompt is inaccurate when case exceeds 5000")
  private static final Long COUNT = 5000L;

  @Resource
  private TaskInfoRepo taskInfoRepo;

  @Resource
  private TenantRepo tenantRepo;

  @Resource
  private UserManager userManager;

  @Resource
  private ApplicationInfo applicationInfo;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private RedisLock redisLock;

  @Scheduled(fixedDelay = EXPIRATION_REMINDER_INTERVAL_MILLIS, initialDelay = 12000)
  public void execute() {
    if (!APP_INIT_READY) {
      return;
    }
    String reqId = UUID.randomUUID().toString();
    try {
      boolean tryLock = redisLock.tryLock(LOCK_KEY, reqId, 10, TimeUnit.MINUTES);
      if (!tryLock) {
        log.warn("TaskWillOverdueEventJob try lock fail!");
        return;
      }

      // Query the first overdue cases
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime deadline = now.plusSeconds(EXPIRATION_REMINDER_INTERVAL_MILLIS / 1000);
      List<TaskInfo> tasks = taskInfoRepo.findWillOverdue(now, deadline, COUNT);

      if (ObjectUtils.isNotEmpty(tasks)) {
        assembleAndSendOverdueNoticeEvent(tasks);
      }
      log.debug("TaskWillOverdueEventJob execute successfully");
    } catch (Exception e) {
      log.error("TaskWillOverdueEventJob execute fail", e);
    } finally {
      redisLock.releaseLock(LOCK_KEY, reqId);
    }
  }

  private void assembleAndSendOverdueNoticeEvent(List<TaskInfo> tasks) {
    // Query the case tenant info
    Map<Long, List<TaskInfo>> tenantTasksMap = tasks.stream().collect(
        Collectors.groupingBy(TaskInfo::getTenantId));
    Map<Long, Tenant> tenantMap = tenantRepo.findAllByIdIn(tenantTasksMap.keySet())
        .stream().collect(Collectors.toMap(Tenant::getId, x -> x));

    // Assemble tester notice events
    Map<Long, UserBase> userMap = userManager.getUserBaseMap(tasks.stream()
        .map(TaskInfo::getAssigneeId).collect(Collectors.toSet()));
    for (Entry<Long, List<TaskInfo>> tenantEntry : tenantTasksMap.entrySet()) {
      Tenant tenant = tenantMap.get(tenantEntry.getKey());
      if (nonNull(tenant)) {
        List<NoticeType> noticeTypes = commonQuery.findTenantEventNoticeTypes(tenant.getId())
            .get(TaskWillOverdueCode);
        if (isEmpty(noticeTypes)) {
          continue;
        }

        Map<Long, List<TaskInfo>> userTasksMap = tenantTasksMap.get(tenantEntry.getKey())
            .stream().collect(Collectors.groupingBy(TaskInfo::getAssigneeId));
        for (Entry<Long, List<TaskInfo>> userEntry : userTasksMap.entrySet()) {
          UserBase user = userMap.get(userEntry.getKey());
          if (nonNull(user)) {
            try {
              // Transfer principal downwards
              PrincipalContext.set(createInnerPrincipal(tenant, user, applicationInfo));

              // Add Overdue event
              assembleAndSendOverdueNoticeEvent(userEntry.getValue().get(0),
                  userEntry.getValue().size(), noticeTypes);
            } finally {
              PrincipalContext.remove();
            }
          } else {
            log.error("TaskWillOverdueEventJob : User [{}] not found!", userEntry.getKey());
          }
        }
      } else {
        log.error("TaskWillOverdueEventJob : Tenant [{}] not found!", tenantEntry.getKey());
      }
    }
  }

  private void assembleAndSendOverdueNoticeEvent(TaskInfo caseDb, int num,
      List<NoticeType> noticeTypes) {
    List<Long> receiveObjectIds = new ArrayList<>();
    receiveObjectIds.add(caseDb.getAssigneeId());
    String message = message(TaskWillOverdue, new Object[]{num},
        PrincipalContext.getDefaultLanguage().toLocale());
    EventContent event = assembleAngusTesterUserNoticeEvent(TaskWillOverdueCode, message,
        TASK.getValue(), caseDb.getId().toString(), caseDb.getName(), noticeTypes,
        receiveObjectIds);
    EventSender.CommonQueue.send(event);
  }

  public static Principal createInnerPrincipal(Tenant tenant, UserBase user,
      ApplicationInfo applicationInfo) {
    return new Principal().setOptTenantId(tenant.getId())
        .setTenantId(tenant.getId()).setTenantName(tenant.getName())
        .setClientId(XCAN_TENANT_PLATFORM_CODE)
        .setServiceCode(applicationInfo.getArtifactId())
        .setInstanceId(applicationInfo.getInstanceId())
        .setDefaultLanguage(SupportedLanguage.zh_CN)
        /*.setPlatformScope(Platform.XCAN_TP)*/
        .setUserId(user.getId()).setUserFullname(user.getFullname());
  }
}
