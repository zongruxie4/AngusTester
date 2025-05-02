package cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample;

import static java.util.Objects.nonNull;

import cloud.xcan.angus.spec.unit.TimeValue;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecSampleSummaryVo {

  private boolean finish;

  @JsonIgnore
  @Schema(description = "The server records the sampling time")
  private Long timestamp0;

  private long iterations0;

  private TimeValue duration0;

  ////////////////////////////Sampling Fields///////////////////////////
  @Schema(description = "The runner records the sampling time")
  private Long timestamp00;

  @Schema(description = "Sampling task name")
  private String name;

  /**
   * =========Time=========
   */
  private long duration;

  private long startTime;

  private long endTime;

  /**
   * =======Counter=======
   */
  private long errors;

  private Long iterations;

  private long n;

  private long operations;

  private long transactions;

  private long readBytes;

  private long writeBytes;

  /**
   * ======Throughput======
   * <p>
   * Very target or target transaction group will be sampled
   */
  private double ops;

  private double tps;

  private double brps;

  private double bwps;

  /**
   * ==Aggregation(Trans)=
   */
  private double tranMean;

  private double tranMin;

  private double tranMax;

  /**
   * ==Percentile(Trans)==
   */
  private double tranP50;

  private double tranP75;

  private double tranP90;

  private double tranP95;

  private double tranP99;

  private double tranP999;

  /**
   * ========Error========
   */
  private double errorRate;
  // private Map<String, Long> errorCauseCounter;

  /**
   * ========Threads=======
   */
  private int threadPoolSize;

  private int threadPoolActiveSize;

  private int threadMaxPoolSize;

  private boolean threadRunning;

  private boolean threadTerminated;

  /**
   * ========UploadResultProgress=======
   */
  private long uploadResultBytes;

  private long uploadResultTotalBytes;

  private String uploadResultProgress;

  /**
   * ========Extension====
   */
  //private Map<String, Long> extCounterMap1;
  //private Map<String, Long> extCounterMap2;

  private long extCounter1;

  private long extCounter2;

  private double extGauge1;

  private double extGauge2;

  public double getOps() {
    return BigDecimal.valueOf(this.ops).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  public double getTps() {
    return BigDecimal.valueOf(this.tps).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  public double getTranMean() {
    return BigDecimal.valueOf(this.tranMean).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  public double getTranP50() {
    return BigDecimal.valueOf(this.tranP50).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  public double getTranP75() {
    return BigDecimal.valueOf(this.tranP75).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  public double getTranP90() {
    return BigDecimal.valueOf(this.tranP90).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  public double getTranP95() {
    return BigDecimal.valueOf(this.tranP95).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  public double getTranP99() {
    return BigDecimal.valueOf(this.tranP99).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  public double getTranP999() {
    return BigDecimal.valueOf(this.tranP999).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  public double getErrorRate() {
    return BigDecimal.valueOf(this.errorRate).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }

  public boolean isIterationsProgress() {
    return iterations0 > 0;
  }

  public long getCurrentIterations() {
    return isIterationsProgress() ? iterations : 0;
  }

  public String getCurrentIterationsProgress() {
    if (isIterationsProgress()) {
      double progress = (double) iterations * 100 / iterations0;
      return progress > 100d ? "100" : new DecimalFormat("0.00").format(progress);
    }
    return "0";
  }

  public boolean isDurationProgress() {
    return nonNull(duration0);
  }

  public String getCurrentDuration() {
    return isDurationProgress() ?
        (duration / 1000) /*Remove the value of the millisecond bit*/ + "s" : "0";
  }

  public String getCurrentDurationProgress() {
    if (isDurationProgress()) {
      double progress = (double) (duration / 1000/*Remove the value of the millisecond bit*/) * 100
          / (double) (duration0.toMilliSecond() / 1000 /*Remove the value of the millisecond bit*/);
      return progress > 100d ? "100" : new DecimalFormat("0.00").format(progress);
    }
    return "0";
  }

  public Date getTimestamp() {
    return nonNull(timestamp0) ? new Date(timestamp0) : null;
  }
}
