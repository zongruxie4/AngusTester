package cloud.xcan.angus.core.tester.domain.scenario.count;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class ScenarioResourcesCreationCount {

  @Schema(description = "Total number of scenario")
  private long allSce;

  @Schema(description = "The number of new scenario added in the last one week")
  private long sceByLastWeek;

  @Schema(description = "The number of new scenario added in the last one month")
  private long sceByLastMonth;

  @Schema(description = "The number of scenario group by type")
  private LinkedHashMap<ScriptType, Integer> sceByScriptType = new LinkedHashMap<>();

  @Schema(description = "The number of scenario group by plugin")
  private LinkedHashMap<String, Integer> sceByPluginName = new LinkedHashMap<>();
}
