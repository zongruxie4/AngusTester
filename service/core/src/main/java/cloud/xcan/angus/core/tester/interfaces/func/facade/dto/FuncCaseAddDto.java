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
  @Schema(description = "Case name", requiredMode = RequiredMode.REQUIRED)
  private String name;

  //@NotBlank
  //@Length(max = MAX_CODE_LENGTH)
  //@Schema(description = "Case code", requiredMode = RequiredMode.REQUIRED)
  //private String code;

  @NotNull
  @Schema(description = "Functional testing plan id", requiredMode = RequiredMode.REQUIRED)
  private Long planId;

  @Schema(description = "Function case module id")
  private Long moduleId;

  @Length(max = MAX_KEY_LENGTH)
  @Schema(description = "Version of software for the task")
  private String softwareVersion;

  @Schema(description = "Case priority")
  private Priority priority;

  @Future
  @NotNull
  @Schema(description = "Case deadline date", requiredMode = RequiredMode.REQUIRED)
  public LocalDateTime deadlineDate;

  @Min(0)
  @Max(MAX_WORKLOAD_NUM)
  @Schema(description = "Case Workload")
  private BigDecimal evalWorkload;

  //@Schema(description = "Actual usage Workload")
  //private Double actualWorkload;

  @EditorContentLength
  @Schema(description = "Case preconditions")
  private String precondition;

  @Schema(description = "Case steps view type, default is 'TABLE'")
  private CaseStepView stepView;

  //@NotEmpty
  @Size(max = MAX_CASE_STEPS_NUM)
  @Schema(description = "Case steps"/*, requiredMode = RequiredMode.REQUIRED*/)
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

  @Schema(description = "Case attachments")
  @Size(max = MAX_ATTACHMENT_NUM)
  private List<Attachment> attachments;

  @Size(max = MAX_CASE_OR_TASK_REFS_NUM)
  @Schema(description = "Reference task ids")
  private LinkedHashSet<Long> refTaskIds;

  @Size(max = MAX_CASE_OR_TASK_REFS_NUM)
  @Schema(description = "Reference case ids")
  private LinkedHashSet<Long> refCaseIds;
}
