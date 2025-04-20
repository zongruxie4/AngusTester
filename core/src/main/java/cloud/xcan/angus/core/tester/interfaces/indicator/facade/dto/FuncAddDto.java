package cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.gm.indicator.SecurityCheckSetting;
import cloud.xcan.angus.api.gm.indicator.SmokeCheckSetting;
import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.validator.EnumPart;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid

@Setter
@Getter
@Accessors(chain = true)
public class FuncAddDto {

  @NotNull
  @Schema(description = "Apis or services id", requiredMode = RequiredMode.REQUIRED)
  private Long targetId;

  @NotNull
  @EnumPart(enumClass = CombinedTargetType.class, allowableValues = {"API"})
  @Schema(description = "Target Type, allowable values: API", allowableValues = "API", requiredMode = RequiredMode.REQUIRED)
  private CombinedTargetType targetType;

  @Schema(description = "Enabled or disabled smoke testing, default Enabled")
  private boolean smoke;

  @Schema(description = "Enabled or disabled smoke testing")
  private SmokeCheckSetting smokeCheckSetting;

  @Schema(description = "User-defined check smoke testing assertion")
  private Assertion<HttpExtraction> userDefinedSmokeAssertion;

  @Schema(description = "Enabled or disabled security testing")
  private boolean security;

  @Schema(description = "Enabled or disabled security testing, default Enabled")
  private SecurityCheckSetting securityCheckSetting;

  @Schema(description = "User-defined check security testing assertion")
  private Assertion<HttpExtraction> userDefinedSecurityAssertion;

}




