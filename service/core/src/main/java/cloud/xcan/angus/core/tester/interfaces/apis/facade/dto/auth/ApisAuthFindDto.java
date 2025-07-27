package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.auth;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;


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

  @Schema(description = "Authorization creation date for temporal filtering")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}



