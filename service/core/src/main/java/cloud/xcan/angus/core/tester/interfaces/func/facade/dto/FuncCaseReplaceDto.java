package cloud.xcan.angus.core.tester.interfaces.func.facade.dto;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_TAGS_NUM;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginConstant.MAX_CASE_OR_TASK_REFS_NUM;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginConstant.MAX_CASE_STEPS_NUM;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_ATTACHMENT_NUM;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_KEY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X4;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_WORKLOAD_NUM;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.core.tester.domain.func.cases.CaseStepView;
import cloud.xcan.angus.core.tester.domain.func.cases.CaseTestStep;
import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class FuncCaseReplaceDto {

  @Schema(description = "Functional test case identifier for replacement operation")
  private Long id;

  @Length(max = MAX_NAME_LENGTH_X4)
  @Schema(description = "Functional test case name for identification and organization")
  private String name;

  //@NotBlank
  //@Length(max = MAX_CODE_LENGTH)
  //@Schema(description = "Case code", requiredMode = RequiredMode.REQUIRED)
  //private String code;

  @Schema(description = "Functional test plan identifier for case association")
  private Long planId;

  @Schema(description = "Functional test case module identifier for categorization")
  private Long moduleId;

  @Length(max = MAX_KEY_LENGTH)
  @Schema(description = "Software version for test case execution context")
  private String softwareVersion;

  @Schema(description = "Test case priority level for execution scheduling")
  private Priority priority;

  @NotNull
  @Schema(description = "Test case completion deadline for timeline management", requiredMode = RequiredMode.REQUIRED)
  public LocalDateTime deadlineDate;

  @Min(0)
  @Max(MAX_WORKLOAD_NUM)
  @Schema(description = "Estimated workload for test case effort planning")
  private BigDecimal evalWorkload;

  @Min(0)
  @Max(MAX_WORKLOAD_NUM)
  @Schema(description = "Actual workload for test case effort tracking")
  private BigDecimal actualWorkload;

  @EditorContentLength
  @Schema(description = "Test case preconditions for execution preparation")
  private String precondition;

  @Schema(description = "Test case steps display format with TABLE as default")
  private CaseStepView stepView;

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

  @Size(max = MAX_ATTACHMENT_NUM)
  @Schema(description = "Test case supporting documents and attachments")
  private List<Attachment> attachments;

  @Size(max = MAX_CASE_OR_TASK_REFS_NUM)
  @Schema(description = "Referenced task identifiers for dependency management")
  private LinkedHashSet<Long> refTaskIds;

  @Size(max = MAX_CASE_OR_TASK_REFS_NUM)
  @Schema(description = "Referenced case identifiers for test case relationships")
  private LinkedHashSet<Long> refCaseIds;

}
