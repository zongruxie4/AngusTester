package cloud.xcan.angus.core.tester.domain.mock;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Note: The current class field order is consistent with the export order and configuration header
 * message. Please do not modify the order arbitrarily.
 *
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockResourcesCreationCount {

  @Schema(description = "Total number of mock service")
  private long allService;

  @Schema(description = "Total number of mock api")
  private long allApi;

  @Schema(description = "Total number of mock response")
  private long allResponse;

  @Schema(description = "Total number of mock pushback")
  private long allPushback;

}
