package cloud.xcan.angus.core.tester.interfaces.report.facade.dto.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.tester.domain.report.auth.ReportPermission;
import cloud.xcan.angus.validator.CollectionValueNotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@Setter
@Getter
@Accessors(chain = true)
@Schema(description = "Data transfer object for adding report authorization permissions")
public class ReportAuthAddDto {

  @NotNull
  @Schema(description = "Type of authorization object (USER, GROUP, DEPARTMENT)", example = "USER", requiredMode = RequiredMode.REQUIRED)
  private AuthObjectType authObjectType;

  @NotNull
  @Schema(description = "ID of the authorization object (user ID, group ID, or department ID)", requiredMode = RequiredMode.REQUIRED)
  private Long authObjectId;

  @Size(min = 1)
  @CollectionValueNotNull
  @Schema(description = "Set of authorization permissions that define what operations the object can perform on the report", requiredMode = RequiredMode.REQUIRED)
  private Set<ReportPermission> permissions;

}
