package cloud.xcan.angus.core.tester.interfaces.scenario.facade.vo;

import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ScenarioListVo {

  private Long id;

  private String name;

  private Long projectId;

  //@NameJoinField(id = "projectId", repository = "projectRepo")
  //private String projectName;

  private Long moduleId;

  private String plugin;

  private ScriptType scriptType;

  private Long scriptId;

  private String description;

  private Boolean auth;

  private Long lastExecId;
  private String lastExecName;
  private ExecStatus lastExecStatus;
  private String lastExecFailureMessage;
  private LocalDateTime lastExecDate;

  /**
   * Whether to enable functional testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis.
   */
  private Boolean testFunc;

  private Boolean testFuncPassed;

  private String testFuncFailureMessage;

  /**
   * Whether to enable performance testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis.
   */
  private Boolean testPerf;

  private Boolean testPerfPassed;

  private String testPerfFailureMessage;

  /**
   * Whether to enable stability testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis
   */
  private Boolean testStability;

  private Boolean testStabilityPassed;

  private String testStabilityFailureMessage;

  private Boolean favourite;

  private Boolean follow;

  private Long createdBy;

  private String createdByName;

  private String avatar;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;
}
