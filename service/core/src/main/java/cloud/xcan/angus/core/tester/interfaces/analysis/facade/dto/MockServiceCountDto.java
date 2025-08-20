package cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MockServiceCountDto {

  @Schema(description = "Mock service identifier for count statistics")
  private Long mockServiceId;

}
