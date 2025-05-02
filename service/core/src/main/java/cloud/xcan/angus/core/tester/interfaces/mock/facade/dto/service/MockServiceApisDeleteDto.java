package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service;

import cloud.xcan.angus.mockservice.api.MockApisDeleteDto;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
@ToString
public class MockServiceApisDeleteDto {

  @Schema(description = "Whether to notify other controllers to handle",requiredMode = RequiredMode.REQUIRED)
  private boolean broadcast;

  @Valid
  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private MockApisDeleteDto cmdParams;

}
