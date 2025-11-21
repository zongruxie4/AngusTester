package cloud.xcan.angus.core.tester.interfaces.config.facade.vo;


import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.enums.Percentile;
import cloud.xcan.angus.remote.NameJoinField;
import cloud.xcan.angus.spec.unit.TimeValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;


@Setter
@Getter
@Accessors(chain = true)
public class StabilityListVo {

  private Long id;

  private Long targetId;

  private CombinedTargetType targetType;

  private String targetName;

  @Schema(description = "Target parent or services id")
  private Long targetParentId;

  @Schema(description = "Target parent or services name")
  private String targetParentName;

  @Schema(description = "Enabled or disabled stability testing, default Enabled")
  private Boolean enabled;

  private Integer threads;

  private TimeValue duration;

  private Long art;

  private Percentile percentile;

  private Integer tps;

  @Schema(description = "Error rate")
  private Double errorRate;

  @Schema(description = "CPU usage")
  private Double cpu;
  @Schema(description = "memory usage")
  private Double memory;
  @Schema(description = "disk usage")
  private Double disk;
  @Schema(description = "network usage")
  private Double network;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

}



