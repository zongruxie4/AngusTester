package cloud.xcan.sdf.core.angustester.domain.mock;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Note: The current class field order is consistent with the export order and configuration header
 * message. Please do not modify the order arbitrarily.
 *
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockResourcesCreationCount {

  @ApiModelProperty(value = "Total number of mock service")
  private long allService;

  @ApiModelProperty(value = "Total number of mock api")
  private long allApi;

  @ApiModelProperty(value = "Total number of mock response")
  private long allResponse;

  @ApiModelProperty(value = "Total number of mock pushback")
  private long allPushback;

}
