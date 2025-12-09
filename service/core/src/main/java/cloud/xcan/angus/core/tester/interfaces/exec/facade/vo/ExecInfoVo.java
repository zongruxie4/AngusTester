package cloud.xcan.angus.core.tester.interfaces.exec.facade.vo;


import static java.util.Objects.nonNull;

import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleSummaryInfoVo;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.configuration.StartMode;
import cloud.xcan.angus.model.script.configuration.TestPlatform;
import cloud.xcan.angus.spec.unit.TimeValue;
import io.swagger.v3.oas.annotations.media.Schema;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecInfoVo {

  private Long id;

  private Long projectId;

  private String no;

  private String name;

  private String plugin;

  private TestPlatform platform;

  private ScriptType scriptType;

  private Long scriptId;

  //private String scriptName;

  private Long scriptSourceId;

  private ExecStatus status;

  private Long iterations;

  private TimeValue duration;

  private int thread;

  private int priority;

  private Boolean ignoreAssertions;

  private StartMode startMode;

  private LocalDateTime startAtDate;

  private TimeValue reportInterval;

  private Boolean updateTestResult;

  private Boolean canUpdateTestResult;

  private boolean syncTestResult;

  private String syncTestResultFailure;

  private Set<Long> assocApiCaseIds;

  private Boolean trial;

  private LocalDateTime actualStartDate;

  private LocalDateTime endDate;

  private String meterStatus;

  private String meterMessage;

  @Schema(description = "Summarized sampling statistics results")
  private ExecSampleSummaryInfoVo sampleSummaryInfo;

  private int schedulingNum;

  private LocalDateTime lastSchedulingDate;

  private List<RunnerRunVo> lastSchedulingResult;

  public boolean isIterationsProgress() {
    return nonNull(iterations) && iterations > 0;
  }

  public Long getCurrentIterations() {
    return isIterationsProgress() && nonNull(sampleSummaryInfo)
        ? sampleSummaryInfo.getIterations() : 0;
  }

  public String getCurrentIterationsProgress() {
    if (isIterationsProgress() && nonNull(sampleSummaryInfo)) {
      double progress = sampleSummaryInfo.getIterations() * 100D / iterations;
      return progress > 100d ? "100" : new DecimalFormat("0.00").format(progress);
    }
    return "0";
  }

  public boolean isDurationProgress() {
    return nonNull(duration);
  }

  public String getCurrentDuration() {
    return isDurationProgress() && nonNull(sampleSummaryInfo)
        ? (sampleSummaryInfo.getDuration() / 1000/*Remove the value of the millisecond bit*/) + "s"
        : "0";
  }

  public String getCurrentDurationProgress() {
    if (isDurationProgress() && nonNull(sampleSummaryInfo)) {
      double progress = (double) (sampleSummaryInfo.getDuration()
          / 1000/*Remove the value of the millisecond bit*/) * 100
          / (long/*Remove the value of the millisecond bit*/) duration.toSecond();
      return progress > 100d ? "100" : new DecimalFormat("0.00").format(progress);
    }
    return "0";
  }
}
