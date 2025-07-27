package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service;


import static cloud.xcan.angus.remote.ApiConstant.RLimit.MAX_PAGE_SIZE;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockServiceAssocApisUpdateDto {

  @NotNull
  @Schema(description = "Mock service identifier for association update operation", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Schema(description = "Project identifier for association update; at least one of projectId or apiIds must be provided. If both are empty, the association will be cleared.")
  private Long projectId;

  @Size(min = 1, max = MAX_PAGE_SIZE)
  @Schema(description = "API identifiers to associate with the service; at least one of projectId or apiIds must be provided. If both are empty, the association will be cleared.")
  private HashSet<Long> apiIds;

}
