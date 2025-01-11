package cloud.xcan.sdf.api.angustester.scenario.vo;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.commonlink.exec.ExecStatus;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ScenarioListVo {

  private Long id;

  private String name;

  private Long projectId;

  //@NameJoinField(id = "projectId", repository = "projectRepo")
  //private String projectName;

  private String plugin;

  private ScriptType scriptType;

  private Long scriptId;

  private String description;

  private Boolean authFlag;

  private Long lastExecId;
  private ExecStatus lastExecStatus;
  private String lastExecFailureMessage;

  /**
   * Whether to enable functional testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis.
   */
  private Boolean testFuncFlag;

  private Boolean testFuncPassedFlag;

  private String testFuncFailureMessage;

  /**
   * Whether to enable performance testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis.
   */
  private Boolean testPerfFlag;

  private Boolean testPerfPassedFlag;

  private String testPerfFailureMessage;

  /**
   * Whether to enable stability testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis
   */
  private Boolean testStabilityFlag;

  private Boolean testStabilityPassedFlag;

  private String testStabilityFailureMessage;

  private Boolean favouriteFlag;

  private Boolean followFlag;

  private Long createdBy;

  private String createdByName;

  private String avatar;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;
}
