package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServicePermission;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ServiceAddAuthDto {

  @NotNull
  @Schema(example = "USER", description = "Authorization object type", requiredMode = RequiredMode.REQUIRED)
  private AuthObjectType authObjectType;

  @NotNull
  @Schema(description = "Authorization object id", requiredMode = RequiredMode.REQUIRED)
  private Long authObjectId;

  @Size(min = 1)
  @Schema(description = "Authorization permissions(Operation permission), default view")
  private Set<MockServicePermission> permissions;
}
