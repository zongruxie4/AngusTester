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

  @Schema(description="Modify monitor id. Create a new monitor when the value is null")
  private Long id;

  @Schema(description = "Monitor scenario id, required when creating a new monitor")
  private Long scenarioId;

  @NotEmpty
  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Monitor name", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Length(max = MAX_DESC_LENGTH)
  @Schema(description = "Monitor description")
  private String description;

  @Valid
  @NotNull
  @Schema(description = "Monitor time setting", requiredMode = RequiredMode.REQUIRED)
  private MonitorTimeSetting timeSetting;

  @Schema(description = "Monitor server setting")
  private List<Server> serverSetting;

  @Valid
  @NotNull
  @Schema(description = "Monitor notice setting", requiredMode = RequiredMode.REQUIRED)
  private NoticeSetting noticeSetting;

}
