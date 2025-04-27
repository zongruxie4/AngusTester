package cloud.xcan.angus.core.tester.interfaces.indicator.facade.dto;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_API_PERF_CONCURRENCY;
import static cloud.xcan.angus.model.AngusConstant.MAX_EXEC_DURATION_IN_MS;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.enums.Percentile;
import cloud.xcan.angus.spec.unit.TimeValue;
import cloud.xcan.angus.validator.EnumPart;
import cloud.xcan.angus.validator.TimeValueRange;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
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
public class PerfReplaceDto {

  @NotNull
  @Schema(description = "Apis or services id", requiredMode = RequiredMode.REQUIRED)
  private Long targetId;

  @NotNull
  @EnumPart(enumClass = CombinedTargetType.class, allowableValues = {"API", "SCENARIO"})
  @Schema(description = "Target Type, allowable values: API,SCENARIO", allowableValues = "API,SCENARIO", requiredMode = RequiredMode.REQUIRED)
  private CombinedTargetType targetType;

  @NotNull
  @Min(1)
  @Max(MAX_API_PERF_CONCURRENCY)
  @Schema(description = "The number of concurrent threads(VU)", requiredMode = RequiredMode.REQUIRED)
  private Integer threads;

  @TimeValueRange(minInMs = 1000, maxInMs = MAX_EXEC_DURATION_IN_MS) // 1 days
  @NotNull
  @Schema(description = "Duration of task execution, when iterations and duration are not configured, they will automatically execute for 30 seconds.", requiredMode = RequiredMode.REQUIRED)
  private TimeValue duration;

  @Min(0)
  @Schema(description = "Adjust ramp up thread number, the value does not exceed the threads.")
  private Integer rampUpThreads;

  @Schema(description = "Adjust ramp up time interval, the value does not exceed the duration.")
  private TimeValue rampUpInterval;

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
  @Schema(description = "Error rate (ERROR)/%", requiredMode = RequiredMode.REQUIRED)
  private Double errorRate;

}




