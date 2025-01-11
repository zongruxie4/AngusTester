package cloud.xcan.sdf.core.angustester.interfaces.indicator.facade.vo;


import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.enums.Percentile;
import cloud.xcan.sdf.spec.unit.TimeValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class StabilityListVo {

  private Long id;

  private Long targetId;

  private CombinedTargetType targetType;

  private String targetName;

  @ApiModelProperty(value = "Target parent or services id")
  private Long targetParentId;

  @ApiModelProperty(value = "Target parent or services name")
  private String targetParentName;

  @ApiModelProperty(value = "Enabled or disabled stability testing, default Enabled")
  private Boolean enabled;

  private Integer threads;

  private TimeValue duration;

  private Long art;

  private Percentile percentile;

  private Integer tps;

  @ApiModelProperty(value = "Error rate")
  private Double errorRate;

  @ApiModelProperty(value = "CPU usage")
  private Double cpu;
  @ApiModelProperty(value = "memory usage")
  private Double memory;
  @ApiModelProperty(value = "disk usage")
  private Double disk;
  @ApiModelProperty(value = "network usage")
  private Double network;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

}



