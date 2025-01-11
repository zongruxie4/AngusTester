package cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto.sprint.auth;


import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.angustester.domain.task.sprint.TaskSprintPermission;
import cloud.xcan.sdf.web.validator.annotations.CollectionValueNotNull;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Set;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class TaskSprintAuthAddDto {

  @NotNull
  @ApiModelProperty(example = "USER", required = true)
  private AuthObjectType authObjectType;

  @NotNull
  @ApiModelProperty(example = "1", required = true)
  private Long authObjectId;

  @NotEmpty
  @Size(min = 1)
  @CollectionValueNotNull // Fix:: invalid enumeration value is null element
  @ApiModelProperty(value = "Authorization permissions(Operation permission)", required = true)
  private Set<TaskSprintPermission> permissions;

}




