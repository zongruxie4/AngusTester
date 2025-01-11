package cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_REMARK_LENGTH_X30;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class TaskDescriptionReplaceDto {

  @Length(max = DEFAULT_REMARK_LENGTH_X30)
  @ApiModelProperty(value = "Task description, allow clear description by empty value")
  private String description;

}
