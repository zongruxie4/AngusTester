package cloud.xcan.angus.core.tester.infra.job;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.EXECUTION;
import static cloud.xcan.angus.api.commonlink.EventUtils.assembleAngusTesterUserNoticeEvent;
import static cloud.xcan.angus.core.tester.domain.CtrlEventMessage.ExecutionTestCompleted;
import static cloud.xcan.angus.core.tester.domain.CtrlEventMessage.ExecutionTestCompletedCode;
import static cloud.xcan.angus.core.tester.domain.CtrlEventMessage.ExecutionTestFailed;
import static cloud.xcan.angus.core.tester.domain.CtrlEventMessage.ExecutionTestFailedCode;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.spec.experimental.BizConstant.XCAN_TENANT_PLATFORM_CODE;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.utils.ObjectUtils.emptySafe;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

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
import cloud.xcan.angus.core.tester.domain.exec.Exec;
import cloud.xcan.angus.core.tester.domain.exec.ExecRepo;
import cloud.xcan.angus.spec.principal.Principal;
import cloud.xcan.angus.spec.principal.PrincipalContext;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ExecEventJob {

  private static final String LOCK_KEY = "tester:job:ExecEventJob";
  private static final int COUNT = 500;

  @Resource
  private JobTemplate jobTemplate;

  @Resource
  private ExecRepo execRepo;

  @Resource
  private TenantRepo tenantRepo;

  @Resource
  private UserManager userManager;

  @Resource
  private CommonQuery commonQuery;

  @Resource
  private ApplicationInfo applicationInfo;

  @Scheduled(fixedDelay = 21 * 1300, initialDelay = 1210)
  public void execute() {
    jobTemplate.execute(LOCK_KEY, 10, TimeUnit.MINUTES, () -> {
      // Submitted transaction by repo
      List<Exec> execDbs = execRepo.findAssembleAndSendEventTask(COUNT);
      if (isNotEmpty(execDbs)) {
        // Query the exec tenant info
        Map<Long, Tenant> tenantMap = tenantRepo.findAllByIdIn(
            execDbs.stream().map(Exec::getTenantId).collect(
                Collectors.toSet())).stream().collect(Collectors.toMap(Tenant::getId, x -> x));
        // Query the exec user info
        Map<Long, UserBase> userMap = userManager.getUserBaseMap(execDbs.stream()
            .filter(x -> nonNull(x.getModifiedBy()) && x.getModifiedBy() > 0)
            .map(Exec::getModifiedBy).collect(Collectors.toSet()));

        for (Exec execDb : execDbs) {
          try {
            if (execDb.getScriptType().isTest()) {
              Tenant tenant = tenantMap.get(execDb.getTenantId());
              UserBase user = userMap.get(execDb.getModifiedBy());
              if (isNull(tenant) || isNull(user)) {
                log.error("tenant:{} user:{} not exist!", execDb.getTenantId(),
                    execDb.getModifiedBy());
                execRepo.updateAssembleAndSendEvent(execDb.getId(), false);
                continue;
              }

              // Transfer principal downwards
              PrincipalContext.set(createInnerPrincipal(tenant, user, applicationInfo));

              // Add Overdue event
              assembleAndSendNoticeEvent(execDb);
            }
            execRepo.updateAssembleAndSendEvent(execDb.getId(), true);
          } catch (Exception e) {
            log.error("ExecStartJob#inner execute fail: ", e);
            execRepo.updateAssembleAndSendEvent(execDb.getId(), false);
          } finally {
            PrincipalContext.remove();
          }
        }
      }
    });
  }

  public static Principal createInnerPrincipal(Tenant tenant, UserBase user,
      ApplicationInfo applicationInfo) {
    return new Principal().setOptTenantId(tenant.getId())
        .setTenantId(tenant.getId()).setTenantName(tenant.getName())
        .setClientId(XCAN_TENANT_PLATFORM_CODE)
        .setServiceCode(applicationInfo.getArtifactId())
        .setInstanceId(applicationInfo.getInstanceId())
        /*.setPlatformScope(Platform.XCAN_TP)*/
        .setUserId(user.getId()).setFullName(user.getFullName());
  }

  public static Principal createInnerPrincipal(ApplicationInfo applicationInfo) {
    return new Principal().setOptTenantId(getOptTenantId())
        .setTenantId(getOptTenantId()).setTenantName(null)
        .setClientId(XCAN_TENANT_PLATFORM_CODE)
        .setServiceCode(applicationInfo.getArtifactId())
        .setInstanceId(applicationInfo.getInstanceId())
        /*.setPlatformScope(Platform.XCAN_TP)*/
        .setUserId(-1L).setFullName("");
  }

  private void assembleAndSendNoticeEvent(Exec execDb) {
    List<Long> receiveObjectIds = new ArrayList<>();
    receiveObjectIds.add(execDb.getModifiedBy());
    EventContent event;
    if (execDb.getStatus().isCompleted()) {
      List<NoticeType> noticeTypes = commonQuery.findTenantEventNoticeTypes(execDb.getTenantId())
          .get(ExecutionTestCompletedCode);
      if (isEmpty(noticeTypes)) {
        return;
      }
      String message = message(ExecutionTestCompleted, new Object[]{execDb.getName()});
      event = assembleAngusTesterUserNoticeEvent(ExecutionTestCompletedCode, message,
          EXECUTION.getValue(), execDb.getId().toString(), execDb.getName(), noticeTypes,
          receiveObjectIds);
      EventSender.CommonQueue.send(event);
    } else {
      List<NoticeType> noticeTypes = commonQuery.findTenantEventNoticeTypes(execDb.getTenantId())
          .get(ExecutionTestFailedCode);
      if (isEmpty(noticeTypes)) {
        return;
      }
      String message = message(ExecutionTestFailed, new Object[]{execDb.getName(),
          emptySafe(execDb.getMeterMessage(), execDb.getStatus().getMessage())});
      event = assembleAngusTesterUserNoticeEvent(ExecutionTestFailedCode, message,
          EXECUTION.getValue(), execDb.getId().toString(), execDb.getName(), noticeTypes,
          receiveObjectIds);
      EventSender.CommonQueue.send(event);
    }
  }
}
