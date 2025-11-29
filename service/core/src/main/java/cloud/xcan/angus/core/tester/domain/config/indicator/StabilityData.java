package cloud.xcan.angus.core.tester.domain.config.indicator;

import static cloud.xcan.angus.api.commonlink.CommonConstant.DEFAULT_ART;
import static cloud.xcan.angus.api.commonlink.CommonConstant.DEFAULT_ERROR_RATE;
import static cloud.xcan.angus.api.commonlink.CommonConstant.DEFAULT_PERCENTILE;
import static cloud.xcan.angus.api.commonlink.CommonConstant.DEFAULT_RES_USED_RATE;
import static cloud.xcan.angus.api.commonlink.CommonConstant.DEFAULT_STABILITY_DURATION;
import static cloud.xcan.angus.api.commonlink.CommonConstant.DEFAULT_STABILITY_THREADS;
import static cloud.xcan.angus.api.commonlink.CommonConstant.DEFAULT_STABILITY_TPS;

import cloud.xcan.angus.api.enums.Percentile;
import cloud.xcan.angus.spec.annotations.Beta;
import cloud.xcan.angus.spec.experimental.ValueObjectSupport;
import cloud.xcan.angus.spec.unit.TimeValue;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Getter
@Setter
@Accessors(chain = true)
public class StabilityData extends ValueObjectSupport<StabilityData> {

  private Integer threads;

  private TimeValue duration;

  private Integer tps;

  private Long art;

  private Percentile percentile;

  private Double errorRate;

  private Double cpu;

  private Double memory;

  private Double disk;

  @Beta // Not fully supported
  private Double network;

  public StabilityData() {
  }

  public StabilityData(Integer threads, TimeValue duration, Integer tps, Long art,
      Percentile percentile, Double errorRate, Double cpu, Double memory, Double disk,
      Double network) {
    this.threads = threads;
    this.duration = duration;
    this.tps = tps;
    this.art = art;
    this.percentile = percentile;
    this.errorRate = errorRate;
    this.cpu = cpu;
    this.memory = memory;
    this.disk = disk;
    this.network = network;
  }

  public static StabilityData default0() {
    return new StabilityData(DEFAULT_STABILITY_THREADS, DEFAULT_STABILITY_DURATION,
        DEFAULT_STABILITY_TPS, DEFAULT_ART, DEFAULT_PERCENTILE, DEFAULT_ERROR_RATE.doubleValue(),
        DEFAULT_RES_USED_RATE, DEFAULT_RES_USED_RATE, DEFAULT_RES_USED_RATE, DEFAULT_RES_USED_RATE);
  }
}
