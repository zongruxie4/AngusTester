package cloud.xcan.sdf.core.angustester.interfaces.tag.facade.dto;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class TagUpdateDto {

  @NotNull
  @ApiModelProperty(example = "1", required = true)
  private Long id;

  @NotBlank
  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(example = "Sprint-1", required = true)
  private String name;

}
