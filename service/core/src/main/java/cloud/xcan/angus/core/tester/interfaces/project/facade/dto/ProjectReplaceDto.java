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

  @Schema(description="Modify project id. Create a new project when the value is null")
  private Long id;

  @Schema(description = "Create project type, default `AGILE`", example = "AGILE")
  private ProjectType type;

  @NotBlank
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Project name, must be unique", example = "DemoProject", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Schema(description = "Project avatar")
  @Length(max = MAX_URL_LENGTH_X2)
  private String avatar;

  @NotNull
  @Schema(description = "Owner id", requiredMode = RequiredMode.REQUIRED)
  private Long ownerId;

  @NotNull
  @Schema(description = "Project start date, Determine the start times of the research and testing activities"
      + " to ensure completion within the project cycle", example = "2023-06-10 00:00:00", requiredMode = RequiredMode.REQUIRED)
  private LocalDateTime startDate;

  @NotNull
  @Schema(description = "Project deadline date, Determine the end times of the research and testing activities"
      + " to ensure completion within the project cycle", example = "2029-06-20 00:00:00", requiredMode = RequiredMode.REQUIRED)
  private LocalDateTime deadlineDate;

  @Schema(description = "Project description")
  @EditorContentLength
  private String description;

  @Schema(description = "Whether to import project example data, effective only when creating a new project")
  private boolean importExample = false;

  @NotNull
  @Schema(description = "Project member type and ids", requiredMode = RequiredMode.REQUIRED)
  private LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> memberTypeIds;

}




