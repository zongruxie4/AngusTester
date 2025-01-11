package cloud.xcan.sdf.api.angustester.script.vo.targets;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.model.script.ScriptSource;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
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
