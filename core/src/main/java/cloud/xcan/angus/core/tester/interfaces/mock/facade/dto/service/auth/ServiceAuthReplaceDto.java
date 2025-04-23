package cloud.xcan.angus.core.tester.interfaces.mock.facade.dto.service.auth;

import cloud.xcan.angus.core.tester.domain.mock.service.auth.MockServicePermission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class ServiceAuthReplaceDto {

  @NotEmpty
  @Size(min = 1)
  @Schema(description = "Authorization permissions(Operation permission), default view")
  private Set<MockServicePermission> permissions;

}




