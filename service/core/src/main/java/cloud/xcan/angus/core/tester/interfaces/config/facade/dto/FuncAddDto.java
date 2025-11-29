package cloud.xcan.angus.core.tester.interfaces.config.facade.dto;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.core.tester.domain.config.indicator.SecurityCheckSetting;
import cloud.xcan.angus.core.tester.domain.config.indicator.SmokeCheckSetting;
import cloud.xcan.angus.model.element.assertion.Assertion;
import cloud.xcan.angus.model.element.extraction.HttpExtraction;
import cloud.xcan.angus.validator.EnumPart;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class FuncAddDto {

  @NotNull
  @Schema(description = "Target identifier for functional test indicator configuration", requiredMode = RequiredMode.REQUIRED)
  private Long targetId;

  @NotNull
  @EnumPart(enumClass = CombinedTargetType.class, allowableValues = {"API"})
  @Schema(description = "Target type for functional test indicator configuration (API)", allowableValues = "API", requiredMode = RequiredMode.REQUIRED)
  private CombinedTargetType targetType;

  @Schema(description = "Smoke testing enablement flag with default enabled state")
  private boolean smoke;

  @Schema(description = "Smoke testing configuration parameters and settings")
  private SmokeCheckSetting smokeCheckSetting;

  @Schema(description = "Custom assertion configuration for smoke testing validation")
  private Assertion<HttpExtraction> userDefinedSmokeAssertion;

  @Schema(description = "Security testing enablement flag")
  private boolean security;

  @Schema(description = "Security testing configuration parameters and settings with default enabled state")
  private SecurityCheckSetting securityCheckSetting;

  @Schema(description = "Custom assertion configuration for security testing validation")
  private Assertion<HttpExtraction> userDefinedSecurityAssertion;

}




