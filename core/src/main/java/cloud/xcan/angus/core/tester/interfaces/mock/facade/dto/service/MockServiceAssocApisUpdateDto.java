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
  @Schema(description = "Mock service id", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Schema(description = "Services id. The parameters projectId and apiIds must have one, if all are empty, clear the association")
  private Long projectId;

  @Size(min = 1, max = MAX_PAGE_SIZE)
  @Schema(description = "Update the apiIds of the associated project. The parameters projectId and apiIds must have one, if all are empty, clear the association")
  private HashSet<Long> apiIds;

}
