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
public class PerfListVo {

  private Long id;

  private Long targetId;

  private CombinedTargetType targetType;

  private String targetName;

  @Schema(description = "Target parent or services id")
  private Long targetParentId;

  @Schema(description = "Target parent or services name")
  private String targetParentName;

  @Schema(description = "Enabled or disabled performance testing, default Enabled")
  private Boolean enabled;

  private Integer threads;

  private TimeValue duration;

  private Integer rampUpThreads;

  private TimeValue rampUpInterval;

  private Long art;

  private Percentile percentile;

  private Integer tps;

  private Double errorRate;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;


}



