package cloud.xcan.angus.core.tester.interfaces.config.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.remote.PageQuery;
import cloud.xcan.angus.validator.EnumPart;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@Accessors(chain = true)
public class StabilityFindDto extends PageQuery {

  @Schema(description = "Stability test indicator identifier for precise query")
  private Long id;

  @NotNull
  @EnumPart(enumClass = CombinedTargetType.class, allowableValues = {"API", "SCENARIO"})
  @Schema(description = "Target type for stability test indicator query (API, SCENARIO)", allowableValues = "API,SCENARIO", requiredMode = RequiredMode.REQUIRED)
  private CombinedTargetType targetType;

  @Schema(description = "Administrator query flag for accessing all stability test indicators")
  private Boolean admin;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Target name for fuzzy search and filtering")
  private String targetName;

  @Schema(description = "Stability test indicator creator identifier")
  private Long createdBy;

  @Schema(description = "Stability test indicator creation timestamp")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}



