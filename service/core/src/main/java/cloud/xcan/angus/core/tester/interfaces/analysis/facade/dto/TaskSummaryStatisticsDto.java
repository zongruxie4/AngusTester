package cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BID_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_KEY_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.enums.Result;
import cloud.xcan.angus.core.tester.domain.task.BugLevel;
import cloud.xcan.angus.core.tester.domain.task.TaskStatus;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.core.tester.domain.task.sprint.TaskSprintPermission;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;


@Getter
@Setter
@Accessors(chain = true)
public class TaskSummaryStatisticsDto extends PageQuery {

  @Schema(description = "Task identifier")
  private Long id;

  @NotNull
  @Schema(description = "Project identifier for task statistics", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Task name for search and filtering")
  private String name;

  @Length(max = MAX_BID_LENGTH)
  @Schema(description = "Task code for identification and search")
  private String code;

  @Length(max = MAX_KEY_LENGTH)
  @Schema(description = "Software version associated with the task")
  private String version;

  @Schema(description = "Sprint identifier for task categorization")
  private Long sprintId;

  @Schema(description = "Module identifier for task categorization")
  private Long moduleId;

  @Schema(description = "Backlog classification flag: true for sprint backlog, false for product backlog")
  private Boolean backlog;

  @Schema(description = "Associated scenario or API identifier")
  public Long targetId;

  @Schema(description = "Associated scenario directory or services identifier")
  public Long targetParentId;

  @Schema(description = "Task type for categorization")
  private TaskType taskType;

  @Schema(description = "Bug severity level")
  private BugLevel bugLevel;

  @Schema(description = "Task test type for execution categorization")
  private TestType testType;

  @Schema(description = "Assignee user identifier")
  private Long assigneeId;

  @Schema(description = "Confirmer user identifier")
  private Long confirmerId;

  @Schema(description = "Tester user identifier")
  private Long testerId;

  @Schema(description = "Missing bug detection flag")
  private Boolean missingBug;

  @Schema(description = "Flag indicating if task is unplanned")
  private Boolean unplanned;

  @Schema(description = "Tag identifier for task categorization")
  private Long tagId;

  @Schema(description = "Task priority level")
  private Priority priority;

  @Schema(description = "Estimated workload for the task")
  private BigDecimal evalWorkload;

  @Schema(description = "Actual workload consumed by the task")
  private BigDecimal actualWorkload;

  @Schema(description = "Current task status")
  private TaskStatus status;

  @Schema(description = "Flag indicating if task is overdue")
  private Boolean overdue;

  @Schema(description = "Number of task execution failures")
  private Integer failNum;

  @Schema(description = "Total number of task executions")
  private Integer totalNum;

  @Schema(description = "Execution identifier for task runs")
  private Long execId;

  @Schema(description = "Execution user identifier")
  private Long execBy;

  @Schema(description = "Task execution result status")
  private Result execResult;

  @Schema(description = "Task start timestamp")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime startDate;

  @Schema(description = "Task deadline timestamp")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime deadlineDate;

  @Schema(description = "Task confirmation timestamp")
  @DateTimeFormat(pattern = DATE_FMT)
  public LocalDateTime confirmedDate;

  @Schema(description = "Task completion timestamp")
  @DateTimeFormat(pattern = DATE_FMT)
  public LocalDateTime completedDate;

  @Schema(description = "Task processing timestamp")
  @DateTimeFormat(pattern = DATE_FMT)
  public LocalDateTime processedDate;

  @Schema(description = "Task cancellation timestamp")
  @DateTimeFormat(pattern = DATE_FMT)
  public LocalDateTime canceledDate;

  @Schema(description = "Task execution timestamp")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime execDate;

  @Schema(description = "Task creator user identifier")
  private Long createdBy;

  @Schema(description = "Task creation timestamp")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  @Schema(description = "Last modifier user identifier")
  private Long lastModifiedBy;

  @Schema(description = "Last modification timestamp")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime lastModifiedDate;

  @Schema(description = "Administrator access flag for cross-project task queries")
  private Boolean admin;

  @Schema(description = "Permission level for task access control")
  private TaskSprintPermission hasPermission;

}
