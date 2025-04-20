package cloud.xcan.angus.core.tester.domain.task.meeting;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.api.pojo.IdAndCreatedDateBase;
import cloud.xcan.angus.core.jpa.auditor.AuditingEntity;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
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

  @Type(JsonType.class)
  @Column(columnDefinition = "json", name = "moderator")
  private UserInfo moderator;

  @Type(JsonType.class)
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
