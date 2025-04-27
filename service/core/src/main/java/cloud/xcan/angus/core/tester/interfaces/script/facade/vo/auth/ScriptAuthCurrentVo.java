package cloud.xcan.angus.core.tester.interfaces.script.facade.vo.auth;

import cloud.xcan.angus.api.commonlink.script.ScriptPermission;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ScriptAuthCurrentVo {

  private boolean scriptAuth;

  private Set<ScriptPermission> permissions;
}
