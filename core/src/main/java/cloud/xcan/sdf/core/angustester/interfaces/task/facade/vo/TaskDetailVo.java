package cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.api.commonlink.associate.AssociateUserType;
import cloud.xcan.sdf.api.enums.EvalWorkloadMethod;
import cloud.xcan.sdf.api.enums.Priority;
import cloud.xcan.sdf.api.enums.Result;
import cloud.xcan.sdf.api.pojo.Attachment;
import cloud.xcan.sdf.api.pojo.Progress;
import cloud.xcan.sdf.api.vo.IdAndNameVo;
import cloud.xcan.sdf.core.angustester.domain.task.BugLevel;
import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.FuncCaseInfoVo;
import io.swagger.annotations.ApiModelProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
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

  private Boolean sprintAuthFlag;

  private Long moduleId;

  @NameJoinField(id = "moduleId", repository = "moduleRepo")
  private String moduleName;

  @ApiModelProperty(value = "Backlog flag. true: Sprint backlog, false: Product backlog")
  private Boolean backlogFlag;

  private Priority priority;

  private TaskStatus status;

  private TaskType taskType;

  private BugLevel bugLevel;

  private TestType testType;

  @ApiModelProperty(value = "Version of software for the task")
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

  private Long confirmorId;

  @NameJoinField(id = "confirmorId", repository = "commonUserBaseRepo")
  private String confirmorName;

  @ApiModelProperty(value = "Task tester")
  private Long testerId;

  @NameJoinField(id = "testerId", repository = "commonUserBaseRepo")
  private String testerName;

  private Boolean missingBugFlag;

  private Boolean unplannedFlag;

  private Long parentTaskId;

  @NameJoinField(id = "parentTaskId", repository = "taskInfoRepo")
  private String parentTaskName;

  private Progress subTaskProgress;

  private List<TaskInfoVo> subTaskInfos;

  private List<TaskInfoVo> refTaskInfos;

  private List<FuncCaseInfoVo> refCaseInfos;

  private List<IdAndNameVo> tags;

  private EvalWorkloadMethod evalWorkloadMethod;

  private BigDecimal evalWorkload;

  private BigDecimal actualWorkload;

  @ApiModelProperty("The number of task processing failures")
  private int failNum;

  @ApiModelProperty("The total number of tasks processed")
  private int totalNum;

  private List<Attachment> attachments;

  private String description;

  private List<AssociateUserType> currentAssociateType;

  private Boolean confirmTaskFlag;

  private Boolean overdueFlag;

  private Long targetId;

  private String targetName;

  private Long targetParentId;

  private String targetParentName;

  private Long scriptId;

  @NameJoinField(id = "scriptId", repository = "scriptInfoRepo")
  private String scriptName;

  private Result execResult;

  private String execFailureMessage;

  private Integer execTestNum;

  private Integer execTestFailureNum;

  private Long execId;

  private String execName;

  private Long execBy;

  @NameJoinField(id = "execBy", repository = "commonUserBaseRepo")
  private String execByName;

  private LocalDateTime execDate;

  private Boolean favouriteFlag;

  private Boolean followFlag;

  private int commentNum;

  private int remarkNum;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}
