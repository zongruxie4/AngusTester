package cloud.xcan.sdf.model.script;

import cloud.xcan.sdf.spec.experimental.EndpointRegister;
import cloud.xcan.sdf.spec.locale.EnumMessage;

/**
 * @author xiaolong.liu
 */
@EndpointRegister
public enum ScriptSource implements EnumMessage<String> {
  USER_DEFINED, IMPORTED, SERVICE_SMOKE, SERVICE_SECURITY, API, SCENARIO, GENERATE_DATA;

  public String getValue() {
    return this.name();
  }

  public boolean isUnique() {
    return this.equals(API) || this.equals(SCENARIO);
  }

  public boolean needTestResult() {
    return this.equals(API) || this.equals(SCENARIO);
  }

  public boolean isApis() {
    return this.equals(API);
  }

  public boolean isScenario() {
    return this.equals(SCENARIO);
  }
}
