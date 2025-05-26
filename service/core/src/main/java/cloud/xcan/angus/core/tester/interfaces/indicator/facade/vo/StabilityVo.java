package cloud.xcan.angus.core.tester.interfaces.indicator.facade.vo;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.enums.Percentile;
import cloud.xcan.angus.spec.unit.TimeValue;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class StabilityVo {

  private Long id;

  private Long targetId;

  private CombinedTargetType targetType;

  private String targetName;

  private Integer threads;

  private TimeValue duration;

  private Long art;

  private Percentile percentile;

  private Integer tps;

  private Double errorRate;

  private Double cpu;
  private Double memory;
  private Double disk;
  private Double network;

}
