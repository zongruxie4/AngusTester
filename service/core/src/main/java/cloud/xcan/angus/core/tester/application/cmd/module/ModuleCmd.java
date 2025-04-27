package cloud.xcan.angus.core.tester.application.cmd.module;

import cloud.xcan.angus.core.tester.domain.module.Module;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;

public interface ModuleCmd {

  List<IdKey<Long, Object>> add(Long projectId, List<Module> modules);

  void update(List<Module> modules);

  List<IdKey<Long, Object>> replace(List<Module> modules);

  List<IdKey<Long, Object>> importExample(Long projectId);

  void delete(Collection<Long> ids);

}
