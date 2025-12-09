package cloud.xcan.angus.model.scenario;


import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.configuration.TestPlatform;
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

  private TestPlatform platform = TestPlatform.API;

  private ScriptType scriptType;

  private Long scriptId;

}
