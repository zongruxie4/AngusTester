package cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto.baseline;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X2;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_REMARK_LENGTH;

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
public class FuncBaselineUpdateDto {

  @ApiModelProperty("It is required to add a baseline.")
  private Long id;

  @ApiModelProperty(value = "Baseline name, Brief overview of the baseline, supporting up to 200 characters.")
  @Length(max = DEFAULT_NAME_LENGTH_X2)
  private String name;

  //@ApiModelProperty(value = "Plan id")
  //private Long planId;

  @Length(max = DEFAULT_REMARK_LENGTH)
  @ApiModelProperty(value = "Baseline information")
  private String description;

  //@Size(max = MAX_OPT_CASE_NUM)
  //@ApiModelProperty("It is valid to add a baseline.")
  //private LinkedHashSet<Long> caseIds;

}
