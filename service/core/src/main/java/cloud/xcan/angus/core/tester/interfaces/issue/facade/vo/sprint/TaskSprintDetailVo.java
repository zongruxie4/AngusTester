package cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.sprint;


import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.tester.domain.issue.sprint.TaskSprintStatus;
import cloud.xcan.angus.core.tester.interfaces.issue.facade.vo.meeting.TaskMeetingDetailVo;
import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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

  private Boolean auth;

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

  //private Integer deleted;

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
