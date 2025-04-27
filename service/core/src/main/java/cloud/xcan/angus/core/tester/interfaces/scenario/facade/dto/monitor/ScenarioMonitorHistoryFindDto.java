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
public class ScenarioMonitorHistoryFindDto extends PageQuery {

  @Schema(description = "Monitor history id")
  private Long id;

  @NotNull
  @Schema(description = "Monitor id", requiredMode = RequiredMode.REQUIRED)
  private Long monitorId;

  private ScenarioMonitorStatus status;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}
