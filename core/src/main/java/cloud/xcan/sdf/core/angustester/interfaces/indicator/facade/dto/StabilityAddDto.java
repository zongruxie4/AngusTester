package cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.dto;

import static cloud.xcan.angus.model.AngusConstant.MAX_EXEC_DURATION_IN_MS;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.enums.Percentile;
import cloud.xcan.sdf.spec.unit.TimeValue;
import cloud.xcan.sdf.web.validator.annotations.EnumPart;
import cloud.xcan.sdf.web.validator.annotations.TimeValueRange;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
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
public class StabilityAddDto {

  @NotNull
  @ApiModelProperty(value = "Services or api id", example = "1", required = true)
  private Long targetId;

  @NotNull
  @EnumPart(enumClass = CombinedTargetType.class, allowableValues = {"API", "SCENARIO"})
  @ApiModelProperty(value = "Target Type, allowable values: API,SCENARIO", allowableValues = "API,SCENARIO", required = true)
  private CombinedTargetType targetType;

  @NotNull
  @Min(1)
  @ApiModelProperty(value = "The number of concurrent threads(VU)", example = "10", required = true)
  private Integer threads;

  @NotNull
  @TimeValueRange(minInMs = 1000, maxInMs = MAX_EXEC_DURATION_IN_MS) // 1 days
  @ApiModelProperty(value = "Duration of task execution, when iterations and duration are not configured, they will automatically execute for 5 minutes.", required = true)
  private TimeValue duration;

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
  @ApiModelProperty(value = "Error rate (ERROR)/%", example = "0.01", required = true)
  private Double errorRate;

  @Min(0)
  @Max(100)
  @ApiModelProperty(value = "CPU usage rate", example = "0.75")
  private Double cpu;

  @Min(0)
  @Max(100)
  @ApiModelProperty(value = "memory usage rate", example = "0.75")
  private Double memory;

  @Min(0)
  @Max(100)
  @ApiModelProperty(value = "disk usage rate", example = "0.75")
  private Double disk;

  @Min(0)
  @DecimalMax(value = "10000.00")
  @ApiModelProperty(value = "network usage rate", example = "0.75")
  private Double network;

}




