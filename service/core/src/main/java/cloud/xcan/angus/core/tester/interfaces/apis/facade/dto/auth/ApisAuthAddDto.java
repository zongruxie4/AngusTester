package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.auth;

import cloud.xcan.angus.api.commonlink.apis.ApiPermission;
import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.validator.CollectionValueNotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class ApisAuthAddDto {

  @NotNull
  @Schema(example = "USER", requiredMode = RequiredMode.REQUIRED)
  private AuthObjectType authObjectType;

  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long authObjectId;

  @Size(min = 1)
  @CollectionValueNotNull // Fix:: invalid enumeration value is null element
  @Schema(description = "Authorization permissions(Operation permission), Default view")
  private Set<ApiPermission> permissions;

}




