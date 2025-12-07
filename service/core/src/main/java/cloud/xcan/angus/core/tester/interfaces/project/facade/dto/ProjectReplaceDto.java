package cloud.xcan.angus.core.tester.interfaces.project.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X2;

import cloud.xcan.angus.api.commonlink.tag.OrgTargetType;
import cloud.xcan.angus.core.tester.domain.project.ProjectType;
import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class ProjectReplaceDto {

  @Schema(description = "Project identifier for replacement operation; null indicates creation of a new project")
  private Long id;

  @Schema(description = "Project type defining the development methodology and workflow", example = "AGILE")
  private ProjectType type;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Project name for identification and management, must be unique within the organization", example = "DemoProject", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Schema(description = "Project avatar URL for visual identification")
  @Length(max = MAX_URL_LENGTH_X2)
  private String avatar;

  @NotNull
  @Schema(description = "Project owner identifier for responsibility assignment", requiredMode = RequiredMode.REQUIRED)
  private Long ownerId;

  @NotNull
  @Schema(description = "Project start date for timeline planning and milestone tracking", example = "2023-06-10 00:00:00", requiredMode = RequiredMode.REQUIRED)
  private LocalDateTime startDate;

  @NotNull
  @Schema(description = "Project deadline date for delivery planning and resource allocation", example = "2029-06-20 00:00:00", requiredMode = RequiredMode.REQUIRED)
  private LocalDateTime deadlineDate;

  @Schema(description = "Project description for detailed information and requirements")
  @EditorContentLength
  private String description;

  @Schema(description = "Project version for version control and tracking", example = "V1.0")
  @Length(max = 50)
  private String version;

  @Schema(description = "Flag to import example data for rapid project setup; effective only when creating a new project")
  private boolean importExample = false;

  @NotNull
  @Schema(description = "Project member mapping by organization target type and member identifiers", requiredMode = RequiredMode.REQUIRED)
  private LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> memberTypeIds;

}




