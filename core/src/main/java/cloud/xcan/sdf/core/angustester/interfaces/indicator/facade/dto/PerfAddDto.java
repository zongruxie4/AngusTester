package cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto;

import static cloud.xcan.angus.model.AngusConstant.MAX_EXEC_DURATION_IN_MS;
import static cloud.xcan.sdf.api.commonlink.TesterConstant.MAX_API_PERF_CONCURRENCY;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.enums.Percentile;
import cloud.xcan.sdf.spec.unit.TimeValue;
import cloud.xcan.sdf.web.validator.annotations.EnumPart;
import cloud.xcan.sdf.web.validator.annotations.TimeValueRange;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class PerfAddDto {

  @NotNull
  @ApiModelProperty(value = "Apis or services id", required = true)
  private Long targetId;

  @NotNull
  @EnumPart(enumClass = CombinedTargetType.class, allowableValues = {"API", "SCENARIO"})
  @ApiModelProperty(value = "Target Type, allowable values: API,SCENARIO", allowableValues = "API,SCENARIO", required = true)
  private CombinedTargetType targetType;

  @NotNull
  @Min(1)
  @Max(MAX_API_PERF_CONCURRENCY)
  @ApiModelProperty(value = "The number of concurrent threads(VU)", required = true)
  private Integer threads;

  @TimeValueRange(minInMs = 1000, maxInMs = MAX_EXEC_DURATION_IN_MS) // 1 days
  @NotNull
  @ApiModelProperty(value = "Duration of task execution, when iterations and duration are not configured, they will automatically execute for 30 seconds.", required = true)
  private TimeValue duration;

  @Min(0)
  @ApiModelProperty(value = "Adjust ramp up thread number, the value does not exceed the threads.")
  private Integer rampUpThreads;

  @ApiModelProperty(value = "Adjust ramp up time interval, the value does not exceed the duration.")
  private TimeValue rampUpInterval;

  @Min(1)
  @ApiModelProperty(value = "Average response time in milliseconds(ART、ms)")
  private Long art;

  @ApiModelProperty(value = "Average response time percentile")
  private Percentile percentile;

  @NotNull
  @Min(1)
  @ApiModelProperty(value = "Operations per second(QPS/TPS)", required = true)
  private Integer tps;

  @NotNull
  @Min(0)
  @Max(100)
  @ApiModelProperty(value = "Error rate (ERROR)/%", required = true)
  private Double errorRate;

}




