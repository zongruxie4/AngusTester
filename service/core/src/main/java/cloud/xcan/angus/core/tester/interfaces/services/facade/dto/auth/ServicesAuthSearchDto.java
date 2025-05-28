package cloud.xcan.angus.core.tester.interfaces.services.facade.dto.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.remote.PageQuery;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ServicesAuthSearchDto extends PageQuery {

  @Schema
  private Long id;

  @JsonIgnore
  @Schema(hidden = true)
  private Long serviceId;

  @NotNull
  @Schema(example = "USER", description = "Authorization object type", requiredMode = RequiredMode.REQUIRED)
  private AuthObjectType authObjectType;

}



