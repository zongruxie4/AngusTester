package cloud.xcan.sdf.model.scenario;


import cloud.xcan.angus.model.script.configuration.ScriptType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ScenarioInfo {

  private Long id;

  private String name;

  private String plugin;

  private ScriptType scriptType;

  private Long scriptId;

}
