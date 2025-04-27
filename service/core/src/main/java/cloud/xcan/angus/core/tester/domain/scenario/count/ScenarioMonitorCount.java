package cloud.xcan.angus.core.tester.domain.scenario.count;

import cloud.xcan.angus.spec.unit.TimeValue;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ScenarioMonitorCount {

  private int totalNum;
  private int successNum;
  private int failureNum;
  private double successRate;

  private int last24HoursNum;
  private int last24HoursSuccessNum;
  private double last24HoursSuccessRate;

  private int last7DayNum;
  private int last7DaySuccessNum;
  private double last7DaySuccessRate;

  private int last30DayNum;
  private int last30DaySuccessNum;
  private double last30DaySuccessRate;

  private TimeValue avgDelayTime;
  private TimeValue minDelayTime;
  private TimeValue maxDelayTime;
  private TimeValue p50DelayTime;
  private TimeValue p75DelayTime;
  private TimeValue p90DelayTime;
}
