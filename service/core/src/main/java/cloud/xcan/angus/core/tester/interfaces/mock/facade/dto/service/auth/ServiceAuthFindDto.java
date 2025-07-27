package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.auth;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;


@Setter
@Getter
@Accessors(chain = true)
public class ServiceAuthFindDto extends PageQuery {

  @NotNull
  @Schema(description = "Authorization object type for access control filtering", example = "USER", requiredMode = RequiredMode.REQUIRED)
  private AuthObjectType authObjectType;

  @Schema(description = "Authorization object identifier for precise filtering")
  private Long authObjectId;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Authorization creation date for temporal filtering")
  private Date createdDate;

}



