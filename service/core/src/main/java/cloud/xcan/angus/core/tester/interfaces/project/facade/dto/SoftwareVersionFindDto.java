package cloud.xcan.angus.core.tester.interfaces.project.facade.dto;

import cloud.xcan.angus.core.tester.domain.project.version.SoftwareVersionStatus;
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
public class SoftwareVersionFindDto extends PageQuery {

  @NotNull
  @Schema(description = "Project identifier for version filtering and organization", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Version identifier for specific version lookup")
  private Long id;

  @Schema(description = "Version name for partial matching search")
  private String name;

  @Schema(description = "Version status for lifecycle filtering")
  private SoftwareVersionStatus status;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}
