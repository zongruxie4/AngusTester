package cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.auth;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.enums.AuthObjectType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Date;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Valid
@Setter
@Getter
@ApiModel
public class TaskSprintAuthFindDto extends PageQuery {

  //@NotNull -> Transferring values in filters
  @ApiModelProperty(value = "Task sprint id", required = true)
  private Long sprintId;

  @NotNull
  @ApiModelProperty(example = "USER", required = true)
  private AuthObjectType authObjectType;

  @ApiModelProperty(value = "Authorization object id")
  private Long authObjectId;

  private Date createdDate;

}



