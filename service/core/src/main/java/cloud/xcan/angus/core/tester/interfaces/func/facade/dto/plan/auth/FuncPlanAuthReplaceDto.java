package cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.auth;

import cloud.xcan.angus.core.tester.domain.func.plan.auth.FuncPlanPermission;
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
public class FuncPlanAuthReplaceDto {

  @NotNull
  @Size(min = 1)
  @CollectionValueNotNull
  @Schema(description = "Authorization permissions(Operation permission)", requiredMode = RequiredMode.REQUIRED)
  private Set<FuncPlanPermission> permissions;

}




