package cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto;

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

  @NotNull
  @EnumPart(enumClass = CombinedTargetType.class, allowableValues = {"API", "SCENARIO"})
  @Schema(allowableValues = "API,SCENARIO", requiredMode = RequiredMode.REQUIRED)
  private CombinedTargetType targetType;

  @Schema(description = "Required when app administrators query all stability indicator")
  private Boolean admin;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Target name")
  private String targetName;

  @Schema(description = "Stability indicator creator id")
  private Long createdBy;

  @Schema(description = "Stability indicator creation time ")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}



