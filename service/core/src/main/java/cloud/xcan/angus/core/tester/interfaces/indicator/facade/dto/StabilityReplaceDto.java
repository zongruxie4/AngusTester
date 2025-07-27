package cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto;

import static cloud.xcan.angus.model.AngusConstant.MAX_EXEC_DURATION_IN_MS;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.enums.Percentile;
import cloud.xcan.angus.spec.unit.TimeValue;
import cloud.xcan.angus.validator.EnumPart;
import cloud.xcan.angus.validator.TimeValueRange;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class StabilityReplaceDto {

  @NotNull
  @Schema(description = "Target identifier for stability test indicator replacement", requiredMode = RequiredMode.REQUIRED)
  private Long targetId;

  @NotNull
  @EnumPart(enumClass = CombinedTargetType.class, allowableValues = {"API", "SCENARIO"})
  @Schema(description = "Target type for stability test indicator replacement (API, SCENARIO)", allowableValues = "API,SCENARIO", requiredMode = RequiredMode.REQUIRED)
  private CombinedTargetType targetType;

  @NotNull
  @Min(1)
  @Schema(description = "Concurrent virtual user count for stability testing", example = "10", requiredMode = RequiredMode.REQUIRED)
  private Integer threads;

  @NotNull
  @TimeValueRange(minInMs = 1000, maxInMs = MAX_EXEC_DURATION_IN_MS) // 1 days
  @Schema(description = "Test execution duration with automatic 5-minute fallback when not configured", requiredMode = RequiredMode.REQUIRED)
  private TimeValue duration;

  @Min(1)
  @Schema(description = "Average response time threshold in milliseconds")
  private Long art;

  @Schema(description = "Response time percentile for statistical analysis")
  private Percentile percentile;

  @NotNull
  @Min(1)
  @Schema(description = "Transactions per second target for stability validation", requiredMode = RequiredMode.REQUIRED)
  private Integer tps;

  @NotNull
  @Min(0)
  @Max(100)
  @Schema(description = "Maximum acceptable error rate percentage", example = "0.01", requiredMode = RequiredMode.REQUIRED)
  private Double errorRate;

  @Min(0)
  @Max(100)
  @Schema(description = "CPU utilization threshold percentage", example = "0.75")
  private Double cpu;

  @Min(0)
  @Max(100)
  @Schema(description = "Memory utilization threshold percentage", example = "0.75")
  private Double memory;

  @Min(0)
  @Max(100)
  @Schema(description = "Disk utilization threshold percentage", example = "0.75")
  private Double disk;

  @Min(0)
  @DecimalMax(value = "10000.00")
  @Schema(description = "Network bandwidth utilization threshold in Mbps", example = "0.75")
  private Double network;

}




