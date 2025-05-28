package cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanPermission;
import cloud.xcan.angus.validator.CollectionValueNotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class FuncPlanAuthAddDto {

  @NotNull
  @Schema(example = "USER", requiredMode = RequiredMode.REQUIRED)
  private AuthObjectType authObjectType;

  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private Long authObjectId;

  //@NotNull
  @Size(min = 1)
  @CollectionValueNotNull // Fix:: invalid enumeration value is null element
  @Schema(description = "Authorization permissions(Operation permission)", requiredMode = RequiredMode.REQUIRED)
  private Set<FuncPlanPermission> permissions;

}
