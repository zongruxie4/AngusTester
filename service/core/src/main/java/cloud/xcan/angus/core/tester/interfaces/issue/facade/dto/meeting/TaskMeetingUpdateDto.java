package cloud.xcan.angus.core.tester.interfaces.issue.facade.dto.meeting;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_ADDRESS_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.core.tester.domain.issue.meeting.TaskMeetingType;
import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class TaskMeetingUpdateDto {

  @NotNull
  @Schema(description = "Meeting identifier for modification", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  //@NotNull
  //@Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  //private Long projectId;

  @Schema(description = "Sprint identifier for meeting iteration association")
  private Long sprintId;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Meeting subject for identification and agenda summary")
  private String subject;

  @Schema(description = "Meeting type classification for workflow management and categorization")
  private TaskMeetingType type;

  @Schema(description = "Meeting date for scheduling and calendar management")
  private String date;

  @Schema(description = "Meeting time range for start and end time specification")
  private String time;

  @Length(max = MAX_ADDRESS_LENGTH)
  @Schema(description = "Meeting location or online meeting link for venue specification")
  private String location;

  @Schema(description = "Meeting moderator information for host identification and contact")
  private UserInfo moderator;

  @Size(max = MAX_BATCH_SIZE)
  @Schema(description = "Meeting participants list for attendance tracking and notification")
  private List<UserInfo> participants;

  @EditorContentLength(max = 6000)
  @Schema(description = "Meeting agenda and discussion content for documentation")
  private String content;

}
