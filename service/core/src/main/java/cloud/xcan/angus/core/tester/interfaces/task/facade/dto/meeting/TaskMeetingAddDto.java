package cloud.xcan.angus.core.tester.interfaces.task.facade.dto.meeting;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_ADDRESS_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_BATCH_SIZE;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.api.commonlink.user.UserInfo;
import cloud.xcan.angus.core.tester.domain.task.meeting.TaskMeetingType;
import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
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
public class TaskMeetingAddDto {

  @NotNull
  @Schema(description = "Project identifier for meeting organization and access control", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Sprint identifier for meeting iteration association")
  private Long sprintId;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Meeting subject for identification and agenda summary", requiredMode = RequiredMode.REQUIRED)
  private String subject;

  @NotNull
  @Schema(description = "Meeting type classification for workflow management and categorization", requiredMode = RequiredMode.REQUIRED)
  private TaskMeetingType type;

  @NotEmpty
  @Schema(description = "Meeting date for scheduling and calendar management", requiredMode = RequiredMode.REQUIRED)
  private String date;

  @NotEmpty
  @Schema(description = "Meeting time range for start and end time specification", requiredMode = RequiredMode.REQUIRED)
  private String time;

  @Length(max = MAX_ADDRESS_LENGTH)
  @Schema(description = "Meeting location or online meeting link for venue specification")
  private String location;

  @NotNull
  @Schema(description = "Meeting moderator information for host identification and contact", requiredMode = RequiredMode.REQUIRED)
  private UserInfo moderator;

  @NotEmpty
  @Size(max = MAX_BATCH_SIZE)
  @Schema(description = "Meeting participants list for attendance tracking and notification", requiredMode = RequiredMode.REQUIRED)
  private List<UserInfo> participants;

  @NotEmpty
  @EditorContentLength(max = 6000)
  @Schema(description = "Meeting agenda and discussion content for documentation", requiredMode = RequiredMode.REQUIRED)
  private String content;
}
