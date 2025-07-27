package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ApisDesignFindDto extends PageQuery {

  @Schema(description = "API design identifier for precise query")
  private Long id;

  @NotNull
  @Schema(description = "Project identifier for API design query scope definition", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "API design name for fuzzy search and filtering")
  private String name;

  @Schema(description = "Release status flag for published design filtering")
  private Boolean release;

  @Schema(description = "Tenant identifier for multi-tenant filtering")
  private Long tenantId;

  @Schema(description = "Creator identifier for ownership filtering")
  private Long createdBy;

  @Schema(description = "Creation date for temporal filtering")
  private LocalDateTime createdDate;

  @Schema(description = "Last modifier identifier for update tracking")
  protected Long lastModifiedBy;

  @Schema(description = "Last modification date for temporal filtering")
  private LocalDateTime lastModifiedDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}
