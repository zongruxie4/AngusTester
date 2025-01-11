package cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service;

import static cloud.xcan.sdf.api.ApiConstant.RLimit.MAX_PAGE_SIZE;

import io.swagger.annotations.ApiModelProperty;
import java.util.HashSet;
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
public class MockServiceAssocApisUpdateDto {

  @NotNull
  @ApiModelProperty(value = "Mock service id", required = true)
  private Long id;

  @ApiModelProperty(value = "Services id. The parameters projectId and apiIds must have one, if all are empty, clear the association")
  private Long projectId;

  @Size(min = 1, max = MAX_PAGE_SIZE)
  @ApiModelProperty(value = "Update the apiIds of the associated project. The parameters projectId and apiIds must have one, if all are empty, clear the association")
  private HashSet<Long> apiIds;

}
