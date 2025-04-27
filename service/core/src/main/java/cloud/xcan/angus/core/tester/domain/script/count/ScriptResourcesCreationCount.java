package cloud.xcan.angus.core.tester.domain.script.count;

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
public class ScriptResourcesCreationCount {

  @Schema(description = "Total number of script")
  private long allScript;

  @Schema(description = "The number of new script added in the last one week")
  private long scriptByLastWeek;

  @Schema(description = "The number of new script added in the last one month")
  private long scriptByLastMonth;

  @Schema(description = "The number of script group by type")
  private LinkedHashMap<ScriptType, Integer> scriptByScriptType = new LinkedHashMap<>();

  @Schema(description = "The number of script group by plugin")
  private LinkedHashMap<String, Integer> scriptByPluginName = new LinkedHashMap<>();

}
