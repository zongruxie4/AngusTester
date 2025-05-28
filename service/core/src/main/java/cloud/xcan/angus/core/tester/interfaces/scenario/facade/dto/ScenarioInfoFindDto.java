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

  private Long id;

  @Length(max = MAX_NAME_LENGTH_X2)
  private String name;

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH)
  private String plugin;

  private ScriptType scriptType;

  @Schema(description = "Required when app administrators query all scenarios")
  private Boolean admin;

  @Schema(description = "Required when the user query has the one permission scenario")
  private ScenarioPermission hasPermission;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime lastModifiedDate;

  private Long favouriteBy;

  private Long followBy;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}



