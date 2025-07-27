package cloud.xcan.angus.core.tester.interfaces.project.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Setter
@Getter
public class ProjectFindDto extends PageQuery {

  @Schema(description = "Project identifier for precise query filtering")
  private Long id;

  @Schema(description = "Project name for fuzzy search or filtering")
  private String name;

  @Schema(description = "Flag indicating whether this is an admin query for all projects")
  private Boolean admin;

  @Schema(description = "Project owner identifier for ownership-based filtering")
  private Long ownerId;

  @Schema(description = "Creator identifier for creation-based filtering")
  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Project creation date for temporal filtering")
  private LocalDateTime createdDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}



