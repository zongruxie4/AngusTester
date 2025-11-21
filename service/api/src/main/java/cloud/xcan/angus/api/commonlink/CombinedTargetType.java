package cloud.xcan.angus.api.commonlink;

import cloud.xcan.angus.spec.experimental.EndpointRegister;
import cloud.xcan.angus.spec.locale.EnumMessage;

/**
 * @author XiaoLong Liu
 */
@EndpointRegister
public enum CombinedTargetType implements EnumMessage<String> {
  PROJECT, TAG, MODULE, EVALUATION,
  TASK, TASK_SPRINT, SOFTWARE_VERSION, TASK_ANALYSIS, MEETING,
  FUNC_PLAN, FUNC_REVIEW, FUNC_CASE, FUNC_CASE_BASELINE, FUNC_CASE_ANALYSIS,
  SERVICE, API, API_CASE, API_DESIGN, SCENARIO, SCENARIO_MONITOR, SCRIPT, MOCK_SERVICE,
  VARIABLE, DATASET, MOCK_APIS, EXECUTION, REPORT;

  public boolean isApisTargetType(ApisTargetType type) {
    return this.getValue().equals(type.getValue());
  }

  public boolean isService() {
    return this.equals(SERVICE);
  }

  public boolean isParent() {
    return this.equals(SERVICE) || this.equals(MOCK_SERVICE);
  }

  public boolean isApi() {
    return this.equals(API);
  }

  public boolean isApiCase() {
    return this.equals(API_CASE);
  }

  public boolean isScenario() {
    return this.equals(SCENARIO);
  }

  public boolean isTask() {
    return this.equals(TASK);
  }

  public boolean isFuncCase() {
    return this.equals(FUNC_CASE);
  }

  /**
   * `EXECUTION` not included.
   */
  public boolean supportRefVariableResource() {
    return this.equals(API) || this.equals(API_CASE) || this.equals(SCENARIO);
  }

  @Override
  public String getValue() {
    return this.name();
  }
}
