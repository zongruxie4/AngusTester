package cloud.xcan.angus.core.tester.interfaces.test.facade.dto.baseline;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class FuncBaselineFindDto extends PageQuery {

  @Schema(description = "Functional test baseline identifier for precise query")
  private Long id;

  @Schema(description = "Functional test baseline name for fuzzy search and filtering")
  private String name;

  @NotNull
  @Schema(description = "Project identifier for baseline scope filtering", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Functional test plan identifier for baseline filtering")
  private Long planId;

  @Schema(description = "Baseline establishment status for state-based filtering")
  private Boolean established;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}
