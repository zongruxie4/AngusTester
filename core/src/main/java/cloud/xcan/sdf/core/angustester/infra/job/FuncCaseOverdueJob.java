package cloud.xcan.sdf.core.angustester.infra.job;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.FUNC_CASE;
import static cloud.xcan.sdf.api.commonlink.EventUtils.assembleAngusTesterUserNoticeEvent;
import static cloud.xcan.sdf.core.angustester.domain.TesterEventMessage.FunctionCaseOverdue;
import static cloud.xcan.sdf.core.angustester.domain.TesterEventMessage.FunctionCaseOverdueCode;
import static cloud.xcan.sdf.core.angustester.infra.job.TaskWillOverdueEventJob.createInnerPrincipal;
import static cloud.xcan.sdf.spec.locale.MessageHolder.message;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;
import static org.springframework.cache.AppEnvUtils.APP_INIT_READY;

import cloud.xcan.sdf.api.commonlink.tenant.Tenant;
import cloud.xcan.sdf.api.commonlink.tenant.TenantRepo;
import cloud.xcan.sdf.api.commonlink.user.UserBase;
import cloud.xcan.sdf.api.enums.NoticeType;
import cloud.xcan.sdf.api.manager.UserManager;
import cloud.xcan.sdf.core.angustester.application.query.common.CommonQuery;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfoRepo;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.sdf.core.event.EventSender;
import cloud.xcan.sdf.core.event.source.EventContent;
import cloud.xcan.sdf.core.pojo.principal.PrincipalContext;
import cloud.xcan.sdf.core.spring.boot.ApplicationInfo;
import cloud.xcan.sdf.lettucex.distlock.RedisLock;
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
public class FuncCaseOverdueJob {

  private static final String LOCK_KEY = "job:angustester:FuncCaseOverdueJob";

  private static final Long COUNT = 2000L;

  @Resource
  private RedisLock redisLock;

  @Resource
  private FuncCaseRepo funcCaseRepo;

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

  @Scheduled(fixedDelay = 23 * 1000, initialDelay = 1200)
  public void execute() {
    if (!APP_INIT_READY) {
      return;
    }
    String reqId = UUID.randomUUID().toString();
    try {
      boolean tryLock = redisLock.tryLock(LOCK_KEY, reqId, 6, TimeUnit.MINUTES);
      if (!tryLock) {
        log.warn("FuncCaseOverdueJob try lock fail!");
        return;
      }

      List<FuncCaseInfo> funcCases = funcCaseInfoRepo.findIdByOverdue(COUNT);
      if (isNotEmpty(funcCases)) {
        // Update to overdue status
        funcCaseRepo.updateOverdueFlag(
            funcCases.stream().map(FuncCaseInfo::getId).collect(Collectors.toList()));

        // Add overdue events
        assembleAndSendOverdueNoticeEvent(funcCases.stream()
            .filter(x -> nonNull(x.getTesterId())).collect(Collectors.toList()));
      }
      log.debug("FuncCaseOverdueJob execute successfully");
    } catch (Exception e) {
      log.error("FuncCaseOverdueJob execute fail", e);
    } finally {
      redisLock.releaseLock(LOCK_KEY, reqId);
    }
  }

  private void assembleAndSendOverdueNoticeEvent(List<FuncCaseInfo> funcCases) {
    if (isEmpty(funcCases)) {
      return;
    }

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
            .get(FunctionCaseOverdueCode);
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

              for (FuncCaseInfo caseInfo : userEntry.getValue()) {
                // Add Overdue event
                assembleAndSendOverdueNoticeEvent(caseInfo, noticeTypes);
              }
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

  private void assembleAndSendOverdueNoticeEvent(FuncCaseInfo caseDb,
      List<NoticeType> noticeTypes) {
    List<Long> receiveObjectIds = new ArrayList<>();
    receiveObjectIds.add(caseDb.getTesterId());
    String message = message(FunctionCaseOverdue, new Object[]{caseDb.getName()},
        PrincipalContext.getDefaultLanguage().toLocale());
    EventContent event = assembleAngusTesterUserNoticeEvent(FunctionCaseOverdueCode, message,
        FUNC_CASE.getValue(), caseDb.getId().toString(), caseDb.getName(), noticeTypes,
        receiveObjectIds);
    EventSender.CommonQueue.send(event);
  }
}
