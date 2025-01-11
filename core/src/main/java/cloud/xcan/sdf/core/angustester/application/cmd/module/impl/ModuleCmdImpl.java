package cloud.xcan.sdf.core.angustester.application.cmd.module.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.MODULE;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.activityParams;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.CREATED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.DELETED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.UPDATED;
import static cloud.xcan.sdf.core.utils.CoreUtils.batchCopyPropertiesIgnoreNull;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.module.ModuleCmd;
import cloud.xcan.sdf.core.angustester.application.converter.ModuleConverter;
import cloud.xcan.sdf.core.angustester.application.query.module.ModuleQuery;
import cloud.xcan.sdf.core.angustester.application.query.project.ProjectQuery;
import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseRepo;
import cloud.xcan.sdf.core.angustester.domain.module.Module;
import cloud.xcan.sdf.core.angustester.domain.module.ModuleRepo;
import cloud.xcan.sdf.core.angustester.domain.project.Project;
import cloud.xcan.sdf.core.angustester.domain.task.TaskRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class ModuleCmdImpl extends CommCmd<Module, Long> implements ModuleCmd {

  @Resource
  private ModuleRepo moduleRepo;

  @Resource
  private ModuleQuery moduleQuery;

  @Resource
  private FuncCaseRepo funcCaseRepo;

  @Resource
  private TaskRepo taskRepo;

  @Resource
  private ProjectQuery projectQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> add(Long projectId, List<Module> modules) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {

      @Override
      protected void checkParams() {
        // Check the edit permission
        Project projectDb = projectQuery.checkAndFind(projectId);
        projectQuery.checkEditModulePermission(projectDb);

        // Check the pid module exists under same project
        moduleQuery.checkAndFind(projectId, modules.stream().filter(Module::hasParent)
            .map(Module::getPid).collect(Collectors.toSet()));

        // Check the names exists
        moduleQuery.checkAddNameExist(projectId, modules.stream().map(Module::getName)
            .collect(Collectors.toSet()));

        // @DoInFuture("Check maximum 5 levels")

        // Check the quota limit
        moduleQuery.checkQuota(modules.size());
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<IdKey<Long, Object>> idKeys = batchInsert(modules, "name");

        activityCmd.batchAdd(toActivities(MODULE, modules, CREATED, activityParams(modules)));
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(List<Module> modules) {
    new BizTemplate<Void>() {
      List<Module> modulesDb;

      @Override
      protected void checkParams() {
        // Check the modules exists
        modulesDb = moduleQuery.checkAndFind(
            modules.stream().map(Module::getId).collect(Collectors.toList()));

        Set<Long> projectIds = modulesDb.stream().map(Module::getProjectId)
            .collect(Collectors.toSet());
        ProtocolAssert.assertTrue(projectIds.size() == 1,
            "Only batch adding modules with one project is allowed");
        Long projectId = projectIds.iterator().next();

        // Check the edit permission
        Project projectDb = projectQuery.checkAndFind(projectId);
        projectQuery.checkEditModulePermission(projectDb);

        // Check the pid module exists under same project
        moduleQuery.checkAndFind(projectId, modules.stream().filter(Module::hasParent)
            .map(Module::getPid).collect(Collectors.toSet()));

        // Check the PID is not a circular reference
        moduleQuery.checkUpdatePidNotCircular(projectId, modules);

        // @DoInFuture("Check maximum 5 levels")

        // Check the names exists
        moduleQuery.checkUpdateNameExists(projectId, modules);
      }

      @Override
      protected Void process() {
        batchUpdate0(batchCopyPropertiesIgnoreNull(modules, modulesDb));

        activityCmd.batchAdd(toActivities(MODULE, modulesDb, UPDATED));
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public List<IdKey<Long, Object>> replace(List<Module> modules) {
    return new BizTemplate<List<IdKey<Long, Object>>>() {
      Long projectId;
      List<Module> updateModules;
      List<Module> updateModulesDb;

      @Override
      protected void checkParams() {
        // Check the one project is allowed
        Set<Long> projectIds = modules.stream().map(Module::getProjectId)
            .collect(Collectors.toSet());
        ProtocolAssert.assertTrue(projectIds.size() == 1,
            "Only batch adding modules with one project is allowed");
        projectId = projectIds.iterator().next();

        // Check the pid module exists under same project
        updateModules = modules.stream().filter(x -> nonNull(x.getId()))
            .collect(Collectors.toList());
        if (isNotEmpty(modules)) {
          // Check the edit permission
          Project projectDb = projectQuery.checkAndFind(projectId);
          projectQuery.checkEditModulePermission(projectDb);

          // Check the pid module exists under same project
          updateModulesDb = moduleQuery.checkAndFind(projectId, modules.stream()
              .filter(Module::hasParent).map(Module::getPid).collect(Collectors.toSet()));

          // Check the PID is not a circular reference
          moduleQuery.checkUpdatePidNotCircular(projectId, updateModules);

          // @DoInFuture("Check maximum 5 levels")

          // Check the names exists
          moduleQuery.checkUpdateNameExists(projectId, modules);
        }
      }

      @Override
      protected List<IdKey<Long, Object>> process() {
        List<IdKey<Long, Object>> idKeys = new ArrayList<>();
        List<Module> addModules = modules.stream().filter(x -> isNull(x.getId()))
            .collect(Collectors.toList());
        if (isNotEmpty(addModules)) {
          idKeys.addAll(add(projectId, addModules));
        }

        if (isNotEmpty(addModules)) {
          Map<Long, Module> updateModulesMap = modules.stream()
              .collect(Collectors.toMap(Module::getId, x -> x));
          moduleRepo.batchUpdate(updateModulesDb.stream()
              .map(x -> ModuleConverter.setReplaceInfo(x, updateModulesMap.get(x.getId())))
              .collect(Collectors.toList()));

          activityCmd.batchAdd(toActivities(MODULE, updateModulesDb, UPDATED));
        }
        return idKeys;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      List<Module> modulesDb;
      Long projectId;

      @Override
      protected void checkParams() {
        // check tag exist
        modulesDb = moduleQuery.checkAndFind(ids);

        // Check the one project is allowed
        Set<Long> projectIds = modulesDb.stream().map(Module::getProjectId)
            .collect(Collectors.toSet());
        ProtocolAssert.assertTrue(projectIds.size() == 1,
            "Only batch adding modules with one project is allowed");
        projectId = projectIds.iterator().next();

        // Check the edit permission
        Project projectDb = projectQuery.checkAndFind(projectId);
        projectQuery.checkEditModulePermission(projectDb);
      }

      @Override
      protected Void process() {
        Set<Long> moduleAndSubIds = moduleQuery.findModuleAndSubIds(projectId, ids);

        // Delete modules
        moduleRepo.deleteByIdIn(moduleAndSubIds);

        // Clear case module
        funcCaseRepo.updateModuleNull(moduleAndSubIds);

        // Clear task module
        taskRepo.updateModuleNull(moduleAndSubIds);

        activityCmd.batchAdd(toActivities(MODULE, modulesDb, DELETED));
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<Module, Long> getRepository() {
    return this.moduleRepo;
  }
}
