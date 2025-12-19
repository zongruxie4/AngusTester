package cloud.xcan.angus.core.tester.interfaces.test.facade.dto.plan.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class FuncPlanAuthFindDto extends PageQuery {

  //@NotNull -> Transferring values in filters
  @Schema(description = "Functional test plan identifier for authorization filtering", requiredMode = RequiredMode.REQUIRED)
  private Long planId;

  @NotNull
  @Schema(description = "Authorization object type for permission scope filtering", example = "USER", requiredMode = RequiredMode.REQUIRED)
  private AuthObjectType authObjectType;

  @Schema(description = "Authorization object identifier for permission filtering")
  private Long authObjectId;

}
