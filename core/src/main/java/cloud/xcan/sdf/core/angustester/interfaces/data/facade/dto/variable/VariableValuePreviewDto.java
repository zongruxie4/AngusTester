package cloud.xcan.sdf.core.angustester.interfaces.data.facade.dto.variable;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_PARAM_VALUE_LENGTH;

import cloud.xcan.angus.model.element.extraction.DefaultExtraction;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class VariableValuePreviewDto {

  @ApiModelProperty(value = "Variable id")
  private Long id;

  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "Variable name")
  private String name;

  @Length(max = MAX_PARAM_VALUE_LENGTH)
  @ApiModelProperty(value = "Required when non extraction type variable, the value of extraction type will be automatically set after extraction.")
  private String value;

  @Valid
  @ApiModelProperty(value = "Extraction rules configuration")
  private DefaultExtraction extraction;

}
