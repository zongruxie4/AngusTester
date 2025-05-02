package cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.debug;


import cloud.xcan.angus.agent.message.runner.RunnerRunVo;
import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.api.pojo.node.NodeInfo;
import cloud.xcan.angus.core.tester.domain.exec.result.ExecSampleContentInfo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample.ExecSampleSummaryInfoVo;
import cloud.xcan.angus.model.script.configuration.Configuration;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.pipeline.Task;
import cloud.xcan.angus.remote.NameJoinField;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecDebugDetailVo {

  private Long id;

  private String no;

  private String name;

  private String plugin;

  private ScriptType scriptType;

  private Long scriptId;

  private String scriptName;

  private Long scenarioId;

  private boolean testSucceed;

  private String testFailureMessage;

  private ExecStatus status;

  private String message;

  private NodeInfo execNode;

  private List<NodeInfo> availableNodes;

  ////////////////////////Redundant script field/////////////////////////

  ////////////////////////AngusScript/////////////////////////
  //private Plugin plugin;

  private Configuration configuration;

  private Task task;

  private LinkedHashMap<String, List<String>> pipelineTargetMappings;

  ////////////////////////AngusScript/////////////////////////

  private LocalDateTime endDate;

  private String meterStatus;

  private String meterMessage;

  private Boolean singleTargetPipeline;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  @Schema(description = "Summarized sampling statistics results")
  private ExecSampleSummaryInfoVo sampleSummaryInfo;

  private RunnerRunVo schedulingResult;

  private List<ExecSampleContentInfo> sampleContents;


}



