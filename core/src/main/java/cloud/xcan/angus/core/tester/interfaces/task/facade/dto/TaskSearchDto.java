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
public class TaskSearchDto extends PageQuery {

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Task id")
  private Long id;

  @Schema(description = "Task name")
  private String name;

  @Length(max = MAX_BID_LENGTH)
  @Schema(description = "Task code")
  private String code;

  @Schema(description = "Version of software for the task")
  private String softwareVersion;

  @Schema(description = "Task sprint id")
  private Long sprintId;

  @Schema(description = "Task module id")
  private Long moduleId;

  @Schema(description = "Backlog flag. true: Sprint backlog, false: Product backlog")
  private Boolean backlog;

  @Schema(description = "The scenario or api ID associated with the task")
  public Long targetId;

  @Schema(description = "The scenario dir or services ID associated with the task")
  public Long targetParentId;

  @Schema(description = "Task type")
  private TaskType taskType;

  @Schema(description = "Bug level")
  private BugLevel bugLevel;

  @Schema(description = "Task test type")
  private TestType testType;

  @Schema(description = "Assignee id")
  private Long assigneeId;

  @Schema(description = "Confirmor id")
  private Long confirmorId;

  @Schema(description = "Tester id")
  private Long testerId;

  @Schema(description = "Missing bug flag")
  private Boolean missingBug;

  private Boolean unplanned;

  @Schema(description = "Task parent id")
  private Long parentTaskId;

  @Schema(description = "Tag id")
  private Long tagId;

  @Schema(description = "Task priority")
  private Priority priority;

  @Schema(description = "Task eval workload")
  private BigDecimal evalWorkload;

  @Schema(description = "Task actual workload")
  private BigDecimal actualWorkload;

  @Schema(description = "Task status")
  private TaskStatus status;

  @Schema(description = "Whether the task is overdue")
  private Boolean overdue;

  @Schema(description = "The number of tasks failures")
  private Integer failNum;

  @Schema(description = "The number of tasks processed")
  private Integer totalNum;

  @Schema(description = "Execution id")
  private Long execId;

  @Schema(description = "Execution user id")
  private Long execBy;

  @Schema(description = "Task execution result")
  private Result execResult;

  @Schema(description = "Task start date")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime startDate;

  @Schema(description = "Task deadline")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime deadlineDate;

  @Schema(description = "Task confirmed date")
  @DateTimeFormat(pattern = DATE_FMT)
  public LocalDateTime confirmedDate;

  @Schema(description = "Task completed date")
  @DateTimeFormat(pattern = DATE_FMT)
  public LocalDateTime completedDate;

  @Schema(description = "Task processed date")
  @DateTimeFormat(pattern = DATE_FMT)
  public LocalDateTime processedDate;

  @Schema(description = "Task canceled date")
  @DateTimeFormat(pattern = DATE_FMT)
  public LocalDateTime canceledDate;

  @Schema(description = "Task execution date")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime execDate;

  @Schema(description = "Task creator")
  private Long createdBy;

  @Schema(description = "Task creation date")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  @Schema(description = "Task last modified user")
  private Long lastModifiedBy;

  @Schema(description = "Task last modified date")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime lastModifiedDate;

  @Schema(description = "Required when app administrators query all projects")
  private Boolean admin;

  @Schema(description = "Required when the user query has the one permission project")
  private TaskSprintPermission hasPermission;

  private Long favouriteBy;

  private Long followBy;

  private Long commentBy;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}
