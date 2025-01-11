package cloud.xcan.sdf.core.angustester.interfaces.script.facade.vo.auth;

import cloud.xcan.sdf.api.commonlink.script.ScriptPermission;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ScriptAuthCurrentVo {

  private boolean scriptAuthFlag;

  private Set<ScriptPermission> permissions;
}
