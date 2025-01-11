package cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto;

import cloud.xcan.sdf.api.commonlink.services.ServicesPermission;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ServicesReplaceAuthDto {

  @NotNull
  @ApiModelProperty
  private List<ServicesPermission> authData;
}
