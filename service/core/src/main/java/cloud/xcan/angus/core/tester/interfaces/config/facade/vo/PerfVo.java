package cloud.xcan.angus.core.tester.interfaces.config.facade.vo;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.api.enums.Percentile;
import cloud.xcan.angus.spec.unit.TimeValue;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class PerfVo {

  private Long id;

  private Long targetId;

  private CombinedTargetType targetType;

  private String targetName;

  private Integer threads;

  private TimeValue duration;

  private Integer rampUpThreads;

  private TimeValue rampUpInterval;

  private Long art;

  private Percentile percentile;

  private Integer tps;

  private Double errorRate;

}
