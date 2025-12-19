package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
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

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}
