package cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.meeting;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.core.angustester.domain.task.meeting.TaskMeetingType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class TaskMeetingSearchDto extends PageQuery {

  private Long id;

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @ApiModelProperty(value = "Meeting sprint id")
  private Long sprintId;

  @ApiModelProperty(value = "Meeting moderator id")
  private Long moderatorId;

  @Length(max = DEFAULT_NAME_LENGTH_X2)
  @ApiModelProperty(value = "Meeting subject.")
  private String subject;

  @ApiModelProperty(value = "Meeting type, Specify the type of meeting for identification and management purposes.")
  private TaskMeetingType type;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}
