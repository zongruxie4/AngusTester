package cloud.xcan.angus.core.tester.domain.scenario.summary;


import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.configuration.TestPlatform;
import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ScenarioDetailSummary {

  private Long id;

  private String name;

  private Long projectId;

  @NameJoinField(id = "projectId", repository = "projectRepo")
  private String projectName;

  private String plugin;

  private String description;

  private TestPlatform platform = TestPlatform.API;

  private ScriptType scriptType;

  private Long scriptId;

  private String scriptName;

  private AngusScript script;

  private Boolean auth;

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

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}



