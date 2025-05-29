package cloud.xcan.angus.core.tester.application.query.module.impl;

import static cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource.AngusTesterModule;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.spec.utils.ObjectUtils.distinctByKey;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.util.Collections.emptyMap;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.manager.SettingTenantQuotaManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.jpa.criteria.CriteriaUtils;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.module.ModuleQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.domain.module.Module;
import cloud.xcan.angus.core.tester.domain.module.ModuleRepo;
import cloud.xcan.angus.remote.message.http.ResourceExisted;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.remote.search.SearchCriteria;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.JpaSort;

@Biz
public class ModuleQueryImpl implements ModuleQuery {

  @Resource
  private ModuleRepo moduleRepo;

  @Resource
  private SettingTenantQuotaManager settingTenantQuotaManager;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Override
  public Module detail(Long id) {
    return new BizTemplate<Module>() {

      @Override
      protected Module process() {
        Module module = checkAndFind(id);
        module.setHasEditPermission(projectQuery.hasEditPermission(module.getProjectId()));
        return module;
      }
    }.execute();
  }

  @Override
  public List<Module> find(GenericSpecification<Module> spec) {
    return new BizTemplate<List<Module>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected List<Module> process() {
        List<Module> modules = moduleRepo.findAll(spec,
            PageRequest.of(0, 10000, JpaSort.by(Order.asc("id")))).getContent();
        List<Module> allModules = findAndAllParent(modules);
        setEditPermission(spec.getCriteria(), allModules);
        return allModules;
      }
    }.execute();
  }

  @Override
  public List<Module> checkAndFind(Long projectId, Collection<Long> moduleIds) {
    if (isNull(projectId) || isEmpty(moduleIds)) {
      return null;
    }
    List<Module> modules = moduleRepo.findByProjectIdAndIdIn(projectId, moduleIds);
    assertResourceNotFound(isNotEmpty(modules), moduleIds.iterator().next(), "Module");
    if (moduleIds.size() != modules.size()) {
      for (Module module : modules) {
        assertResourceNotFound(moduleIds.contains(module.getId()), module.getId(), "Module");
      }
    }
    return modules;
  }

  @Override
  public Module checkAndFind(Long id) {
    return moduleRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Module"));
  }

  @Override
  public List<Module> checkAndFind(Collection<Long> ids) {
    if (isEmpty(ids)) {
      return null;
    }
    List<Module> modules = moduleRepo.findAllById(ids);
    assertResourceNotFound(isNotEmpty(modules), ids.iterator().next(), "Module");
    if (ids.size() != modules.size()) {
      for (Module module : modules) {
        assertResourceNotFound(ids.contains(module.getId()), module.getId(), "Module");
      }
    }
    return modules;
  }

  @Override
  public Set<Long> findModuleAndSubIds(Long projectId, Collection<Long> moduleIds) {
    return findModuleAndSub(projectId, moduleIds).stream().map(Module::getId)
        .collect(Collectors.toSet());
  }

  @Override
  public List<Module> findModuleAndSub(Long projectId, Collection<Long> moduleIds) {
    if (isEmpty(moduleIds)) {
      return Collections.emptyList();
    }
    // Find current modules
    List<Module> modules = moduleRepo.findByProjectIdAndIdIn(projectId, moduleIds);
    if (isEmpty(modules)) {
      return Collections.emptyList();
    }
    List<Module> allModuleAndSub = new ArrayList<>(modules);
    do {
      // Find sub modules
      modules = moduleRepo.findByProjectIdAndPidIn(projectId, moduleIds);
      if (isNotEmpty(modules)) {
        allModuleAndSub.addAll(modules);
        moduleIds = modules.stream().map(Module::getId).collect(Collectors.toSet());
      }
    } while (isNotEmpty(modules));
    return allModuleAndSub;
  }

  @Override
  public List<Module> findAndAllParent(Collection<Module> modules) {
    List<Module> allModules = new ArrayList<>();
    if (isNotEmpty(modules)) {
      allModules.addAll(modules);
      Set<Long> parentModuleIds = modules.stream().filter(Module::hasParent)
          .map(Module::getPid).collect(Collectors.toSet());
      do {
        List<Module> parentModules = moduleRepo.findAllById(parentModuleIds);
        if (isNotEmpty(parentModules)) {
          allModules.addAll(parentModules);
          parentModuleIds = parentModules.stream().filter(Module::hasParent)
              .map(Module::getPid).collect(Collectors.toSet());
        }
      } while (!parentModuleIds.isEmpty());
      return allModules.stream().filter(distinctByKey(Module::getId))
          .collect(Collectors.toList());
    }
    return allModules;
  }

  @Override
  public List<Module> findSub(Long projectId, Collection<Long> moduleIds) {
    if (isEmpty(moduleIds)) {
      return Collections.emptyList();
    }
    List<Module> allModuleAndSub = new ArrayList<>();
    List<Module> projectModuleSubs;
    do {
      // Find sub modules
      projectModuleSubs = moduleRepo.findByProjectIdAndPidIn(projectId, moduleIds);
      if (isNotEmpty(projectModuleSubs)) {
        allModuleAndSub.addAll(projectModuleSubs);
        moduleIds = projectModuleSubs.stream().map(Module::getId).collect(Collectors.toList());
      }
    } while (isNotEmpty(projectModuleSubs));
    return allModuleAndSub;
  }

  @Override
  public List<Long> findSubIds(Long projectId, Collection<Long> moduleIds) {
    return findSub(projectId, moduleIds).stream().map(Module::getId).collect(Collectors.toList());
  }

  @Override
  public Map<String, Module> checkAndFindByName(Long projectId, Set<String> names) {
    if (isEmpty(names)) {
      return emptyMap();
    }
    List<Module> casesDb = moduleRepo.findByProjectIdAndNameIn(projectId, names);
    if (isEmpty(casesDb)) {
      throw ResourceNotFound.of(names.iterator().next(), "Module");
    }
    if (names.size() != casesDb.size()) {
      Collection<String> namesDb = casesDb.stream()
          .map(Module::getName).collect(Collectors.toSet());
      names.removeAll(namesDb);
      throw ResourceNotFound.of(names.iterator().next(), "Module");
    }
    return casesDb.stream().collect(Collectors.toMap(Module::getName, x -> x));
  }

  @Override
  public void checkAddNameExist(Long projectId, Set<String> names) {
    List<Module> moduleDb = moduleRepo.findByProjectIdAndNameIn(projectId, names);
    if (isNotEmpty(moduleDb)) {
      throw ResourceExisted.of(moduleDb.get(0).getName(), "Module");
    }
  }

  @Override
  public void checkUpdateNameExists(Long projectId, Collection<Module> modules) {
    List<Module> modulesDb = moduleRepo.findByProjectIdAndNameIn(
        projectId, modules.stream().map(Module::getName).collect(Collectors.toList()));
    if (isEmpty(modulesDb)) {
      return;
    }
    for (Module module : modules) {
      for (Module moduleDb : modulesDb) {
        if (module.getName().equalsIgnoreCase(moduleDb.getName())
            && !module.getId().equals(moduleDb.getId())) {
          throw ResourceExisted.of(moduleDb.getName(), "Module");
        }
      }
    }
  }

  @Override
  public void checkUpdatePidNotCircular(Long projectId, List<Module> modules) {
    for (Module module : modules) {
      if (nonNull(module.getPid())) {
        List<Long> subIds = findSubIds(projectId, List.of(module.getId()));
        ProtocolAssert.assertTrue(!subIds.contains(module.getPid()),
            "Module pid is circular reference");
      }
    }
  }

  @Override
  public void checkQuota(int incr) {
    long count = moduleRepo.count();
    settingTenantQuotaManager.checkTenantQuota(AngusTesterModule, null, incr + count);
  }

  @Override
  public void setEditPermission(Set<SearchCriteria> criteria, List<Module> allModules) {
    if (isEmpty(allModules)) {
      return;
    }
    String projectId = CriteriaUtils.findFirstValue(criteria, "projectId");
    boolean hasEditPermission = projectQuery.hasEditPermission(Long.parseLong(projectId));
    allModules.forEach(module -> module.setHasEditPermission(hasEditPermission));
  }
}
