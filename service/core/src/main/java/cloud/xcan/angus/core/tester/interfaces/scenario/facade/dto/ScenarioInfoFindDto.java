package cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.core.tester.domain.scenario.auth.ScenarioPermission;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter

public class ScenarioInfoFindDto extends PageQuery {

  @Schema(description = "Scenario identifier for precise query filtering")
  private Long id;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Scenario name for fuzzy search or filtering")
  private String name;

  @NotNull
  @Schema(description = "Project identifier for scenario filtering", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Scenario execution plugin name for filtering")
  private String plugin;

  @Schema(description = "Scenario script type for format-based filtering")
  private ScriptType scriptType;

  @Schema(description = "Flag indicating whether this is an admin query for all scenarios")
  private Boolean admin;

  @Schema(description = "Required permission level for scenario access filtering")
  private ScenarioPermission hasPermission;

  @Schema(description = "Creator identifier for creation-based filtering")
  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Scenario creation date for temporal filtering")
  private LocalDateTime createdDate;

  @Schema(description = "Last modifier identifier for modification-based filtering")
  private Long lastModifiedBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Scenario last modification date for temporal filtering")
  private LocalDateTime lastModifiedDate;

  @Schema(description = "User identifier who favorited the scenario for filtering")
  private Long favouriteBy;

  @Schema(description = "User identifier who followed the scenario for filtering")
  private Long followBy;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}



