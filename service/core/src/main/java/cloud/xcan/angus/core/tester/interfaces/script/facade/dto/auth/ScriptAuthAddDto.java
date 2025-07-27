package cloud.xcan.angus.core.tester.interfaces.script.facade.dto.auth;

import cloud.xcan.angus.api.commonlink.script.ScriptPermission;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.validator.CollectionValueNotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ScriptAuthAddDto {

  @NotNull
  @Schema(description = "Authorization object type defining the entity category for permission assignment", example = "USER", requiredMode = RequiredMode.REQUIRED)
  private AuthObjectType authObjectType;

  @NotNull
  @Schema(description = "Authorization object identifier for specific entity permission assignment", requiredMode = RequiredMode.REQUIRED)
  private Long authObjectId;

  @Size(min = 1)
  @CollectionValueNotNull // Fix:: invalid enumeration value is null element
  @Schema(description = "Authorization permissions defining operational access rights for the script")
  private List<ScriptPermission> permissions;

}




