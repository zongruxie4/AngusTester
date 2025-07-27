package cloud.xcan.angus.core.tester.interfaces.services.facade.dto.auth;

import cloud.xcan.angus.api.commonlink.services.ServicesPermission;
import cloud.xcan.angus.api.enums.AuthObjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ServicesAddAuthDto {

  @NotNull
  @Schema(description = "Authorization object type defining the entity category for permission assignment", example = "USER", requiredMode = RequiredMode.REQUIRED)
  private AuthObjectType authObjectType;

  @NotNull
  @Schema(description = "Authorization object identifier for specific entity permission assignment", requiredMode = RequiredMode.REQUIRED)
  private Long authObjectId;

  //@CollectionValueNotNull // Fix:: invalid enumeration value is null element
  @Schema(description = "Authorization permissions defining operational access rights for the service")
  private Set<ServicesPermission> permissions;

}
