package cloud.xcan.angus.core.tester.infra.util;

import static cloud.xcan.angus.core.spring.SpringContextHolder.getBean;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getTenantId;

import cloud.xcan.angus.idgen.BidGenerator;

public class BIDUtils {

  /**
   * Generates a unique resource id for the current tenant.
   *
   * @return the generated resource id
   */
  public static Long getId(BIDKey bidKey) {
    return Long.valueOf(getBean(BidGenerator.class).getId(bidKey.name(), getTenantId()));
  }

  public enum BIDKey {
    projectId,
    sprintId,
    planId,
    caseReviewId,
    caseBaselineId,
    taskId,
    caseId,
    variableId,
    datasetId,
    scriptId,
    scenarioId,
    apisId,
    serviceId,
    serverId,
    mockServiceId,
    execId
  }
}
