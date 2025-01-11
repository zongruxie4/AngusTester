package cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.sprint;


import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.api.enums.EvalWorkloadMethod;
import cloud.xcan.sdf.api.pojo.Attachment;
import cloud.xcan.sdf.api.pojo.Progress;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintStatus;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.meeting.TaskMeetingDetailVo;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import java.util.List;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class TaskSprintDetailVo {

  private Long id;

  private Long projectId;

  //@NameJoinField(id = "projectId", repository = "projectRepo")
  //private String projectName;

  private String name;

  private TaskSprintStatus status;

  private Boolean authFlag;

  private LocalDateTime startDate;

  private LocalDateTime deadlineDate;

  private Long ownerId;

  private String ownerName;

  private String ownerAvatar;

  private String taskPrefix;

  private EvalWorkloadMethod evalWorkloadMethod;

  public List<Attachment> attachments;

  private String acceptanceCriteria;

  private List<TaskMeetingDetailVo> meetings;

  private String otherInformation;

  //private Integer deletedFlag;

  //private Long deletedBy;

  //private LocalDateTime deletedDate;

  private Long tenantId;

  //@NameJoinField(id = "tenantId", repository = "commonTenantRepo")
  //private String tenantName;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

  private long taskNum;

  private long validNum;

  private Progress progress;

  private List<UserInfo> members;

}
