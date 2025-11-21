package cloud.xcan.angus.core.tester.interfaces.config.facade.dto;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_API_PERF_CONCURRENCY;
import static cloud.xcan.angus.model.AngusConstant.MAX_EXEC_DURATION_IN_MS;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.enums.Percentile;
import cloud.xcan.angus.spec.unit.TimeValue;
import cloud.xcan.angus.validator.EnumPart;
import cloud.xcan.angus.validator.TimeValueRange;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class PerfAddDto {

  @NotNull
  @Schema(description = "Target identifier for performance test indicator configuration", requiredMode = RequiredMode.REQUIRED)
  private Long targetId;

  @NotNull
  @EnumPart(enumClass = CombinedTargetType.class, allowableValues = {"API", "SCENARIO"})
  @Schema(description = "Target type for performance test indicator configuration (API, SCENARIO)", allowableValues = "API,SCENARIO", requiredMode = RequiredMode.REQUIRED)
  private CombinedTargetType targetType;

  @NotNull
  @Min(1)
  @Max(MAX_API_PERF_CONCURRENCY)
  @Schema(description = "Concurrent virtual user count for performance testing", requiredMode = RequiredMode.REQUIRED)
  private Integer threads;

  @TimeValueRange(minInMs = 1000, maxInMs = MAX_EXEC_DURATION_IN_MS) // 1 days
  @NotNull
  @Schema(description = "Test execution duration with automatic 30-second fallback when not configured", requiredMode = RequiredMode.REQUIRED)
  private TimeValue duration;

  @Min(0)
  @Schema(description = "Gradual thread ramp-up count not exceeding total thread count")
  private Integer rampUpThreads;

  @Schema(description = "Gradual thread ramp-up time interval not exceeding total duration")
  private TimeValue rampUpInterval;

  @Min(1)
  @Schema(description = "Average response time threshold in milliseconds")
  private Long art;

  @Schema(description = "Response time percentile for statistical analysis")
  private Percentile percentile;

  @NotNull
  @Min(1)
  @Schema(description = "Transactions per second target for performance validation", requiredMode = RequiredMode.REQUIRED)
  private Integer tps;

  @NotNull
  @Min(0)
  @Max(100)
  @Schema(description = "Maximum acceptable error rate percentage", requiredMode = RequiredMode.REQUIRED)
  private Double errorRate;

}




