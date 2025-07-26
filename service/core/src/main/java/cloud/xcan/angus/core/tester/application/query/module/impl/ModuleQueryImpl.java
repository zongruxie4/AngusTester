package cloud.xcan.angus.core.tester.application.query.module.impl;

import static cloud.xcan.angus.api.commonlink.setting.quota.QuotaResource.AngusTesterModule;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.spec.utils.ObjectUtils.distinctByKey;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static java.util.Collections.emptyList;
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
import cloud.xcan.angus.core.tester.domain.module.ModuleSearchRepo;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.JpaSort;

/**
 * Implementation of ModuleQuery for managing module operations and data retrieval.
 * <p>
 * This class provides comprehensive functionality for querying and managing modules,
 * which represent organizational units within projects for test case and task management.
 * It handles module retrieval, validation, hierarchy management, and permission checking.
 * <p>
 * Key features include:
 * <ul>
 *   <li>Module detail retrieval with permission validation</li>
 *   <li>Hierarchical module structure management and traversal</li>
 *   <li>Parent and child module relationship handling</li>
 *   <li>Module name uniqueness validation and conflict detection</li>
 *   <li>Circular reference prevention in module hierarchies</li>
 *   <li>Quota management and resource validation</li>
 *   <li>Edit permission checking and assignment</li>
 *   <li>Full-text search support for module content</li>
 * </ul>
 * <p>
 * The implementation uses the BizTemplate pattern for consistent business logic handling
 * and proper error management across all operations.
 */
@Biz
public class ModuleQueryImpl implements ModuleQuery {

  @Resource
  private ModuleRepo moduleRepo;
  @Resource
  private ModuleSearchRepo moduleSearchRepo;
  @Resource
  private SettingTenantQuotaManager settingTenantQuotaManager;
  @Resource
  private ProjectQuery projectQuery;
  @Resource
  private ProjectMemberQuery projectMemberQuery;

  /**
   * Retrieves detailed information for a specific module with permission validation.
   * <p>
   * Fetches complete module details and validates edit permissions for the current user
   * based on project membership and role assignments.
   *
   * @param id the module ID to retrieve details for
   * @return Module object with complete details and permission flags
   * @throws ResourceNotFound if the module is not found
   */
  @Override
  public Module detail(Long id) {
    return new BizTemplate<Module>() {
      @Override
      protected Module process() {
        Module module = checkAndFind(id);
        // Set edit permission based on project-level permissions
        module.setHasEditPermission(projectQuery.hasEditPermission(module.getProjectId()));
        return module;
      }
    }.execute();
  }

  /**
   * Retrieves a list of modules with comprehensive filtering and permission handling.
   * <p>
   * Supports both regular search and full-text search with project member validation.
   * Retrieves all parent modules for complete hierarchy context and sets edit permissions.
   * <p>
   * Note: The method currently returns an empty list due to incomplete implementation.
   *
   * @param spec the search specification with criteria and filters
   * @param fullTextSearch whether to use full-text search capabilities
   * @param match array of field names to include in full-text search
   * @return List of Module objects with complete hierarchy and permission information
   */
  @Override
  public List<Module> find(GenericSpecification<Module> spec, boolean fullTextSearch,
      String[] match) {
    return new BizTemplate<List<Module>>() {
      @Override
      protected void checkParams() {
        // Validate project member permissions for the search criteria
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected List<Module> process() {
        // Create pagination request for comprehensive module retrieval
        PageRequest pageable = PageRequest.of(0, 10000, JpaSort.by(Order.asc("id")));
        
        // Execute search based on whether full-text search is enabled
        Page<Module> page = fullTextSearch
            ? moduleSearchRepo.find(spec.getCriteria(), pageable, Module.class, match)
            : moduleRepo.findAll(spec, pageable);

        if (page.hasContent()) {
          // Retrieve all parent modules for complete hierarchy context
          List<Module> allModules = findAndAllParent(page.getContent());
          // Set edit permissions for all modules based on project permissions
          setEditPermission(spec.getCriteria(), allModules);
        }
        return emptyList();
      }
    }.execute();
  }

  /**
   * Validates that modules exist within a specific project and retrieves them.
   * <p>
   * Performs existence validation for modules within the project scope and
   * throws ResourceNotFound if any module is not found or belongs to a different project.
   *
   * @param projectId the project ID for scope validation
   * @param moduleIds collection of module IDs to validate and retrieve
   * @return List of Module objects if all modules exist within the project
   * @throws ResourceNotFound if any module is not found or belongs to a different project
   */
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

  /**
   * Validates that a module exists and retrieves it.
   * <p>
   * Performs existence validation and throws ResourceNotFound if the module
   * is not found in the system.
   *
   * @param id the module ID to validate and retrieve
   * @return Module object if found
   * @throws ResourceNotFound if the module is not found
   */
  @Override
  public Module checkAndFind(Long id) {
    return moduleRepo.findById(id).orElseThrow(() -> ResourceNotFound.of(id, "Module"));
  }

  /**
   * Validates that multiple modules exist and retrieves them.
   * <p>
   * Performs batch existence validation and throws ResourceNotFound if any module
   * is not found in the system.
   *
   * @param ids collection of module IDs to validate and retrieve
   * @return List of Module objects for all found modules
   * @throws ResourceNotFound if any module is not found
   */
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

  /**
   * Finds module IDs and all their sub-module IDs within a project.
   * <p>
   * Retrieves the specified modules and all their descendants in the hierarchy,
   * returning only the IDs for efficient processing.
   *
   * @param projectId the project ID for scope validation
   * @param moduleIds collection of module IDs to find sub-modules for
   * @return Set of module IDs including the specified modules and all their sub-modules
   */
  @Override
  public Set<Long> findModuleAndSubIds(Long projectId, Collection<Long> moduleIds) {
    return findModuleAndSub(projectId, moduleIds).stream().map(Module::getId)
        .collect(Collectors.toSet());
  }

  /**
   * Finds modules and all their sub-modules within a project.
   * <p>
   * Retrieves the specified modules and all their descendants in the hierarchy,
   * traversing the complete module tree structure recursively.
   *
   * @param projectId the project ID for scope validation
   * @param moduleIds collection of module IDs to find sub-modules for
   * @return List of Module objects including the specified modules and all their sub-modules
   */
  @Override
  public List<Module> findModuleAndSub(Long projectId, Collection<Long> moduleIds) {
    if (isEmpty(moduleIds)) {
      return Collections.emptyList();
    }
    // Retrieve the initial modules from the specified IDs
    List<Module> modules = moduleRepo.findByProjectIdAndIdIn(projectId, moduleIds);
    if (isEmpty(modules)) {
      return Collections.emptyList();
    }
    
    List<Module> allModuleAndSub = new ArrayList<>(modules);
    do {
      // Find all sub-modules of the current modules
      modules = moduleRepo.findByProjectIdAndPidIn(projectId, moduleIds);
      if (isNotEmpty(modules)) {
        allModuleAndSub.addAll(modules);
        // Update module IDs for the next iteration to find deeper sub-modules
        moduleIds = modules.stream().map(Module::getId).collect(Collectors.toSet());
      }
    } while (isNotEmpty(modules));
    return allModuleAndSub;
  }

  /**
   * Finds modules and all their parent modules in the hierarchy.
   * <p>
   * Retrieves the specified modules and all their ancestors in the hierarchy,
   * traversing up the module tree structure to build complete context.
   *
   * @param modules collection of modules to find parents for
   * @return List of Module objects including the specified modules and all their parents
   */
  @Override
  public List<Module> findAndAllParent(Collection<Module> modules) {
    List<Module> allModules = new ArrayList<>();
    if (isNotEmpty(modules)) {
      allModules.addAll(modules);
      // Collect parent module IDs from modules that have parents
      Set<Long> parentModuleIds = modules.stream().filter(Module::hasParent)
          .map(Module::getPid).collect(Collectors.toSet());
      
      do {
        // Retrieve parent modules for the current parent IDs
        List<Module> parentModules = moduleRepo.findAllById(parentModuleIds);
        if (isNotEmpty(parentModules)) {
          allModules.addAll(parentModules);
          // Collect parent IDs for the next iteration to find higher-level parents
          parentModuleIds = parentModules.stream().filter(Module::hasParent)
              .map(Module::getPid).collect(Collectors.toSet());
        }
      } while (!parentModuleIds.isEmpty());
      
      // Remove duplicate modules based on ID to ensure uniqueness
      return allModules.stream().filter(distinctByKey(Module::getId))
          .collect(Collectors.toList());
    }
    return allModules;
  }

  /**
   * Finds all sub-modules of the specified modules within a project.
   * <p>
   * Retrieves all descendants of the specified modules in the hierarchy,
   * traversing down the module tree structure recursively.
   *
   * @param projectId the project ID for scope validation
   * @param moduleIds collection of module IDs to find sub-modules for
   * @return List of Module objects representing all sub-modules of the specified modules
   */
  @Override
  public List<Module> findSub(Long projectId, Collection<Long> moduleIds) {
    if (isEmpty(moduleIds)) {
      return Collections.emptyList();
    }
    List<Module> allModuleAndSub = new ArrayList<>();
    List<Module> projectModuleSubs;
    do {
      // Find sub-modules of the current modules
      projectModuleSubs = moduleRepo.findByProjectIdAndPidIn(projectId, moduleIds);
      if (isNotEmpty(projectModuleSubs)) {
        allModuleAndSub.addAll(projectModuleSubs);
        // Update module IDs for the next iteration to find deeper sub-modules
        moduleIds = projectModuleSubs.stream().map(Module::getId).collect(Collectors.toList());
      }
    } while (isNotEmpty(projectModuleSubs));
    return allModuleAndSub;
  }

  /**
   * Finds all sub-module IDs of the specified modules within a project.
   * <p>
   * Retrieves all descendant IDs of the specified modules in the hierarchy,
   * returning only the IDs for efficient processing.
   *
   * @param projectId the project ID for scope validation
   * @param moduleIds collection of module IDs to find sub-module IDs for
   * @return List of module IDs representing all sub-modules of the specified modules
   */
  @Override
  public List<Long> findSubIds(Long projectId, Collection<Long> moduleIds) {
    return findSub(projectId, moduleIds).stream().map(Module::getId).collect(Collectors.toList());
  }

  /**
   * Validates that modules exist by name within a project and retrieves them.
   * <p>
   * Performs existence validation for modules by name within the project scope and
   * throws ResourceNotFound if any module name is not found.
   *
   * @param projectId the project ID for scope validation
   * @param names set of module names to validate and retrieve
   * @return Map of module names to Module objects if all modules exist
   * @throws ResourceNotFound if any module name is not found
   */
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

  /**
   * Validates that module names do not already exist within a project.
   * <p>
   * Checks for name uniqueness within the specified project to prevent
   * duplicate module names during creation operations.
   *
   * @param projectId the project ID for scope validation
   * @param names set of module names to validate
   * @throws ResourceExisted if any module name already exists
   */
  @Override
  public void checkAddNameExist(Long projectId, Set<String> names) {
    List<Module> moduleDb = moduleRepo.findByProjectIdAndNameIn(projectId, names);
    if (isNotEmpty(moduleDb)) {
      throw ResourceExisted.of(moduleDb.get(0).getName(), "Module");
    }
  }

  /**
   * Validates that module names do not conflict during update operations.
   * <p>
   * Checks for name uniqueness within the specified project, excluding
   * the current modules being updated to allow name preservation.
   *
   * @param projectId the project ID for scope validation
   * @param modules collection of modules to validate names for
   * @throws ResourceExisted if any module name conflicts with existing modules
   */
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

  /**
   * Validates that module parent-child relationships do not create circular references.
   * <p>
   * Ensures that setting a module's parent ID does not create a circular dependency
   * where a module would become its own ancestor in the hierarchy.
   *
   * @param projectId the project ID for scope validation
   * @param modules list of modules to validate parent-child relationships for
   * @throws ProtocolAssert.ProtocolException if a circular reference is detected
   */
  @Override
  public void checkUpdatePidNotCircular(Long projectId, List<Module> modules) {
    for (Module module : modules) {
      if (nonNull(module.getPid())) {
        // Find all sub-modules of the current module
        List<Long> subIds = findSubIds(projectId, List.of(module.getId()));
        // Ensure the parent ID is not among the sub-modules to prevent circular reference
        ProtocolAssert.assertTrue(!subIds.contains(module.getPid()),
            "Module pid is circular reference");
      }
    }
  }

  /**
   * Validates that the module quota has not been exceeded.
   * <p>
   * Checks the current module count against the tenant quota and validates
   * that adding the specified number of modules would not exceed the limit.
   *
   * @param incr the number of modules to be added
   * @throws QuotaExceeded if the quota limit would be exceeded
   */
  @Override
  public void checkQuota(int incr) {
    long count = moduleRepo.count();
    settingTenantQuotaManager.checkTenantQuota(AngusTesterModule, null, incr + count);
  }

  /**
   * Sets edit permissions for modules based on project-level permissions.
   * <p>
   * Assigns edit permission flags to all modules based on the current user's
   * edit permissions for the project, ensuring consistent permission handling.
   *
   * @param criteria search criteria containing project information
   * @param allModules list of modules to set edit permissions for
   */
  @Override
  public void setEditPermission(Set<SearchCriteria> criteria, List<Module> allModules) {
    if (isEmpty(allModules)) {
      return;
    }
    // Extract project ID from search criteria for permission checking
    String projectId = CriteriaUtils.findFirstValue(criteria, "projectId");
    boolean hasEditPermission = projectQuery.hasEditPermission(Long.parseLong(projectId));
    // Set edit permission for all modules based on project-level permissions
    allModules.forEach(module -> module.setHasEditPermission(hasEditPermission));
  }
}
