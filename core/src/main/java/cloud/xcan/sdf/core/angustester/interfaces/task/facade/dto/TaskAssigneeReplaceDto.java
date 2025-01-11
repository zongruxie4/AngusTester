package cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto;


import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Valid
@Getter
@Setter
@Accessors(chain = true)
public class TaskAssigneeReplaceDto {

  @ApiModelProperty(value = "Assignee id, allow clear assignee by empty value")
  private Long assigneeId;

}
