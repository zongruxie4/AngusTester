package cloud.xcan.angus.core.tester.interfaces.script.facade.dto.auth;

import cloud.xcan.angus.api.commonlink.script.ScriptPermission;
import cloud.xcan.angus.validator.CollectionValueNotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ScriptAuthReplaceDto {

  @NotEmpty
  @Size(min = 1)
  @CollectionValueNotNull // Fix:: invalid enumeration value is null element
  @Schema(description = "Authorization permissions(Operation permission)", requiredMode = RequiredMode.REQUIRED)
  private Set<ScriptPermission> permissions;

}




