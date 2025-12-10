package cloud.xcan.angus.core.tester.interfaces.project.facade.dto;

import cloud.xcan.angus.core.tester.domain.project.ProjectDataType;
import cloud.xcan.angus.core.tester.domain.project.ProjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO for project data import operation. Supports importing project data from ZIP or TAR archive
 * files.
 */
@Getter
@Setter
@Accessors(chain = true)
public class ProjectImportDto {

  @NotNull
  @Schema(description = "Project type (AGILE, WATERFALL, etc.)", requiredMode = RequiredMode.REQUIRED)
  private ProjectType projectType;

  @NotNull
  @Schema(description = "Project data types are divided into two categories: project data and project deliverables, with project data as the default type",
      requiredMode = RequiredMode.REQUIRED, defaultValue = "DATA")
  private ProjectDataType dataType = ProjectDataType.DATA;

  @NotBlank
  @Schema(description = "Project name for the imported project", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @NotNull
  @Schema(type = "string", format = "binary", description = "Project import file in ZIP (default) or TAR format containing project data files", requiredMode = RequiredMode.REQUIRED)
  private MultipartFile file;

}

