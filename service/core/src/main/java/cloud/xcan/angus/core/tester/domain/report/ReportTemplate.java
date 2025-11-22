package cloud.xcan.angus.core.tester.domain.report;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum ReportTemplate implements EnumMessage<String> {
  PROJECT_PROGRESS, PROJECT_DATA_ASSETS, PROJECT_EFFICIENCY, PROJECT_ACTIVITY,
  TASK_SPRINT, TASK,
  FUNC_TESTING_PLAN, FUNC_TESTING_CASE,
  SERVICES_TESTING_RESULT, APIS_TESTING_RESULT,
  SCENARIO_TESTING_RESULT,
  EXEC_FUNCTIONAL_RESULT, EXEC_PERFORMANCE_RESULT, EXEC_STABILITY_RESULT, EXEC_CUSTOMIZATION_RESULT;

  @Override
  public String getValue() {
    return this.name();
  }

  public boolean isWidePlan() {
    return this == TASK_SPRINT || this == FUNC_TESTING_PLAN;
  }

  public boolean isExecutionReport() {
    return this == EXEC_FUNCTIONAL_RESULT || this == EXEC_PERFORMANCE_RESULT
        || this == EXEC_CUSTOMIZATION_RESULT;
  }

  public CombinedTargetType getReportTargetType() {
    switch (this) {
      case PROJECT_PROGRESS:
      case PROJECT_DATA_ASSETS:
      case PROJECT_EFFICIENCY:
      case PROJECT_ACTIVITY:
        return CombinedTargetType.PROJECT;
      case TASK_SPRINT:
        return CombinedTargetType.TASK_SPRINT;
      case TASK:
        return CombinedTargetType.TASK;
      case FUNC_TESTING_PLAN:
        return CombinedTargetType.FUNC_PLAN;
      case FUNC_TESTING_CASE:
        return CombinedTargetType.FUNC_CASE;
      case SERVICES_TESTING_RESULT:
        return CombinedTargetType.SERVICE;
      case APIS_TESTING_RESULT:
        return CombinedTargetType.API;
      case SCENARIO_TESTING_RESULT:
        return CombinedTargetType.SCENARIO;
      case EXEC_FUNCTIONAL_RESULT:
      case EXEC_PERFORMANCE_RESULT:
      case EXEC_STABILITY_RESULT:
      case EXEC_CUSTOMIZATION_RESULT:
        return CombinedTargetType.EXECUTION;
    }
    throw new IllegalStateException("Not target value for " + this.name());
  }
}
