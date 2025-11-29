package cloud.xcan.angus.core.tester.domain.config.indicator;

import cloud.xcan.angus.spec.experimental.ValueObjectSupport;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class FuncData extends ValueObjectSupport<FuncData> {

  @Schema(description = "Enabled or disabled smoke testing.", defaultValue = "true")
  private boolean smoke;

  @Schema(description = "Enabled or disabled smoke testing.")
  private SmokeCheckSetting smokeCheckSetting;

  @Schema(description = "User-defined check smoke testing assertion.")
  //private Assertion<HttpExtraction> userDefinedSmokeAssertion;
  private Object userDefinedSmokeAssertion;

  @Schema(description = "Enabled or disabled security testing.", defaultValue = "true")
  private boolean security;

  @Schema(description = "Enabled or disabled security testing.")
  private SecurityCheckSetting securityCheckSetting;

  @Schema(description = "User-defined check security testing assertion.")
  //private Assertion<HttpExtraction> userDefinedSecurityAssertion;
  private Object userDefinedSecurityAssertion;

  public FuncData() {
  }

  public FuncData(boolean smoke, SmokeCheckSetting smokeCheckSetting,
      Object userDefinedSmokeAssertion, boolean security,
      SecurityCheckSetting securityCheckSetting,
      Object userDefinedSecurityAssertion) {
    this.smoke = smoke;
    this.smokeCheckSetting = smokeCheckSetting;
    this.userDefinedSmokeAssertion = userDefinedSmokeAssertion;
    this.security = security;
    this.securityCheckSetting = securityCheckSetting;
    this.userDefinedSecurityAssertion = userDefinedSecurityAssertion;
  }

  public static FuncData default0() {
    return new FuncData(true, SmokeCheckSetting.API_AVAILABLE, null, true,
        SecurityCheckSetting.NOT_SECURITY_CODE, null);
  }

}
