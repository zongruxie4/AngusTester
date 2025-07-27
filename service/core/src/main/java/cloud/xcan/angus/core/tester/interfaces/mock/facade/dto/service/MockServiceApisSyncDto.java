package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service;

import cloud.xcan.angus.mockservice.api.MockApisSyncDto;
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
public class MockServiceApisSyncDto {

  @Schema(description = "Broadcast flag for multi-controller notification and coordination", requiredMode = RequiredMode.REQUIRED)
  private boolean broadcast;

  @Valid
  @NotNull
  @Schema(description = "Mock APIs batch synchronization command parameters for distributed operation", requiredMode = RequiredMode.REQUIRED)
  private MockApisSyncDto cmdParams;

}
