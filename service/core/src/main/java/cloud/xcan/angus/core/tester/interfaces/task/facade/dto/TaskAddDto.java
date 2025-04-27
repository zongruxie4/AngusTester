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
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
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
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@Accessors(chain = true)
public class TaskAddDto {

  @Schema(description = "Project id. It is required to create a backlog", example = "1")
  private Long projectId;

  //@NotNull
  @Schema(description = "Sprint Id. Note: If the value is empty, it will be created as a Backlog", example = "1"/*, requiredMode = RequiredMode.REQUIRED*/)
  private Long sprintId;

  @Schema(description = "Function case module id")
  private Long moduleId;

  @NotEmpty
  @Length(max = MAX_NAME_LENGTH_X4)
  @Schema(description = "Task name", example = "A task", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Length(max = MAX_KEY_LENGTH)
  @Schema(description = "Version of software for the task")
  private String softwareVersion;

  @NotNull
  @Schema(description = "Task type", example = "TASK", requiredMode = RequiredMode.REQUIRED)
  private TaskType taskType;

  @Schema(description = "Bug level, default MINOR", example = "MINOR")
  private BugLevel bugLevel;

  @Schema(description = "The scenario or api ID associated with the task")
  private Long targetId;

  @Schema(example = "FUNCTIONAL")
  private TestType testType;

  //@NotNull Fix:: Backlog assignee is nullable
  @Schema(description = "Assignee id"/*, requiredMode = RequiredMode.REQUIRED*/)
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

  @Schema(example = "MEDIUM", description = "Default MEDIUM")
  private Priority priority = Priority.DEFAULT;

  @Future
  //@NotNull Fix:: Backlog deadlineDate is nullable
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

  //@Min(0)
  //@Max(MAX_WORKLOAD_NUM)
  //@Schema(description = "Actual usage workload")
  //private BigDecimal actualWorkload;

  private Long parentTaskId;

  @Size(max = MAX_CASE_OR_TASK_REFS_NUM)
  @Schema(description = "Reference task ids")
  private LinkedHashSet<Long> refTaskIds;

  @Size(max = MAX_CASE_OR_TASK_REFS_NUM)
  @Schema(description = "Reference case ids")
  private LinkedHashSet<Long> refCaseIds;
}
