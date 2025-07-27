package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ApisMoveDto {

  @NotNull
  @Size(min = 1)
  @Schema(description = "Source API identifiers for relocation operation", requiredMode = RequiredMode.REQUIRED)
  private List<Long> apiIds;

  @NotNull
  @Schema(description = "Target service identifier for API relocation destination", requiredMode = RequiredMode.REQUIRED)
  private Long targetServiceId;

}
