package cloud.xcan.sdf.api.angustester.indicator.vo;

import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.gm.indicator.SecurityCheckSetting;
import cloud.xcan.sdf.api.gm.indicator.SmokeCheckSetting;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class FuncVo {

  private Long id;

  private Long targetId;

  private CombinedTargetType targetType;

  private String targetName;

  @ApiModelProperty(value = "Enabled or disabled smoke testing, default Enabled")
  private boolean smoke;

  @ApiModelProperty(value = "Enabled or disabled smoke testing")
  private SmokeCheckSetting smokeCheckSetting;

  @ApiModelProperty(value = "User-defined check smoke testing assertion")
  private Assertion<HttpExtraction> userDefinedSmokeAssertion;

  @ApiModelProperty(value = "Enabled or disabled security testing")
  private boolean security;

  @ApiModelProperty(value = "Enabled or disabled security testing, default Enabled")
  private SecurityCheckSetting securityCheckSetting;

  @ApiModelProperty(value = "User-defined check security testing assertion")
  private Assertion<HttpExtraction> userDefinedSecurityAssertion;

}
