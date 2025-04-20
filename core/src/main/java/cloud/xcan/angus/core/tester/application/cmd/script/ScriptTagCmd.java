package cloud.xcan.angus.core.tester.application.cmd.script;

import java.util.Collection;

public interface ScriptTagCmd {

  void add(Long id, Collection<String> tags);

  void replace(Long id, Collection<String> tags);

  void clone(Long id, Long newId);

  void deleteByScriptIdIn(Collection<Long> ids);
}
