package cloud.xcan.angus.api.tester.script.vo.targets;

import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Setter
@Getter
@Accessors(chain = true)
public class ScriptTargetVo {

  private Long id;

  private Long scriptId;

  private ScriptType scriptType;

  private ScriptSource source;

  private Long scenarioId;

  private Long targetId;

  private Long parentTargetId;

  private Long tenantId;

}
