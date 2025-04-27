package cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.core.tester.domain.scenario.monitor.ScenarioMonitorStatus;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;


@Setter
@Getter
@Accessors(chain = true)
public class ScenarioMonitorFindDto extends PageQuery {

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description="Monitor id")
  private Long id;

  @Schema(description = "Monitor name")
  private String name;

  @Schema(description = "Monitor scenario id")
  private Long scenarioId;

  private ScenarioMonitorStatus status;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long lastModifiedBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime lastModifiedDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}
