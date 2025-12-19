package cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor;

import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorStatus;
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
public class ScenarioMonitorFindDto extends PageQuery {

  @NotNull
  @Schema(description = "Project identifier for monitoring configuration filtering", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Monitoring configuration identifier for precise query filtering")
  private Long id;

  @Schema(description = "Monitoring configuration name for fuzzy search")
  private String name;

  @Schema(description = "Scenario identifier for monitoring configuration association")
  private Long scenarioId;

  @Schema(description = "Monitoring execution status for filtering")
  private ScenarioMonitorStatus status;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}
