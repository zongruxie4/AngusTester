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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class TaskUpdateDto {

  // No modification allowed, only movement allowed
  //  @NotNull
  //  @Schema(description = "Sprint Id", requiredMode = RequiredMode.REQUIRED)
  //  private Long sprintId;

  @Schema(description = "Function case module id")
  private Long moduleId;

  @Length(max = MAX_NAME_LENGTH_X4)
  @Schema(example = "A Task", description = "Task name")
  private String name;

  @Length(max = MAX_KEY_LENGTH)
  @Schema(description = "Version of software for the task")
  private String softwareVersion;

  // @EnumPart(enumClass = TaskType.class, allowableValues = {"BUG", "STORY", "TASK"}) -> Do in biz
  @Schema(description = "Task type. Note: The test task type cannot be modified", example = "TASK")
  private TaskType taskType;

  @Schema(description = "Bug level, default MINOR",example = "MINOR")
  private BugLevel bugLevel;

  @Schema(description = "Assignee id")
  private Long assigneeId;

  @Schema(description = "Confirmor id")
  private Long confirmorId;

  @Schema(description = "Task tester")
  private Long testerId;

  @Schema(description = "Is it a missing bug")
  private Boolean missingBug;

  @Size(max = MAX_TAGS_NUM)
  @Schema(description = "Report ids")
  private LinkedHashSet<Long> tagIds;

  @Schema(example = "MEDIUM", description = "Task priority, default MEDIUM")
  private Priority priority;

  //@Future Fix:: Save expired task exception
  @Schema(example = "2023-01-02 12:00:00", description = "Task deadline")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime deadlineDate;

  @Valid
  @Size(max = MAX_ATTACHMENT_NUM)
  @Schema(description = "Task attachments")
  private List<Attachment> attachments;

  @EditorContentLength(max = 6000)
  @Schema(description = "Task description")
  private String description;

  @Min(0)
  @Max(MAX_WORKLOAD_NUM)
  @Schema(description = "Task Workload")
  private BigDecimal evalWorkload;

  @Min(0)
  @Max(MAX_WORKLOAD_NUM)
  @Schema(description = "Actual usage workload")
  private BigDecimal actualWorkload;

  private Long parentTaskId;

  @Size(max = MAX_CASE_OR_TASK_REFS_NUM)
  @Schema(description = "Reference task ids")
  private LinkedHashSet<Long> refTaskIds;

  @Size(max = MAX_CASE_OR_TASK_REFS_NUM)
  @Schema(description = "Reference case ids")
  private LinkedHashSet<Long> refCaseIds;
}
