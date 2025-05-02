package cloud.xcan.angus.core.tester.interfaces.exec.facade.vo;


import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleSummaryInfoVo;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.configuration.StartMode;
import cloud.xcan.angus.remote.NameJoinField;
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
public class ExecVo {

  private Long id;

  private Long projectId;

  private String no;

  private String name;

  private String plugin;

  @Schema(description = "Execute actual script type")
  private ScriptType scriptType;

  private Long scriptId;

  private String scriptName;

  private ScriptSource scriptSource;

  private Long scriptSourceId;

  private String scriptSourceName;

  private ExecStatus status;


  /////////////////////////////Redundant script field///////////////////////////
  //-------Fields that need to be overwritten for script configuration--------//
  private Long iterations;

  private TimeValue duration;

  private int thread;

  private int priority;

  private Boolean ignoreAssertions;

  private StartMode startMode;

  private LocalDateTime startAtDate;

  private TimeValue reportInterval;

  //-------Fields that need to be overwritten for script configuration--------//

  private Boolean updateTestResult;

  private Boolean canUpdateTestResult;

  private boolean syncTestResult;

  private String syncTestResultFailure;

  private Set<Long> assocApiCaseIds;

  //private LinkedHashSet<Long> nodeIds;

  ////////////////////////Redundant script field/////////////////////////

  ////////////////////////AngusScript/////////////////////////
  //private Plugin plugin;

  //private Configuration configuration;

  //private Task task;
  ////////////////////////AngusScript/////////////////////////

  private Boolean trial;

  private LocalDateTime actualStartDate;

  private LocalDateTime endDate;

  private String meterStatus;

  private String meterMessage;

  private Boolean singleTargetPipeline;

  private Long execBy;

  @NameJoinField(id = "execBy", repository = "commonUserBaseRepo")
  private String execByName;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

  @Schema(description = "Summarized sampling statistics results")
  private ExecSampleSummaryInfoVo sampleSummaryInfo;

  private boolean hasOperationPermission;

  private int schedulingNum;

  private LocalDateTime lastSchedulingDate;

  private List<RunnerRunVo> lastSchedulingResult;

  public boolean isIterationsProgress() {
    boolean isIteration = nonNull(iterations) && iterations > 0;
    return isIteration || scriptType.isFunctionalTesting();
  }

  public Long getCurrentIterations() {
    return isIterationsProgress() && nonNull(sampleSummaryInfo)
        ? sampleSummaryInfo.getIterations() : 0;
  }

  public String getCurrentIterationsProgress() {
    if (isIterationsProgress() && nonNull(sampleSummaryInfo)) {
      double progress = sampleSummaryInfo.getIterations() * 100D
          / nullSafe(iterations, sampleSummaryInfo.getIterations());
      return progress > 100d ? "100" : new DecimalFormat("0.00").format(progress);
    }
    return "0";
  }

  public Long getIterations() {
    return nonNull(iterations) ? iterations : (scriptType.isFunctionalTesting()
        && nonNull(sampleSummaryInfo)? sampleSummaryInfo.getIterations() : null);
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
