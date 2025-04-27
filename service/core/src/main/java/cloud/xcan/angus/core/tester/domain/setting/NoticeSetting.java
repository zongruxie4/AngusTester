package cloud.xcan.angus.core.tester.domain.setting;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.remote.info.IdAndName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class NoticeSetting {

  @NotNull
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private Boolean enabled = false;

  @Schema(description = "Query organization type, it is required when enabled is true")
  private AuthObjectType orgType;

  @Schema(description = "Query organization info, , it is required when enabled is true")
  private LinkedHashSet<IdAndName> orgs;

}
