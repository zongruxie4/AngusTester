package cloud.xcan.angus.core.tester.interfaces.report.facade.dto.auth;

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
@Schema(description = "Data transfer object for replacing report authorization permissions")
public class ReportAuthReplaceDto {

  @NotNull
  @Size(min = 1)
  @CollectionValueNotNull
  @Schema(description = "Complete set of new authorization permissions that will replace all existing permissions", requiredMode = RequiredMode.REQUIRED)
  private Set<ReportPermission> permissions;

}




