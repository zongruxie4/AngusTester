package cloud.xcan.sdf.core.angustester.interfaces.func.facade.dto;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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
public class FuncCaseTesterReplaceDto {

  @NotNull
  @ApiModelProperty(value = "Tester id", required = true)
  private Long testerId;

}
