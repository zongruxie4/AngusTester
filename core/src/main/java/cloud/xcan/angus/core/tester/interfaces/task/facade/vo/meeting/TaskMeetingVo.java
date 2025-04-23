package cloud.xcan.angus.core.tester.interfaces.task.facade.vo.meeting;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.core.tester.domain.task.meeting.TaskMeetingType;
import cloud.xcan.angus.remote.NameJoinField;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class TaskMeetingVo {

  private Long id;

  private Long projectId;

  private Long sprintId;

  private String subject;

  private TaskMeetingType type;

  private String date;

  private String time;

  private String location;

  private UserInfo moderator;

  private List<UserInfo> participants;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  private LocalDateTime lastModifiedDate;

}
