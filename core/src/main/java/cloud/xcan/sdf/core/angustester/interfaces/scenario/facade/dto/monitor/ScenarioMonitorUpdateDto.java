package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.monitor;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_DESC_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import cloud.xcan.sdf.core.angustester.domain.setting.MonitorTimeSetting;
import cloud.xcan.sdf.core.angustester.domain.setting.NoticeSetting;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ScenarioMonitorUpdateDto {

  @NotNull
  @ApiModelProperty(value = "Modify monitor id", required = true)
  private Long id;

  //@ApiModelProperty(value = "Monitor scenario id, required when creating a new monitor")
  //private Long scenarioId;

  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "Monitor name")
  private String name;

  @Length(max = DEFAULT_DESC_LENGTH)
  @ApiModelProperty(value = "Monitor description")
  private String description;

  @Valid
  @ApiModelProperty(value = "Monitor time setting")
  private MonitorTimeSetting timeSetting;

  @ApiModelProperty(value = "Monitor server setting")
  private List<Server> serverSetting;

  @Valid
  @ApiModelProperty(value = "Monitor notice setting")
  private NoticeSetting noticeSetting;

}
