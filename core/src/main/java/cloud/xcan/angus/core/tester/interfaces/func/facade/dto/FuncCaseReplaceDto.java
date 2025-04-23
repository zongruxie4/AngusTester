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
import jakarta.validation.Valid;
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

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class FuncCaseReplaceDto {

  @Schema(description = "Functional testing case id")
  private Long id;

  @Length(max = MAX_NAME_LENGTH_X4)
  @Schema(description = "Case name")
  private String name;

  //@NotBlank
  //@Length(max = MAX_CODE_LENGTH)
  //@Schema(description = "Case code", requiredMode = RequiredMode.REQUIRED)
  //private String code;

  @Schema(description = "Functional testing plan id, required when adding")
  private Long planId;

  @Schema(description = "Function case module id")
  private Long moduleId;

  @Length(max = MAX_KEY_LENGTH)
  @Schema(description = "Version of software for the task")
  private String softwareVersion;

  @Schema(description = "Case priority")
  private Priority priority;

  @NotNull
  @Schema(description = "Case deadline date", requiredMode = RequiredMode.REQUIRED)
  public LocalDateTime deadlineDate;

  @Min(0)
  @Max(MAX_WORKLOAD_NUM)
  @Schema(description = "Case Workload")
  private BigDecimal evalWorkload;

  @Min(0)
  @Max(MAX_WORKLOAD_NUM)
  @Schema(description = "Actual usage workload")
  private BigDecimal actualWorkload;

  @EditorContentLength
  @Schema(description = "Case preconditions")
  private String precondition;

  @Schema(description = "Case steps view type, default is 'TABLE'")
  private CaseStepView stepView;

  @Size(max = MAX_CASE_STEPS_NUM)
  @Schema(description = "Case steps")
  private List<CaseTestStep> steps;

  @EditorContentLength
  @Schema(description = "Case description")
  private String description;

  @NotNull
  @Schema(description = "Developer id", requiredMode = RequiredMode.REQUIRED)
  private Long developerId;

  @NotNull
  @Schema(description = "Tester id", requiredMode = RequiredMode.REQUIRED)
  private Long testerId;

  //private CaseTestResult testResult;

  @Size(max = MAX_TAGS_NUM)
  @Schema(description = "Report ids")
  private LinkedHashSet<Long> tagIds;

  @Size(max = MAX_ATTACHMENT_NUM)
  @Schema(description = "Case attachments")
  private List<Attachment> attachments;

  @Size(max = MAX_CASE_OR_TASK_REFS_NUM)
  @Schema(description = "Reference task ids")
  private LinkedHashSet<Long> refTaskIds;

  @Size(max = MAX_CASE_OR_TASK_REFS_NUM)
  @Schema(description = "Reference case ids")
  private LinkedHashSet<Long> refCaseIds;

}
