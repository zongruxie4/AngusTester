package cloud.xcan.sdf.core.angustester.domain.task.sprint;


import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.api.enums.EvalWorkloadMethod;
import cloud.xcan.sdf.api.pojo.Attachment;
import cloud.xcan.sdf.api.pojo.IdAndCreatedDateBase;
import cloud.xcan.sdf.api.pojo.Progress;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.task.meeting.TaskMeeting;
import cloud.xcan.sdf.core.jpa.multitenancy.TenantAuditingEntity;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;


@Entity
@Table(name = "task_sprint")
@Where(clause = "deleted_flag = 0 ")
@SQLDelete(sql = "update func_sprint set deleted_flag = 1 where id = ?")
@Setter
@Getter
@Accessors(chain = true)
public class TaskSprint extends TenantAuditingEntity<TaskSprint, Long> implements ActivityResource,
    IdAndCreatedDateBase<TaskSprint> {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "auth_flag")
  private Boolean authFlag;

  @Column(name = "name")
  private String name;

  @Column(name = "status")
  @Enumerated(EnumType.STRING)
  private TaskSprintStatus status;

  @Column(name = "start_date")
  private LocalDateTime startDate;

  @Column(name = "deadline_date")
  private LocalDateTime deadlineDate;

  @Column(name = "owner_id")
  private Long ownerId;

  @Column(name = "task_prefix")
  private String taskPrefix;

  @Column(name = "eval_workload_method")
  @Enumerated(EnumType.STRING)
  private EvalWorkloadMethod evalWorkloadMethod;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "attachments")
  public List<Attachment> attachments;

  @Column(name = "acceptance_criteria")
  private String acceptanceCriteria;

  @Column(name = "other_information")
  private String otherInformation;

  @Column(name = "deleted_flag")
  private Boolean deletedFlag;

  @Column(name = "deleted_by")
  private Long deletedBy;

  @Column(name = "deleted_date")
  private LocalDateTime deletedDate;

  @Transient
  private long taskNum;
  @Transient
  private long validTaskNum;
  @Transient
  private Progress progress;
  @Transient
  private List<UserInfo> members;
  @Transient
  private String ownerName;
  @Transient
  private String ownerAvatar;
  @Transient
  private List<TaskMeeting> meetings;

  @Override
  public Long getParentId() {
    return projectId;
  }

  @Override
  public Long identity() {
    return this.id;
  }

}
