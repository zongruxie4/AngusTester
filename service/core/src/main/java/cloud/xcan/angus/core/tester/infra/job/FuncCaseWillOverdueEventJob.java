package cloud.xcan.angus.core.tester.infra.job;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.FUNC_CASE;
import static cloud.xcan.angus.api.commonlink.EventUtils.assembleAngusTesterUserNoticeEvent;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.FunctionCaseWillOverdue;
import static cloud.xcan.angus.core.tester.domain.TesterEventMessage.FunctionCaseWillOverdueCode;
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
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCaseInfoRepo;
import cloud.xcan.angus.spec.annotations.DoInFuture;
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
public class FuncCaseWillOverdueEventJob {

  private static final String LOCK_KEY = "tester:job:FuncCaseWillOverdueEventJob";

  /**
   * Remind every three hours.
   */
  private final static int EXPIRATION_REMINDER_INTERVAL_MILLIS = 3 * 60 * 60 * 1000;

  @DoInFuture("The prompt is inaccurate when case exceeds 5000")
  private static final Long COUNT = 5000L;

  @Resource
  private JobTemplate jobTemplate;

  @Resource
  private FuncCaseInfoRepo funcCaseInfoRepo;

  @Resource
  private TenantRepo tenantRepo;

  @Resource
  private UserManager userManager;

  @Resource
  private ApplicationInfo applicationInfo;

  @Resource
  private CommonQuery commonQuery;

  @Scheduled(fixedDelay = EXPIRATION_REMINDER_INTERVAL_MILLIS, initialDelay = 11000)
  public void execute() {
    jobTemplate.execute(LOCK_KEY, 6, TimeUnit.MINUTES, () -> {
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime deadline = now.plusSeconds(EXPIRATION_REMINDER_INTERVAL_MILLIS / 1000);
      List<FuncCaseInfo> funcCases = funcCaseInfoRepo.findWillOverdue(now, deadline, COUNT);

      if (isNotEmpty(funcCases)) {
        assembleAndSendOverdueNoticeEvent(funcCases);
      }
    });
  }

  private void assembleAndSendOverdueNoticeEvent(List<FuncCaseInfo> funcCases) {
    // Query the case tenant info
    Map<Long, List<FuncCaseInfo>> tenantCasesMap = funcCases.stream().collect(
        Collectors.groupingBy(FuncCaseInfo::getTenantId));
    Map<Long, Tenant> tenantMap = tenantRepo.findAllByIdIn(tenantCasesMap.keySet())
        .stream().collect(Collectors.toMap(Tenant::getId, x -> x));

    // Assemble tester notice events
    Map<Long, UserBase> userMap = userManager.getUserBaseMap(funcCases.stream()
        .map(FuncCaseInfo::getTesterId).collect(Collectors.toSet()));
    for (Entry<Long, List<FuncCaseInfo>> tenantEntry : tenantCasesMap.entrySet()) {
      Tenant tenant = tenantMap.get(tenantEntry.getKey());
      if (nonNull(tenant)) {
        List<NoticeType> noticeTypes = commonQuery.findTenantEventNoticeTypes(tenant.getId())
            .get(FunctionCaseWillOverdueCode);
        if (isEmpty(noticeTypes)) {
          continue;
        }

        Map<Long, List<FuncCaseInfo>> userCasesMap = tenantCasesMap.get(tenantEntry.getKey())
            .stream().collect(Collectors.groupingBy(FuncCaseInfo::getTesterId));
        for (Entry<Long, List<FuncCaseInfo>> userEntry : userCasesMap.entrySet()) {
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
            log.error("FuncCaseWillOverdueEventJob : User [{}] not found!", userEntry.getKey());
          }
        }
      } else {
        log.error("FuncCaseWillOverdueEventJob : Tenant [{}] not found!", tenantEntry.getKey());
      }
    }
  }

  private void assembleAndSendOverdueNoticeEvent(FuncCaseInfo caseDb, int num,
      List<NoticeType> noticeTypes) {
    List<Long> receiveObjectIds = new ArrayList<>();
    receiveObjectIds.add(caseDb.getTesterId());
    String message = message(FunctionCaseWillOverdue, new Object[]{num},
        PrincipalContext.getDefaultLanguage().toLocale());
    EventContent event = assembleAngusTesterUserNoticeEvent(FunctionCaseWillOverdueCode, message,
        FUNC_CASE.getValue(), caseDb.getId().toString(), caseDb.getName(), noticeTypes,
        receiveObjectIds);
    EventSender.CommonQueue.send(event);
  }
}
