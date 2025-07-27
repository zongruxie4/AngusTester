package cloud.xcan.angus.core.tester.interfaces.task.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BID_LENGTH;

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
public class TaskFindDto extends PageQuery {


  @NotNull
  @Schema(description = "Project identifier for task filtering and organization", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Task identifier for specific task lookup")
  private Long id;

  @Schema(description = "Task display name for partial matching search")
  private String name;

  @Length(max = MAX_BID_LENGTH)
  @Schema(description = "Task unique code for identification and tracking")
  private String code;

  @Schema(description = "Software version identifier for task filtering")
  private String softwareVersion;

  @Schema(description = "Sprint identifier for task iteration filtering")
  private Long sprintId;

  @Schema(description = "Module identifier for task categorization filtering")
  private Long moduleId;

  @Schema(description = "Backlog classification flag. true: Sprint backlog, false: Product backlog")
  private Boolean backlog;

  @Schema(description = "Associated API or scenario identifier for test task filtering")
  public Long targetId;

  @Schema(description = "Associated scenario directory or service identifier for hierarchical filtering")
  public Long targetParentId;

  @Schema(description = "Task classification type for workflow filtering")
  private TaskType taskType;

  @Schema(description = "Bug severity level for defect filtering")
  private BugLevel bugLevel;

  @Schema(description = "Test type classification for execution filtering")
  private TestType testType;

  @Schema(description = "Primary assignee identifier for responsibility filtering")
  private Long assigneeId;

  @Schema(description = "Reviewer identifier for approval workflow filtering")
  private Long confirmorId;

  @Schema(description = "Test execution identifier for testing responsibility filtering")
  private Long testerId;

  @Schema(description = "Missing bug flag for defect classification filtering")
  private Boolean missingBug;

  private Boolean unplanned;

  @Schema(description = "Parent task identifier for hierarchical relationship filtering")
  private Long parentTaskId;

  @Schema(description = "Tag identifier for categorization filtering")
  private Long tagId;

  @Schema(description = "Task priority level for scheduling filtering")
  private Priority priority;

  @Schema(description = "Estimated workload for planning filtering")
  private BigDecimal evalWorkload;

  @Schema(description = "Actual workload for performance analysis filtering")
  private BigDecimal actualWorkload;

  @Schema(description = "Task execution status for workflow filtering")
  private TaskStatus status;

  @Schema(description = "Overdue flag for timeline management filtering")
  private Boolean overdue;

  @Schema(description = "Number of task execution failures for quality analysis")
  private Integer failNum;

  @Schema(description = "Total number of task executions for performance metrics")
  private Integer totalNum;

  @Schema(description = "Execution identifier for test run filtering")
  private Long execId;

  @Schema(description = "Execution user identifier for responsibility tracking")
  private Long execBy;

  @Schema(description = "Task execution result for outcome filtering")
  private Result execResult;

  @Schema(description = "Task execution start timestamp for timeline filtering")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime startDate;

  @Schema(description = "Task completion deadline for timeline filtering")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime deadlineDate;

  @Schema(description = "Task confirmation timestamp for approval workflow filtering")
  @DateTimeFormat(pattern = DATE_FMT)
  public LocalDateTime confirmedDate;

  @Schema(description = "Task completion timestamp for workflow status filtering")
  @DateTimeFormat(pattern = DATE_FMT)
  public LocalDateTime completedDate;

  @Schema(description = "Task processing timestamp for execution tracking")
  @DateTimeFormat(pattern = DATE_FMT)
  public LocalDateTime processedDate;

  @Schema(description = "Task cancellation timestamp for status tracking")
  @DateTimeFormat(pattern = DATE_FMT)
  public LocalDateTime canceledDate;

  @Schema(description = "Task execution timestamp for performance analysis")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime execDate;

  @Schema(description = "Task creator identifier for ownership filtering")
  private Long createdBy;

  @Schema(description = "Task creation timestamp for timeline analysis")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  @Schema(description = "Last modifier identifier for change tracking")
  private Long lastModifiedBy;

  @Schema(description = "Last modification timestamp for change analysis")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime lastModifiedDate;

  @Schema(description = "Administrator flag for cross-project query permissions")
  private Boolean admin;

  @Schema(description = "Permission level required for project access filtering")
  private TaskSprintPermission hasPermission;

  private Long favouriteBy;

  private Long followBy;

  private Long commentBy;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}
