package cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.meeting;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_ADDRESS_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_BATCH_SIZE;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_REMARK_LENGTH_X30;

import cloud.xcan.sdf.api.commonlink.user.UserInfo;
import cloud.xcan.sdf.core.angustester.domain.task.meeting.TaskMeetingType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class TaskMeetingReplaceDto {

  @ApiModelProperty("Modify meeting id. Create a new meeting when the value is null")
  private Long id;

  //@NotNull
  @ApiModelProperty(value = "Project id", example = "1"/*, required = true*/)
  private Long projectId;

  @ApiModelProperty(value = "Meeting sprint id")
  private Long sprintId;

  @Length(max = DEFAULT_NAME_LENGTH_X2)
  @ApiModelProperty(value = "Meeting subject.", required = true)
  private String subject;

  @NotNull
  @ApiModelProperty(value = "Meeting type, Specify the type of meeting for identification and management purposes.", required = true)
  private TaskMeetingType type;

  @NotEmpty
  @ApiModelProperty(value = "Meeting date, record the specific date of the meeting", required = true)
  private String date;

  @NotEmpty
  @ApiModelProperty(value = "Meeting start and time, record the start and end times of the meeting", required = true)
  private String time;

  @Length(max = DEFAULT_ADDRESS_LENGTH)
  @ApiModelProperty(value = "Meeting location, record the meeting venue or the link for online meetings")
  private String location;

  @NotNull
  @ApiModelProperty(value = "Meeting moderator, specify the host of the meeting", required = true)
  private UserInfo moderator;

  @NotEmpty
  @Size(max = DEFAULT_BATCH_SIZE)
  @ApiModelProperty(value = "Meeting participants, list the names of all participants", required = true)
  private List<UserInfo> participants;

  @NotEmpty
  @Length(max = DEFAULT_REMARK_LENGTH_X30)
  @ApiModelProperty(value = "Meeting content, record the content of the meeting", required = true)
  private String content;

}
