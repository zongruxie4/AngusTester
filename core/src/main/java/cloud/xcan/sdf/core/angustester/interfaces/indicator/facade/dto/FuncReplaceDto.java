package cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto;

import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.gm.indicator.SecurityCheckSetting;
import cloud.xcan.sdf.api.gm.indicator.SmokeCheckSetting;
import cloud.xcan.sdf.web.validator.annotations.EnumPart;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class FuncReplaceDto {

  @NotNull
  @ApiModelProperty(value = "Apis or services id", required = true)
  private Long targetId;

  @NotNull
  @EnumPart(enumClass = CombinedTargetType.class, allowableValues = {"API"})
  @ApiModelProperty(value = "Target Type, allowable values: API", allowableValues = "API", required = true)
  private CombinedTargetType targetType;

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




