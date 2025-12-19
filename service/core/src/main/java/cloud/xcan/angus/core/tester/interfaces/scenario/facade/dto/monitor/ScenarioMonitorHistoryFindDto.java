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
public class ScenarioMonitorHistoryFindDto extends PageQuery {

  @Schema(description = "Monitoring history record identifier for precise query filtering")
  private Long id;

  @NotNull
  @Schema(description = "Monitoring configuration identifier for history query filtering", requiredMode = RequiredMode.REQUIRED)
  private Long monitorId;

  @Schema(description = "Monitoring execution status for history filtering")
  private ScenarioMonitorStatus status;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}
