package cloud.xcan.angus.core.tester.application.cmd.script;

import cloud.xcan.angus.core.tester.domain.script.auth.ScriptAuth;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;

public interface ScriptAuthCmd {

  IdKey<Long, Object> add(ScriptAuth scriptAuth);

  void replace(ScriptAuth scriptAuth);

  void delete(Long scriptId);

  void enabled(Long projectId, Boolean enabled);

  void addCreatorAuth(Collection<Long> creatorIds, Long scriptId);

  void moveCreatorAuth(Collection<Long> creatorIds, Long scriptId);

  void deleteByScriptIdIn(Collection<Long> scriptIds);
}




