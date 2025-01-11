package cloud.xcan.sdf.core.angustester.domain.task.summary;


import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.enums.EvalWorkloadMethod;
import cloud.xcan.sdf.core.angustester.domain.task.meeting.TaskMeeting;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class TaskSprintSummary {

  private Long id;

  private Long projectId;

  @NameJoinField(id = "projectId", repository = "projectRepo")
  private String projectName;

  private String name;

  private TaskSprintStatus status;

  private Boolean authFlag;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime startDate;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime deadlineDate;

  private Long ownerId;

  //@NameJoinField(id = "ownerId", repository = "commonUserBaseRepo")
  private String ownerName;

  private String ownerAvatar;

  private String taskPrefix;

  private EvalWorkloadMethod evalWorkloadMethod;

  // public List<Attachment> attachments;

  private String acceptanceCriteria;

  private List<TaskMeeting> meetings;

  private String otherInformation;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String createdByName;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @NameJoinField(id = "lastModifiedBy", repository = "commonUserBaseRepo")
  private String lastModifiedByName;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime lastModifiedDate;

}
