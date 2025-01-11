package cloud.xcan.sdf.core.angustester.application.cmd.module;

import cloud.xcan.sdf.core.angustester.domain.module.Module;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;

public interface ModuleCmd {

  List<IdKey<Long, Object>> add(Long projectId, List<Module> modules);

  void update(List<Module> modules);

  List<IdKey<Long, Object>> replace(List<Module> modules);

  void delete(Collection<Long> ids);

}
