package cloud.xcan.sdf.core.angustester.interfaces.task.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_KEY_LENGTH;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class TaskVersionReplaceDto {

  @Length(max = DEFAULT_KEY_LENGTH)
  @ApiModelProperty(value = "Version of software for the task, allow clear version by empty value")
  private String softwareVersion;

}
