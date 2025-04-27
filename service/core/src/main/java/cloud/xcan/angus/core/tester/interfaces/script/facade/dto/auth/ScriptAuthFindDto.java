package cloud.xcan.angus.core.tester.interfaces.script.facade.dto.auth;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;


@Setter
@Getter

public class ScriptAuthFindDto extends PageQuery {

  @Schema(description = "Authorization id")
  private Long id;

  //@NotNull -> Transferring values in filters
  @Schema(description = "Script id", requiredMode = RequiredMode.REQUIRED)
  private Long scriptId;

  @NotNull
  @Schema(example = "USER", requiredMode = RequiredMode.REQUIRED)
  private AuthObjectType authObjectType;

  @Schema(description = "Authorization object id")
  private Long authObjectId;

  @Schema(description = "Authorization date")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}
