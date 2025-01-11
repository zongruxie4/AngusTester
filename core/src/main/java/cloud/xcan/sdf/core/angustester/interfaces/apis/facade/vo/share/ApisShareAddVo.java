package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.share;

import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@Getter
@Setter
@Accessors(chain = true)
public class ApisShareAddVo {

  @ApiModelProperty(value = "Share id")
  private Long id;

  @ApiModelProperty(value = "Access url")
  private String url;

}
