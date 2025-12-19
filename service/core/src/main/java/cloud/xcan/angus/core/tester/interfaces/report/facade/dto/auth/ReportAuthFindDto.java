package cloud.xcan.angus.core.tester.interfaces.report.facade.dto.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Data transfer object for finding and filtering report authorizations")
public class ReportAuthFindDto extends PageQuery {

  @Schema(description = "Report ID to filter authorizations for a specific report", requiredMode = RequiredMode.REQUIRED)
  private Long reportId;

  @NotNull
  @Schema(description = "Type of authorization object to filter by (USER, GROUP, DEPARTMENT)", example = "USER", requiredMode = RequiredMode.REQUIRED)
  private AuthObjectType authObjectType;

  @Schema(description = "Authorization object ID to filter by specific user, group, or department")
  private Long authObjectId;

}
