package cloud.xcan.angus.core.tester.infra.job;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.TASK;
import static cloud.xcan.angus.api.commonlink.EventUtils.assembleAngusTesterUserNoticeEvent;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.TaskOverdue;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.TaskOverdueCode;
import static cloud.xcan.angus.core.tester.infra.job.TaskWillOverdueEventJob.createInnerPrincipal;
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
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
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
public class TaskOverdueJob {

  private static final String LOCK_KEY = "job:angustester:TaskOverdueJob";

  private static final Long COUNT = 2000L;

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

  @Scheduled(fixedDelay = 24 * 1000, initialDelay = 1800)
  public void execute() {
    jobTemplate.execute(LOCK_KEY, 6, TimeUnit.MINUTES, () -> {
      List<TaskInfo> tasks = taskInfoRepo.findIdByOverdue(COUNT);
      if (isNotEmpty(tasks)) {
        // Update to overdue status
        taskInfoRepo.updateOverdue(tasks.stream().map(TaskInfo::getId)
            .collect(Collectors.toList()));

        // Add overdue events
        assembleAndSendOverdueNoticeEvent(
            tasks.stream().filter(x -> nonNull(x.getAssigneeId())).collect(Collectors.toList()));
      }
    });
  }

  private void assembleAndSendOverdueNoticeEvent(List<TaskInfo> tasks) {
    if (isEmpty(tasks)) {
      return;
    }

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
            .get(TaskOverdueCode);
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

              for (TaskInfo taskInfo : userEntry.getValue()) {
                // Add Overdue event
                assembleAndSendOverdueNoticeEvent(taskInfo, noticeTypes);
              }
            } finally {
              PrincipalContext.remove();
            }
          } else {
            log.error("TaskOverdueJob : User [{}] not found!", userEntry.getKey());
          }
        }
      } else {
        log.error("TaskOverdueJob : Tenant [{}] not found!", tenantEntry.getKey());
      }
    }
  }

  private void assembleAndSendOverdueNoticeEvent(TaskInfo taskDb, List<NoticeType> noticeTypes) {
    List<Long> receiveObjectIds = new ArrayList<>();
    receiveObjectIds.add(taskDb.getAssigneeId());
    String message = message(TaskOverdue, new Object[]{taskDb.getName()},
        PrincipalContext.getDefaultLanguage().toLocale());
    EventContent event = assembleAngusTesterUserNoticeEvent(TaskOverdueCode, message,
        TASK.getValue(), taskDb.getId().toString(), taskDb.getName(), noticeTypes,
        receiveObjectIds);
    EventSender.CommonQueue.send(event);
  }
}
