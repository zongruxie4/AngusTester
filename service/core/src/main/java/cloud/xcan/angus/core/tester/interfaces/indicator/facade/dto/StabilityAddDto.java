package cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto;

import static cloud.xcan.angus.model.AngusConstant.MAX_EXEC_DURATION_IN_MS;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.enums.Percentile;
import cloud.xcan.angus.spec.unit.TimeValue;
import cloud.xcan.angus.validator.EnumPart;
import cloud.xcan.angus.validator.TimeValueRange;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class StabilityAddDto {

  @NotNull
  @Schema(description = "Services or api id", requiredMode = RequiredMode.REQUIRED)
  private Long targetId;

  @NotNull
  @EnumPart(enumClass = CombinedTargetType.class, allowableValues = {"API", "SCENARIO"})
  @Schema(description = "Target Type, allowable values: API,SCENARIO", allowableValues = "API,SCENARIO", requiredMode = RequiredMode.REQUIRED)
  private CombinedTargetType targetType;

  @NotNull
  @Min(1)
  @Schema(description = "The number of concurrent threads(VU)", example = "10", requiredMode = RequiredMode.REQUIRED)
  private Integer threads;

  @NotNull
  @TimeValueRange(minInMs = 1000, maxInMs = MAX_EXEC_DURATION_IN_MS) // 1 days
  @Schema(description = "Duration of task execution, when iterations and duration are not configured, they will automatically execute for 5 minutes.", requiredMode = RequiredMode.REQUIRED)
  private TimeValue duration;

  @Min(1)
  @Schema(description = "Average response time in milliseconds(ART、ms)")
  private Long art;

  @Schema(description = "Average response time percentile")
  private Percentile percentile;

  @NotNull
  @Min(1)
  @Schema(description = "Operations per second(QPS/TPS)", requiredMode = RequiredMode.REQUIRED)
  private Integer tps;

  @NotNull
  @Min(0)
  @Max(100)
  @Schema(description = "Error rate (ERROR)/%", example = "0.01", requiredMode = RequiredMode.REQUIRED)
  private Double errorRate;

  @Min(0)
  @Max(100)
  @Schema(description = "CPU usage rate", example = "0.75")
  private Double cpu;

  @Min(0)
  @Max(100)
  @Schema(description = "memory usage rate", example = "0.75")
  private Double memory;

  @Min(0)
  @Max(100)
  @Schema(description = "disk usage rate", example = "0.75")
  private Double disk;

  @Min(0)
  @DecimalMax(value = "10000.00")
  @Schema(description = "network usage rate", example = "0.75")
  private Double network;

}




