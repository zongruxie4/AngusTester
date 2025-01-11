package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto;

import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ApisMoveDto {

  @NotNull
  @Size(min = 1)
  @ApiModelProperty(value = "Source api ids", required = true)
  private List<Long> apiIds;

  @NotNull
  @ApiModelProperty(example = "1", value = "Target services id", required = true)
  private Long targetServiceId;

}
