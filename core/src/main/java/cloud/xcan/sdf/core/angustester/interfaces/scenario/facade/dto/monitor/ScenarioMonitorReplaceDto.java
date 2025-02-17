package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.monitor;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import cloud.xcan.sdf.core.angustester.domain.setting.MonitorTimeSetting;
import cloud.xcan.sdf.core.angustester.domain.setting.NoticeSetting;
import cloud.xcan.sdf.core.angustester.domain.setting.TimeSetting;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ScenarioMonitorReplaceDto {

  @ApiModelProperty("Modify monitor id. Create a new monitor when the value is null")
  private Long id;

  @ApiModelProperty(value = "Monitor scenario id, required when creating a new monitor")
  private Long scenarioId;

  @NotEmpty
  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "Monitor name", required = true)
  private String name;

  @Length(max = DEFAULT_DESC_LENGTH)
  @ApiModelProperty(value = "Monitor description")
  private String description;

  @Valid
  @NotNull
  @ApiModelProperty(value = "Monitor time setting", required = true)
  private MonitorTimeSetting timeSetting;

  @ApiModelProperty(value = "Monitor server setting")
  private List<Server> serverSetting;

  @Valid
  @NotNull
  @ApiModelProperty(value = "Monitor notice setting", required = true)
  private NoticeSetting noticeSetting;

}
