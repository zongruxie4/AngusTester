package cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.follow;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class ScenarioFollowFindDto extends PageQuery {

  @NotNull
  @Schema(description = "Project identifier for followed scenario filtering", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Scenario name for fuzzy search in followed scenarios")
  private String scenarioName;

}



