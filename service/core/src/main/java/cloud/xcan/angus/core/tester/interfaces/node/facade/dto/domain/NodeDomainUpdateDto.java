package cloud.xcan.angus.core.tester.interfaces.node.facade.dto.domain;

import cloud.xcan.angus.api.enums.NormalStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class NodeDomainUpdateDto {

  @NotNull
  @Schema(description = "Domain identifier for update operation", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  //  @Length(max = MAX_NAME_LENGTH_X2)
  //  @Schema(example = "example.com")
  //  private String name;

  @NotNull
  @Schema(description = "Domain status for lifecycle management and access control", example = "NORMAL", requiredMode = RequiredMode.REQUIRED)
  private NormalStatus status;

}
