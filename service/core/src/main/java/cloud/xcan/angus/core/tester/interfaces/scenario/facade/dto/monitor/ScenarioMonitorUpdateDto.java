package cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.core.tester.domain.setting.MonitorTimeSetting;
import cloud.xcan.angus.core.tester.domain.setting.NoticeSetting;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;


@Setter
@Getter
@Accessors(chain = true)
public class ScenarioMonitorUpdateDto {

  @NotNull
  @Schema(description = "Monitoring configuration identifier for update operation", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  //@Schema(description = "Monitor scenario id, required when creating a new monitor")
  //private Long scenarioId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Monitoring configuration name for identification and management")
  private String name;

  @Length(max = MAX_DESC_LENGTH)
  @Schema(description = "Monitoring configuration description for detailed information")
  private String description;

  @Valid
  @Schema(description = "Monitoring schedule configuration for execution timing and frequency")
  private MonitorTimeSetting timeSetting;

  @Schema(description = "Target server configuration for monitoring execution")
  private List<Server> serverSetting;

  @Valid
  @Schema(description = "Alert notification configuration for issue reporting")
  private NoticeSetting noticeSetting;

}
