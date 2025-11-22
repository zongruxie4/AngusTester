package cloud.xcan.angus.core.tester.domain.report;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

@EndpointRegister
public enum ReportTemplate implements EnumMessage<String> {
  PROJECT_PROGRESS, PROJECT_DATA_ASSETS, PROJECT_EFFICIENCY, PROJECT_ACTIVITY, TEST_EVALUATION,
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
    return switch (this) {
      case PROJECT_PROGRESS, PROJECT_DATA_ASSETS, PROJECT_EFFICIENCY, PROJECT_ACTIVITY,
           TEST_EVALUATION -> CombinedTargetType.PROJECT;
      case TASK_SPRINT -> CombinedTargetType.TASK_SPRINT;
      case TASK -> CombinedTargetType.TASK;
      case FUNC_TESTING_PLAN -> CombinedTargetType.FUNC_PLAN;
      case FUNC_TESTING_CASE -> CombinedTargetType.FUNC_CASE;
      case SERVICES_TESTING_RESULT -> CombinedTargetType.SERVICE;
      case APIS_TESTING_RESULT -> CombinedTargetType.API;
      case SCENARIO_TESTING_RESULT -> CombinedTargetType.SCENARIO;
      case EXEC_FUNCTIONAL_RESULT, EXEC_PERFORMANCE_RESULT, EXEC_STABILITY_RESULT,
           EXEC_CUSTOMIZATION_RESULT -> CombinedTargetType.EXECUTION;
    };
  }
}
