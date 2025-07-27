package cloud.xcan.angus.core.tester.interfaces.services.facade.dto.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ServicesAuthFindDto extends PageQuery {

  //@NotNull -> Transferring values in filters
  @Schema(description = "Service identifier for authorization query filtering", requiredMode = RequiredMode.REQUIRED)
  private Long serviceId;

  @NotNull
  @Schema(description = "Authorization object type for entity category filtering", example = "USER", requiredMode = RequiredMode.REQUIRED)
  private AuthObjectType authObjectType;

  @Schema(description = "Authorization object identifier for specific entity filtering")
  private Long authObjectId;

  @Schema(description = "Authorization creation date for temporal filtering")
  private Date createdDate;

}



