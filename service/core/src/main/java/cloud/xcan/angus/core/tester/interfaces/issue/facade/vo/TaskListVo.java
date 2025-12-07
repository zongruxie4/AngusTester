package cloud.xcan.angus.core.tester.interfaces.issue.facade.vo;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;

import cloud.xcan.angus.api.commonlink.associate.AssociateUserType;
import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.tester.domain.issue.BugLevel;
import cloud.xcan.angus.core.tester.domain.issue.TaskStatus;
import cloud.xcan.angus.core.tester.domain.issue.TaskType;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.FuncCaseInfoVo;
import cloud.xcan.angus.remote.NameJoinField;
import cloud.xcan.angus.remote.vo.IdAndNameVo;
import cloud.xcan.angus.spec.annotations.DoInFuture;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class TaskListVo {

  private Long id;

  private String name;

  private String code;

  private Long projectId;

  @NameJoinField(id = "projectId", repository = "projectRepo")
  private String projectName;

  private Long sprintId;

  @NameJoinField(id = "sprintId", repository = "taskSprintRepo")
  private String sprintName;

  private Boolean sprintAuth;

  private Long moduleId;

  @NameJoinField(id = "moduleId", repository = "moduleRepo")
  private String moduleName;

  @Schema(description = "Backlog flag. true: Sprint backlog, false: Product backlog")
  private Boolean backlog;

  private Priority priority;

  private TaskStatus status;

  /**
   * Use in the Gantt chart
   */
  private Progress progress;

  private TaskType taskType;

  private BugLevel bugLevel;

  @Schema(description = "Version of software for the task")
  private String softwareVersion;

  private LocalDateTime startDate;

  private LocalDateTime deadlineDate;

  public LocalDateTime confirmedDate;

  private LocalDateTime canceledDate;

  private LocalDateTime completedDate;

  public LocalDateTime processedDate;

  private Long assigneeId;

  //@NameJoinField(id = "assigneeId", repository = "commonUserBaseRepo")
  private String assigneeName;

  private String assigneeAvatar;

  private Long confirmerId;

  @NameJoinField(id = "confirmerId", repository = "commonUserBaseRepo")
  private String confirmerName;

  @Schema(description = "Task tester")
  private Long testerId;

  @NameJoinField(id = "testerId", repository = "commonUserBaseRepo")
  private String testerName;

  private Boolean missingBug;

  private Boolean unplanned;

  @DoInFuture("Export parentTaskName")
  private Long parentTaskId;

  @JsonIgnore // Only used by export
  private List<TaskInfoVo> refTaskInfos;

  @JsonIgnore // Only used by export
  private List<FuncCaseInfoVo> refCaseInfos;

  private List<IdAndNameVo> tags;

  private EvalWorkloadMethod evalWorkloadMethod;

  private BigDecimal evalWorkload;

  private BigDecimal actualWorkload;

  @Schema(description = "The number of task processing failures")
  private Integer failNum;

  @Schema(description = "The total number of tasks processed")
  private Integer totalNum;

  private String description;

  private List<AssociateUserType> currentAssociateType;

  private Boolean confirmTask;

  private Boolean overdue;

  private Boolean favourite;

  private Boolean follow;

  private Long tenantId;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  @DateTimeFormat(DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}
