package cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.result;

import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.api.commonlink.exec.result.ExecSampleResultContent;
import cloud.xcan.angus.api.commonlink.exec.result.ExecTargetSummary;
import cloud.xcan.angus.api.commonlink.exec.result.NodeUsageSummary;
import cloud.xcan.angus.api.gm.indicator.to.FuncTo;
import cloud.xcan.angus.api.gm.indicator.to.PerfTo;
import cloud.xcan.angus.api.gm.indicator.to.StabilityTo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleSummaryInfoVo;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ExecTestResultDetailVo {

  private Long id;

  private Long execId;

  private String execName;

  private ExecStatus execStatus;

  private String plugin;

  private ScriptType scriptType;

  private Long scriptId;

  private String scriptName;

  private ScriptSource scriptSource;

  private Long scriptSourceId;

  private String scriptSourceName;

  //@Column(name = "service_id") -> Changes will occur after movement
  //private Long serviceId;

  private FuncTo indicatorFunc;

  private PerfTo indicatorPerf;

  private StabilityTo indicatorStability;

  private boolean passed;

  private String failureMessage;

  private int testNum;

  private int testFailureNum;

  private Long usageFailedNodeId;

  private ExecSampleSummaryInfoVo sampleSummary;

  private ExecTargetSummary targetSummary;

  private ExecTargetSummary caseSummary;

  private Map<Long, NodeUsageSummary> nodeUsageSummary;

  private List<ExecSampleResultContent> sampleContent;

  private List<ExecTestCaseResultDetailVo> caseResults;

  private Long execBy;

  private String execByName;

  private LocalDateTime lastExecDate;

  private LocalDateTime createdDate;

}
