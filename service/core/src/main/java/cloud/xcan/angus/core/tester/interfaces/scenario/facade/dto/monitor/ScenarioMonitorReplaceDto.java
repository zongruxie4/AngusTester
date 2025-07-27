package cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.monitor;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.core.tester.domain.setting.MonitorTimeSetting;
import cloud.xcan.angus.core.tester.domain.setting.NoticeSetting;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;


@Setter
@Getter
@Accessors(chain = true)
public class ScenarioMonitorReplaceDto {

  @Schema(description = "Monitoring configuration identifier for replacement; null indicates creation of new configuration")
  private Long id;

  @Schema(description = "Scenario identifier for monitoring configuration association; required when creating new configuration")
  private Long scenarioId;

  @NotEmpty
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Monitoring configuration name for identification and management", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Length(max = MAX_DESC_LENGTH)
  @Schema(description = "Monitoring configuration description for detailed information")
  private String description;

  @Valid
  @NotNull
  @Schema(description = "Monitoring schedule configuration for execution timing and frequency", requiredMode = RequiredMode.REQUIRED)
  private MonitorTimeSetting timeSetting;

  @Schema(description = "Target server configuration for monitoring execution")
  private List<Server> serverSetting;

  @Valid
  @NotNull
  @Schema(description = "Alert notification configuration for issue reporting", requiredMode = RequiredMode.REQUIRED)
  private NoticeSetting noticeSetting;

}
