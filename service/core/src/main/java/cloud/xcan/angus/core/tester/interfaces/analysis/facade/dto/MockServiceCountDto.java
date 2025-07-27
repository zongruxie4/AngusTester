package cloud.xcan.angus.core.tester.interfaces.analysis.facade.dto;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

@Setter
@Getter
public class MockServiceCountDto {

  @Schema(description = "Mock service identifier for count statistics")
  private Long mockServiceId;

}
