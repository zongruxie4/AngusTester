package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.monitor;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.core.angustester.domain.scenario.monitor.ScenarioMonitorStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ScenarioMonitorSearchDto extends PageQuery {

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @ApiModelProperty("Monitor id")
  private Long id;

  @ApiModelProperty(value = "Monitor name")
  private String name;

  @ApiModelProperty(value = "Monitor scenario id")
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
