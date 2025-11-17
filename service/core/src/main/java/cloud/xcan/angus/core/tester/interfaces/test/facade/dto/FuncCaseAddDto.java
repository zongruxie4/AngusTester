package cloud.xcan.angus.core.tester.interfaces.test.facade.dto;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_TAGS_NUM;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginConstant.MAX_CASE_OR_TASK_REFS_NUM;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginConstant.MAX_CASE_STEPS_NUM;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_ATTACHMENT_NUM;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_KEY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X4;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_WORKLOAD_NUM;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.core.tester.domain.TestLayer;
import cloud.xcan.angus.core.tester.domain.TestPurpose;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseStepView;
import cloud.xcan.angus.core.tester.domain.test.cases.CaseTestStep;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class FuncCaseAddDto {

  @NotBlank
  @Length(max = MAX_NAME_LENGTH_X4)
  @Schema(description = "Functional test case name for identification and organization", requiredMode = RequiredMode.REQUIRED)
  private String name;

  //@NotBlank
  //@Length(max = MAX_CODE_LENGTH)
  //@Schema(description = "Case code", requiredMode = RequiredMode.REQUIRED)
  //private String code;

  @NotNull
  @Schema(description = "Functional test plan identifier for case association", requiredMode = RequiredMode.REQUIRED)
  private Long planId;

  @Schema(description = "Functional test case module identifier for categorization")
  private Long moduleId;

  @Length(max = MAX_KEY_LENGTH)
  @Schema(description = "Software version for test case execution context")
  private String softwareVersion;

  @Schema(description = "Test case priority level for execution scheduling")
  private Priority priority;

  @Future
  @NotNull
  @Schema(description = "Test case completion deadline for timeline management", requiredMode = RequiredMode.REQUIRED)
  public LocalDateTime deadlineDate;

  @Min(0)
  @Max(MAX_WORKLOAD_NUM)
  @Schema(description = "Estimated workload for test case effort planning")
  private BigDecimal evalWorkload;

  //@Schema(description = "Actual usage Workload")
  //private Double actualWorkload;

  @Schema(description = "Test layer for test case classification, defaults to UI", defaultValue = "UI")
  private TestLayer testLayer;

  @Schema(description = "Test type for test case purpose classification, defaults to FUNCTIONAL", defaultValue = "FUNCTIONAL")
  private TestPurpose testPurpose;

  @EditorContentLength
  @Schema(description = "Test case preconditions for execution preparation")
  private String precondition;

  @Schema(description = "Test case steps display format with TABLE as default")
  private CaseStepView stepView;

  //@NotEmpty
  @Size(max = MAX_CASE_STEPS_NUM)
  @Schema(description = "Test case execution steps with detailed procedures")
  private List<CaseTestStep> steps;

  @EditorContentLength
  @Schema(description = "Comprehensive test case description and documentation")
  private String description;

  @NotNull
  @Schema(description = "Developer identifier for case assignment and responsibility", requiredMode = RequiredMode.REQUIRED)
  private Long developerId;

  @NotNull
  @Schema(description = "Tester identifier for case execution and validation", requiredMode = RequiredMode.REQUIRED)
  private Long testerId;

  //private CaseTestResult testResult;

  @Size(max = MAX_TAGS_NUM)
  @Schema(description = "Tag identifiers for test case categorization and filtering")
  private LinkedHashSet<Long> tagIds;

  @Schema(description = "Test case supporting documents and attachments")
  @Size(max = MAX_ATTACHMENT_NUM)
  private List<Attachment> attachments;

  @Size(max = MAX_CASE_OR_TASK_REFS_NUM)
  @Schema(description = "Referenced task identifiers for dependency management")
  private LinkedHashSet<Long> refTaskIds;

  @Size(max = MAX_CASE_OR_TASK_REFS_NUM)
  @Schema(description = "Referenced case identifiers for test case relationships")
  private LinkedHashSet<Long> refCaseIds;

}
