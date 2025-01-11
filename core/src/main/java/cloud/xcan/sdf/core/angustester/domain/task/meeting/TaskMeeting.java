package cloud.xcan.sdf.core.angustester.domain.task.meeting;

import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.api.pojo.IdAndCreatedDateBase;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.jpa.auditor.AuditingEntity;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "task_meeting")
@Setter
@Getter
@Accessors(chain = true)
public class TaskMeeting extends AuditingEntity<TaskMeeting, Long> implements ActivityResource,
    IdAndCreatedDateBase<TaskMeeting> {

  @Id
  private Long id;

  @Column(name = "project_id")
  private Long projectId;

  @Column(name = "sprint_id")
  private Long sprintId;

  private String subject;

  @Enumerated(EnumType.STRING)
  private TaskMeetingType type;

  private String date;

  private String time;

  private String location;

  @Column(name = "moderator_id")
  private Long moderatorId;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "moderator")
  private UserInfo moderator;

  @Type(type = "json")
  @Column(columnDefinition = "json", name = "participants")
  private List<UserInfo> participants;

  private String content;

  @Override
  public Long identity() {
    return id;
  }

  @Override
  public String getName() {
    return subject;
  }

  @Override
  public Long getParentId() {
    return projectId;
  }
}
