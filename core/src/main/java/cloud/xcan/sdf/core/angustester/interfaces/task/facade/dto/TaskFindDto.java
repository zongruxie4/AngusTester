package cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BID_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.enums.Result;
import cloud.xcan.sdf.core.angustester.domain.task.BugLevel;
import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintPermission;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
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
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @ApiModelProperty(value = "Task id")
  private Long id;

  @Length(max = DEFAULT_NAME_LENGTH_X2)
  @ApiModelProperty(value = "Task name")
  private String name;

  @Length(max = DEFAULT_BID_LENGTH)
  @ApiModelProperty(value = "Task code")
  private String code;

  @ApiModelProperty(value = "Version of software for the task")
  private String softwareVersion;

  @ApiModelProperty(value = "Task sprint id")
  private Long sprintId;

  @ApiModelProperty(value = "Task module id")
  private Long moduleId;

  @ApiModelProperty(value = "Backlog flag. true: Sprint backlog, false: Product backlog")
  private Boolean backlogFlag;

  @ApiModelProperty(value = "The scenario or api ID associated with the task")
  public Long targetId;

  @ApiModelProperty(value = "The scenario dir or services ID associated with the task")
  public Long targetParentId;

  @ApiModelProperty(value = "Task type")
  private TaskType taskType;

  @ApiModelProperty(value = "Bug level")
  private BugLevel bugLevel;

  @ApiModelProperty(value = "Task test type")
  private TestType testType;

  @ApiModelProperty(value = "Assignee id")
  private Long assigneeId;

  @ApiModelProperty(value = "Confirmor id")
  private Long confirmorId;

  @ApiModelProperty(value = "Tester id")
  private Long testerId;

  @ApiModelProperty(value = "Missing bug flag")
  private Boolean missingBugFlag;

  private Boolean unplannedFlag;

  @ApiModelProperty(value = "Task parent id")
  private Long parentTaskId;

  @ApiModelProperty(value = "Tag id")
  private Long tagId;

  @ApiModelProperty(value = "Task priority")
  private Priority priority;

  @ApiModelProperty(value = "Task eval workload")
  private BigDecimal evalWorkload;

  @ApiModelProperty(value = "Task actual workload")
  private BigDecimal actualWorkload;

  @ApiModelProperty(value = "Task status")
  private TaskStatus status;

  @ApiModelProperty(value = "Whether the task is overdue")
  private Boolean overdueFlag;

  @ApiModelProperty(value = "The number of tasks failures")
  private Integer failNum;

  @ApiModelProperty(value = "The number of tasks processed")
  private Integer totalNum;

  @ApiModelProperty(value = "Execution id")
  private Long execId;

  @ApiModelProperty(value = "Execution user id")
  private Long execBy;

  @ApiModelProperty(value = "Task execution result")
  private Result execResult;

  @ApiModelProperty(value = "Task start date")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime startDate;

  @ApiModelProperty(value = "Task deadline")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime deadlineDate;

  @ApiModelProperty(value = "Task confirmed date")
  @DateTimeFormat(pattern = DATE_FMT)
  public LocalDateTime confirmedDate;

  @ApiModelProperty(value = "Task completed date")
  @DateTimeFormat(pattern = DATE_FMT)
  public LocalDateTime completedDate;

  @ApiModelProperty(value = "Task processed date")
  @DateTimeFormat(pattern = DATE_FMT)
  public LocalDateTime processedDate;

  @ApiModelProperty(value = "Task canceled date")
  @DateTimeFormat(pattern = DATE_FMT)
  public LocalDateTime canceledDate;

  @ApiModelProperty(value = "Task execution date")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime execDate;

  @ApiModelProperty(value = "Task creator")
  private Long createdBy;

  @ApiModelProperty(value = "Task creation date")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  @ApiModelProperty(value = "Task last modified user")
  private Long lastModifiedBy;

  @ApiModelProperty(value = "Task last modified date")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime lastModifiedDate;

  @ApiModelProperty(value = "Required when app administrators query all projects")
  private Boolean adminFlag;

  @ApiModelProperty(value = "Required when the user query has the one permission project")
  private TaskSprintPermission hasPermission;

  private Long favouriteBy;

  private Long followBy;

  private Long commentBy;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}
