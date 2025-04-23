package cloud.xcan.angus.core.tester.interfaces.project.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X2;

import cloud.xcan.angus.api.commonlink.tag.OrgTargetType;
import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class ProjectUpdateDto {

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Project name, must be unique", example = "DemoProject")
  private String name;

  @Schema(description = "Project avatar")
  @Length(max = MAX_URL_LENGTH_X2)
  private String avatar;

  @Schema(description = "Owner id", example = "1")
  private Long ownerId;

  @Schema(description = "Project start date, Determine the start times of the research and testing activities"
      + " to ensure completion within the project cycle.", example = "2023-06-10 00:00:00")
  private LocalDateTime startDate;

  @Schema(description = "Project deadline date, Determine the end times of the research and testing activities"
      + " to ensure completion within the project cycle.", example = "2029-06-20 00:00:00")
  private LocalDateTime deadlineDate;

  @Schema(description = "Project description")
  @EditorContentLength
  private String description;

  @Schema(description = "Project member type and ids")
  private LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> memberTypeIds;
}




