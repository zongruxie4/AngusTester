package cloud.xcan.angus.core.tester.interfaces.issue.facade.vo;

import cloud.xcan.angus.api.commonlink.associate.AssociateUserType;
import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.enums.Priority;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.tester.domain.issue.BugLevel;
import cloud.xcan.angus.core.tester.domain.issue.TaskStatus;
import cloud.xcan.angus.core.tester.domain.issue.TaskType;
import cloud.xcan.angus.core.tester.interfaces.test.facade.vo.FuncCaseInfoVo;
import cloud.xcan.angus.remote.NameJoinField;
import cloud.xcan.angus.remote.vo.IdAndNameVo;
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
public class TaskDetailVo {

  private Long id;

  private String name;

  private String code;

  private Long projectId;

  //@NameJoinField(id = "projectId", repository = "projectRepo")
  //private String projectName;

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

  private Progress progress;

  private Progress subTaskProgress;

  private TaskType taskType;

  private BugLevel bugLevel;

  @Schema(description = "Version of software for the task")
  private String softwareVersion;

  private LocalDateTime startDate;

  private LocalDateTime deadlineDate;

  private LocalDateTime canceledDate;

  public LocalDateTime confirmedDate;

  private LocalDateTime completedDate;

  public LocalDateTime processedDate;

  private Long assigneeId;

  @NameJoinField(id = "assigneeId", repository = "commonUserBaseRepo")
  private String assigneeName;

  private Long confirmerId;

  @NameJoinField(id = "confirmerId", repository = "commonUserBaseRepo")
  private String confirmerName;

  @Schema(description = "Task tester")
  private Long testerId;

  @NameJoinField(id = "testerId", repository = "commonUserBaseRepo")
  private String testerName;

  private Boolean missingBug;

  private Boolean unplanned;

  private Long parentTaskId;

  @NameJoinField(id = "parentTaskId", repository = "taskInfoRepo")
  private String parentTaskName;

  private List<TaskInfoVo> subTaskInfos;

  private List<TaskInfoVo> refTaskInfos;

  private List<FuncCaseInfoVo> refCaseInfos;

  private List<IdAndNameVo> tags;

  private EvalWorkloadMethod evalWorkloadMethod;

  private BigDecimal evalWorkload;

  private BigDecimal actualWorkload;

  @Schema(description = "The number of task processing failures")
  private int failNum;

  @Schema(description = "The total number of tasks processed")
  private int totalNum;

  private List<Attachment> attachments;

  private String description;

  private List<AssociateUserType> currentAssociateType;

  private Boolean confirmTask;

  private Boolean overdue;

  private Boolean favourite;

  private Boolean follow;

  private int commentNum;

  private int remarkNum;

  private int activityNum;

  private Long tenantId;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String creator;

  private LocalDateTime createdDate;

  private Long modifiedBy;

  @NameJoinField(id = "modifiedBy", repository = "commonUserBaseRepo")
  private String modifier;

  private LocalDateTime modifiedDate;

}
