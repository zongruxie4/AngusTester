package cloud.xcan.angus.core.tester.interfaces.task.facade.dto;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_TAGS_NUM;
import static cloud.xcan.angus.core.tester.domain.TesterFuncPluginConstant.MAX_CASE_OR_TASK_REFS_NUM;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_ATTACHMENT_NUM;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_KEY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X4;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_WORKLOAD_NUM;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.core.tester.domain.task.BugLevel;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@Accessors(chain = true)
public class TaskReplaceDto {

  // No modification allowed, only movement allowed
  //  @NotNull
  //  @Schema(description = "Sprint Id", requiredMode = RequiredMode.REQUIRED)
  //  private Long sprintId;

  @Schema(description = "Function case module identifier for task categorization")
  private Long moduleId;

  @NotEmpty
  @Length(max = MAX_NAME_LENGTH_X4)
  @Schema(example = "A task", description = "Task display name for identification and organization", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Length(max = MAX_KEY_LENGTH)
  @Schema(description = "Software version identifier associated with the task")
  private String softwareVersion;

  // @EnumPart(enumClass = TaskType.class, allowableValues = {"BUG", "STORY", "TASK"}) -> Do in biz
  @Schema(description = "Task classification type for workflow management. Note: Test task type cannot be modified after creation", example = "TASK")
  private TaskType taskType;

  @Schema(description = "Bug severity level for defect classification", example = "MINOR")
  private BugLevel bugLevel;

  //@NotNull Fix:: Backlog assignee is nullable
  @Schema(description = "Primary assignee identifier for task responsibility assignment"/*, requiredMode = RequiredMode.REQUIRED*/)
  private Long assigneeId;

  @Schema(description = "Reviewer identifier for task validation and approval")
  private Long confirmerId;

  @Schema(description = "Test execution identifier for task testing responsibility")
  private Long testerId;

  @Schema(description = "Flag indicating if this is a missing bug report")
  private Boolean missingBug;

  @Size(max = MAX_TAGS_NUM)
  @Schema(description = "Tag identifiers for task categorization and filtering")
  private LinkedHashSet<Long> tagIds;

  @Schema(example = "MEDIUM", description = "Task priority level for scheduling and resource allocation")
  private Priority priority = Priority.DEFAULT;

  //@Future Fix:: Save expired task exception
  //@NotNull Fix:: Backlog deadlineDate is nullable
  @Schema(example = "2023-01-02 12:00:00", description = "Task completion deadline for timeline management")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime deadlineDate;

  @Valid
  @Size(max = MAX_ATTACHMENT_NUM)
  @Schema(description = "Task-related file attachments for documentation and reference")
  private List<Attachment> attachments;

  @EditorContentLength(max = 6000)
  @Schema(description = "Detailed task description for requirements and context")
  private String description;

  @Min(0)
  @Max(MAX_WORKLOAD_NUM)
  @Schema(description = "Estimated workload for task planning and resource allocation")
  private BigDecimal evalWorkload;

  @Min(0)
  @Max(MAX_WORKLOAD_NUM)
  @Schema(description = "Actual workload for performance tracking and analysis")
  private BigDecimal actualWorkload;

  private Long parentTaskId;

  @Size(max = MAX_CASE_OR_TASK_REFS_NUM)
  @Schema(description = "Referenced task identifiers for dependency management")
  private LinkedHashSet<Long> refTaskIds;

  @Size(max = MAX_CASE_OR_TASK_REFS_NUM)
  @Schema(description = "Referenced test case identifiers for test coverage linkage")
  private LinkedHashSet<Long> refCaseIds;
}
