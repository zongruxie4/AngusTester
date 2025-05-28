package cloud.xcan.angus.core.tester.interfaces.func.facade.dto.plan.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class FuncPlanAuthFindDto extends PageQuery {

  //@NotNull -> Transferring values in filters
  @Schema(description = "Plan id", requiredMode = RequiredMode.REQUIRED)
  private Long planId;

  @NotNull
  @Schema(example = "USER", requiredMode = RequiredMode.REQUIRED)
  private AuthObjectType authObjectType;

  @Schema(description = "Authorization object id")
  private Long authObjectId;

  private Date createdDate;

}
