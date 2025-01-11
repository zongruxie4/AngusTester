package cloud.xcan.sdf.core.angustester.application.cmd.script;

import cloud.xcan.sdf.core.angustester.domain.script.auth.ScriptAuth;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;

public interface ScriptAuthCmd {

  IdKey<Long, Object> add(ScriptAuth scriptAuth);

  void replace(ScriptAuth scriptAuth);

  void delete(Long scriptId);

  void enabled(Long projectId, Boolean enabledFlag);

  void addCreatorAuth(Collection<Long> creatorIds, Long scriptId);

  void moveCreatorAuth(Collection<Long> creatorIds, Long scriptId);

  void deleteByScriptIdIn(Collection<Long> scriptIds);
}




