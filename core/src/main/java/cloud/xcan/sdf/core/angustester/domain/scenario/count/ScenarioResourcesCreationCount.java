package cloud.xcan.sdf.core.angustester.domain.scenario.count;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ScenarioResourcesCreationCount {

  @ApiModelProperty(value = "Total number of scenario")
  private long allSce;

  @ApiModelProperty(value = "The number of new scenario added in the last one week")
  private long sceByLastWeek;

  @ApiModelProperty(value = "The number of new scenario added in the last one month")
  private long sceByLastMonth;

  @ApiModelProperty(value = "The number of scenario group by type")
  private LinkedHashMap<ScriptType, Integer> sceByScriptType = new LinkedHashMap<>();

  @ApiModelProperty(value = "The number of scenario group by plugin")
  private LinkedHashMap<String, Integer> sceByPluginName = new LinkedHashMap<>();
}
