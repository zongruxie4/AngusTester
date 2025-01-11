package cloud.xcan.sdf.core.angustester.domain.kanban;

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
public class ResourcesScriptCount {

  @ApiModelProperty(value = "Total number of script")
  private long allScript;

  @ApiModelProperty(value = "The number of script group by type")
  private LinkedHashMap<ScriptType, Integer> scriptByScriptType = new LinkedHashMap<>();

  @ApiModelProperty(value = "The number of script group by plugin")
  private LinkedHashMap<String, Integer> scriptByPluginName = new LinkedHashMap<>();

}
