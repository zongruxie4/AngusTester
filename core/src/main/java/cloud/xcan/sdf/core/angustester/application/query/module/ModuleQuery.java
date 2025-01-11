package cloud.xcan.sdf.core.angustester.application.query.module;


import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.module.Module;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ModuleQuery {

  Module detail(Long id);

  List<Module> find(GenericSpecification<Module> spec);

  List<Module> checkAndFind(Long projectId, Collection<Long> moduleIds);

  Module checkAndFind(Long id);

  List<Module> checkAndFind(Collection<Long> ids);

  Set<Long> findModuleAndSubIds(Long projectId, Collection<Long> moduleIds);

  List<Module> findModuleAndSub(Long projectId, Collection<Long> moduleIds);

  List<Module> findAndAllParent(Collection<Module> modules);

  List<Module> findSub(Long projectId, Collection<Long> moduleIds);

  List<Long> findSubIds(Long projectId, Collection<Long> moduleIds);

  Map<String, Module> checkAndFindByName(Long projectId, Set<String> names);

  void checkAddNameExist(Long projectId, Set<String> names);

  void checkUpdateNameExists(Long projectId, Collection<Module> modules);

  void checkUpdatePidNotCircular(Long projectId, List<Module> modules);

  void checkQuota(int incr);

  void setEditPermissionFlag(Set<SearchCriteria> criteria, List<Module> allModules);
}
