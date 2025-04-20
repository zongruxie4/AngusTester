package cloud.xcan.angus.core.tester.domain.task.sprint;


import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.enums.EvalWorkloadMethod;
import cloud.xcan.angus.api.pojo.Attachment;
import cloud.xcan.angus.api.pojo.IdAndCreatedDateBase;
import cloud.xcan.angus.api.pojo.Progress;
import cloud.xcan.angus.core.jpa.multitenancy.TenantAuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.task.meeting.TaskMeeting;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.Type;


@Entity
@Table(name = "task_sprint")
@SQLRestriction("deleted = 0 ")
@SQLDelete(sql = "update func_sprint set deleted = 1 where id = ?")
@Setter
@Getter
@Accessors(chain = true)
public class TaskSprint extends TenantAuditingEntity<TaskSprint, Long> implements ActivityResource,
    IdAndCreatedDateBase<TaskSprint> {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "auth")
  private Boolean auth;

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

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "attachments")
  public List<Attachment> attachments;

  @Column(name = "acceptance_criteria")
  private String acceptanceCriteria;

  @Column(name = "other_information")
  private String otherInformation;

  @Column(name = "deleted")
  private Boolean deleted;

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
