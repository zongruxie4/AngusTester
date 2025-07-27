package cloud.xcan.angus.core.tester.interfaces.services.facade.dto.auth;

import cloud.xcan.angus.api.commonlink.services.ServicesPermission;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ServicesAuthReplaceDto {

  @NotEmpty
  //@CollectionValueNotNull // Fix:: invalid enumeration value is null element
  @Schema(description = "Authorization permissions defining operational access rights for the service", requiredMode = RequiredMode.REQUIRED)
  private Set<ServicesPermission> permissions;

}




