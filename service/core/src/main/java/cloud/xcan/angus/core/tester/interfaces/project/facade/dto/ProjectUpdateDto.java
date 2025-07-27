package cloud.xcan.angus.core.tester.interfaces.project.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X2;

import cloud.xcan.angus.api.commonlink.tag.OrgTargetType;
import cloud.xcan.angus.validator.EditorContentLength;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
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
public class ProjectUpdateDto {

  @NotNull
  @Schema(description = "Project identifier for update operation", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Project name for identification and management, must be unique within the organization", example = "DemoProject")
  private String name;

  @Schema(description = "Project avatar URL for visual identification")
  @Length(max = MAX_URL_LENGTH_X2)
  private String avatar;

  @Schema(description = "Project owner identifier for responsibility assignment", example = "1")
  private Long ownerId;

  @Schema(description = "Project start date for timeline planning and milestone tracking", example = "2023-06-10 00:00:00")
  private LocalDateTime startDate;

  @Schema(description = "Project deadline date for delivery planning and resource allocation", example = "2029-06-20 00:00:00")
  private LocalDateTime deadlineDate;

  @Schema(description = "Project description for detailed information and requirements")
  @EditorContentLength
  private String description;

  @Schema(description = "Project member mapping by organization target type and member identifiers")
  private LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> memberTypeIds;
}




