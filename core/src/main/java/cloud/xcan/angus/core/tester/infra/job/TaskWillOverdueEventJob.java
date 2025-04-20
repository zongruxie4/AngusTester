package cloud.xcan.angus.core.tester.infra.job;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.TASK;
import static cloud.xcan.angus.api.commonlink.EventUtils.assembleAngusTesterUserNoticeEvent;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.TaskWillOverdue;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.TaskWillOverdueCode;
import static cloud.xcan.angus.spec.experimental.BizConstant.XCAN_TENANT_PLATFORM_CODE;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.tenant.Tenant;
import cloud.xcan.angus.api.commonlink.tenant.TenantRepo;
import cloud.xcan.angus.api.commonlink.user.UserBase;
import cloud.xcan.angus.api.enums.NoticeType;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.event.EventSender;
import cloud.xcan.angus.core.event.source.EventContent;
import cloud.xcan.angus.core.job.JobTemplate;
import cloud.xcan.angus.core.spring.boot.ApplicationInfo;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.domain.task.TaskInfo;
import cloud.xcan.angus.core.tester.domain.task.TaskInfoRepo;
import cloud.xcan.angus.spec.annotations.DoInFuture;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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
  private JobTemplate jobTemplate;

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

  @Scheduled(fixedDelay = EXPIRATION_REMINDER_INTERVAL_MILLIS, initialDelay = 12000)
  public void execute() {
    jobTemplate.execute(LOCK_KEY, 10, TimeUnit.MINUTES, () -> {
      // Query the first overdue cases
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime deadline = now.plusSeconds(EXPIRATION_REMINDER_INTERVAL_MILLIS / 1000);
      List<TaskInfo> tasks = taskInfoRepo.findWillOverdue(now, deadline, COUNT);

      if (isNotEmpty(tasks)) {
        assembleAndSendOverdueNoticeEvent(tasks);
      }
    });
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
        .setUserId(user.getId()).setFullName(user.getFullName());
  }
}
