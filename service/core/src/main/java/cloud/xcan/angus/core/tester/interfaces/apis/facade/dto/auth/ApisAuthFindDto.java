package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ApisAuthFindDto extends PageQuery {

  @Schema(description = "Authorization identifier for precise query")
  private Long id;

  //@NotNull -> Transferring values in filters
  @Schema(description = "API identifier for authorization query scope definition", requiredMode = RequiredMode.REQUIRED)
  private Long apisId;

  @NotNull
  @Schema(example = "USER", description = "Authorization object type for permission target classification", requiredMode = RequiredMode.REQUIRED)
  private AuthObjectType authObjectType;

  @Schema(description = "Authorization object identifier for permission target filtering")
  private Long authObjectId;

}



