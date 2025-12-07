package cloud.xcan.angus.core.tester.application.cmd.project.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.PROJECT;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_AFTER_HOURS;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_BEFORE_HOURS;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_PROJECT_FILE;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.ProjectConverter.getSafeExampleDataTypes;
import static cloud.xcan.angus.core.tester.application.converter.ProjectConverter.toTaskTrash;
import static cloud.xcan.angus.core.tester.infra.util.AngusTesterUtils.parseSample;
import static cloud.xcan.angus.core.utils.CoreUtils.copyProperties;
import static cloud.xcan.angus.core.utils.CoreUtils.copyPropertiesIgnoreNull;
import static cloud.xcan.angus.core.utils.PrincipalContextUtils.getOptTenantId;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getDefaultLanguage;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNull;
import static cloud.xcan.angus.spec.utils.ObjectUtils.nullSafe;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.tag.OrgTargetType;
import cloud.xcan.angus.api.commonlink.user.User;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.data.DatasetCmd;
import cloud.xcan.angus.core.tester.application.cmd.data.VariableCmd;
import cloud.xcan.angus.core.tester.application.cmd.exec.ExecTestCmd;
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskCmd;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockServiceCmd;
import cloud.xcan.angus.core.tester.application.cmd.project.ModuleCmd;
import cloud.xcan.angus.core.tester.application.cmd.project.ProjectCmd;
import cloud.xcan.angus.core.tester.application.cmd.project.ProjectMemberCmd;
import cloud.xcan.angus.core.tester.application.cmd.project.ProjectTrashCmd;
import cloud.xcan.angus.core.tester.application.cmd.project.TagCmd;
import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioCmd;
import cloud.xcan.angus.core.tester.application.cmd.script.ScriptCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesCmd;
import cloud.xcan.angus.core.tester.application.cmd.test.FuncCaseCmd;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.domain.ExampleDataType;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.project.ProjectRepo;
import cloud.xcan.angus.core.tester.domain.project.ProjectType;
import cloud.xcan.angus.core.tester.infra.util.BIDUtils;
import cloud.xcan.angus.core.tester.infra.util.BIDUtils.BIDKey;
import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.experimental.IdKey;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command implementation for project management operations.
 * <p>
 * Provides comprehensive CRUD operations for projects including creation, modification,
 * deletion, import/export, and member management.
 * <p>
 * Implements business logic validation, permission checks, activity logging,
 * and transaction management for all project operations.
 * <p>
 * Supports example data import, member management, trash functionality,
 * and comprehensive activity tracking.
 */
@Slf4j
@Biz
public class ProjectCmdImpl extends CommCmd<Project, Long> implements ProjectCmd {

  @Resource
  private ProjectRepo projectRepo;
  @Resource
  private ProjectQuery projectQuery;
  @Resource
  private ProjectCmd projectCmd;
  @Resource
  private ProjectMemberCmd projectMemberCmd;
  @Resource
  private ProjectTrashCmd trashProjectCmd;
  @Resource
  private TagCmd tagCmd;
  @Resource
  private ModuleCmd moduleCmd;
  @Resource
  private TaskCmd taskCmd;
  @Resource
  private FuncCaseCmd funcCaseCmd;
  @Resource
  private ServicesCmd servicesCmd;
  @Resource
  private ScenarioCmd scenarioCmd;
  @Resource
  private ScriptCmd scriptCmd;
  @Resource
  private MockServiceCmd mockServiceCmd;
  @Resource
  private VariableCmd variableCmd;
  @Resource
  private DatasetCmd datasetCmd;
  @Resource
  private ExecTestCmd execTestCmd;
  @Resource
  private UserManager userManager;
  @Resource
  private ActivityCmd activityCmd;
  @Resource
  private CommonQuery commonQuery;

  /**
   * Adds a new project to the system.
   * <p>
   * Performs comprehensive validation including quota limits, project name uniqueness,
   * and member existence checks.
   * <p>
   * Optionally imports example data based on project configuration and logs creation activity
   * for audit trail purposes.
   * <p>
   * Ensures proper project setup with owner and member associations.
   */
  @Override
  public IdKey<Long, Object> add(Project project) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Check if project creation exceeds quota limits
        projectQuery.checkQuota(1);

        // Validate project name is unique within the tenant
        projectQuery.checkAddNameExists(project.getName());

        // Ensure all specified members exist in the organization
        commonQuery.checkOrgExists(project.getMemberTypeIds());
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Create project with owner and member setup
        IdKey<Long, Object> idKey = projectCmd.add0(project);

        // Import example data if requested
        if (project.isImportExample()) {
          ProjectType type = nullSafe(project.getType(), ProjectType.AGILE);
          Set<ExampleDataType> finalDataTypes = getSafeExampleDataTypes(type, null);
          try {
            importOtherExample(finalDataTypes, project);
          } catch (Exception e) {
            log.error("Import example data exception", e);
          }
        }

        // Log project creation activity for audit
        activityCmd.add(toActivity(PROJECT, project, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  /**
   * Adds a new project with internal setup (owner and member management).
   * <p>
   * Creates the project entity and establishes owner-member relationships.
   * <p>
   * Ensures the project owner is automatically added to the member list.
   * <p>
   * This is an internal method used by the public add method.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add0(Project project) {
    // Save project to database and get generated ID
    project.setId(BIDUtils.getId(BIDKey.projectId));
    IdKey<Long, Object> idKey = insert(project);

    // Ensure project owner is included in member list
    addOwnerToMembers0(project, project.getOwnerId());

    // Create project member associations
    projectMemberCmd.add0(idKey.getId(), project.getMemberTypeIds());
    return idKey;
  }

  /**
   * Updates an existing project in the system.
   * <p>
   * Validates project existence, user permissions, name uniqueness, and member existence
   * before updating project details.
   * <p>
   * Updates project information, manages member changes, and logs modification activity
   * for audit trail purposes.
   * <p>
   * Only project owners and administrators can modify projects.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(Project project) {
    new BizTemplate<Void>() {
      Project projectDb;

      @Override
      protected void checkParams() {
        // Ensure the project exists in database
        projectDb = projectQuery.checkAndFind(project.getId());

        // Check user permission to modify the project (owner or admin only)
        projectQuery.checkModifyPermission(projectDb);

        // Validate project name is unique if changed
        if (isNotEmpty(project.getName())) {
          projectQuery.checkUpdateNameExists(project.getId(), project.getName());
        }

        // Ensure all specified members exist in the organization
        commonQuery.checkOrgExists(project.getMemberTypeIds());
      }

      @Override
      protected Void process() {
        // Update project members if specified
        if (isNotEmpty(project.getMemberTypeIds())) {
          addOwnerToMembers(project, projectDb);
          projectMemberCmd.replace0(projectDb.getId(), project.getMemberTypeIds());
        }

        // Update project information in database
        projectRepo.save(copyPropertiesIgnoreNull(project, projectDb));

        // Log project update activity for audit
        activityCmd.add(toActivity(PROJECT, project, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  /**
   * Replaces (adds or updates) a project in the system.
   * <p>
   * Validates project existence, user permissions, name uniqueness, and member existence
   * before replacing project details.
   * <p>
   * Creates a new project if ID is null, otherwise updates existing project.
   * <p>
   * Updates project information, manages member changes, and logs modification activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(Project project) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Project projectDb;

      @Override
      protected void checkParams() {
        if (nonNull(project.getId())) {
          // Ensure the project exists in database
          projectDb = projectQuery.checkAndFind(project.getId());

          // Check user permission to modify the project (owner or admin only)
          projectQuery.checkModifyPermission(projectDb);

          // Validate project name is unique if changed
          if (isNotEmpty(project.getName())) {
            projectQuery.checkUpdateNameExists(project.getId(), project.getName());
          }

          // Ensure all specified members exist in the organization
          commonQuery.checkOrgExists(project.getMemberTypeIds());
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        // Create new project if ID is null
        if (isNull(project.getId())) {
          return add(project);
        }

        // Update project members
        addOwnerToMembers(project, projectDb);
        projectMemberCmd.replace0(projectDb.getId(), project.getMemberTypeIds());

        // Update project information in database
        projectRepo.save(copyProperties(project, projectDb));

        // Log project update activity for audit
        activityCmd.add(toActivity(PROJECT, project, ActivityType.UPDATED));
        return new IdKey<Long, Object>().setId(projectDb.getId()).setKey(projectDb.getName());
      }
    }.execute();
  }

  /**
   * Imports an example project with sample data from template files.
   * <p>
   * Parses example project template from resources and creates a new project
   * with the specified name and type.
   * <p>
   * Imports comprehensive example data including tags, modules, tasks, cases,
   * services, scenarios, scripts, variables, datasets, mocks, and executions.
   * <p>
   * Logs creation activity for audit trail purposes.
   */
  @Override
  public IdKey<Long, Object> importExample(@Nullable String name, ProjectType type,
      @Nullable Set<ExampleDataType> dataTypes) {
    return new BizTemplate<IdKey<Long, Object>>() {

      @Override
      protected IdKey<Long, Object> process() {
        // Parse project template from resource file
        URL resourceUrl = this.getClass().getResource("/samples/project/"
            + getDefaultLanguage().getValue() + "/" + SAMPLE_PROJECT_FILE);
        Project project = parseSample(Objects.requireNonNull(resourceUrl),
            new TypeReference<Project>() {
            }, SAMPLE_PROJECT_FILE);

        // Create project with example data
        IdKey<Long, Object> idKey = projectCmd.importProjectExample(name, type, project);

        // Import additional example data based on project type
        Set<ExampleDataType> finalDataTypes = getSafeExampleDataTypes(type, dataTypes);
        importOtherExample(finalDataTypes, project);

        // Log project creation activity for audit
        activityCmd.add(toActivity(PROJECT, project, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  /**
   * Imports a project example with internal setup and member management.
   * <p>
   * Creates a new project with example data, sets up project timeline,
   * and establishes member associations.
   * <p>
   * Automatically adds all tenant users as project members.
   * <p>
   * This is an internal method used by the public importExample method.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public @NotNull IdKey<Long, Object> importProjectExample(@Nullable String name,
      ProjectType type, Project project) {
    // Create example project with generated ID and timeline
    project.setId(BIDUtils.getId(BIDKey.projectId));
    project.setType(type)
        .setName(isNotEmpty(name) ? name : project.getName())
        .setOwnerId(getUserId())
        .setStartDate(LocalDateTime.now().minusHours(SAMPLE_BEFORE_HOURS))
        .setDeadlineDate(LocalDateTime.now().minusHours(SAMPLE_AFTER_HOURS));
    // Set default version if not provided
    if (isEmpty(project.getVersion())) {
      project.setVersion("V1.0");
    }
    IdKey<Long, Object> idKey = insert(project);

    // Get all valid users from the tenant for member setup
    List<User> users = userManager.findValidByTenantId(getOptTenantId());
    Assert.assertNotEmpty(users, "Tenant users are empty");

    // Create member type mapping with all tenant users
    LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> memberTypeIds = new LinkedHashMap<>();
    memberTypeIds.put(OrgTargetType.USER,
        new LinkedHashSet<>(users.stream().map(User::getId).collect(Collectors.toSet())));
    project.setMemberTypeIds(memberTypeIds);

    // Create project member associations
    projectMemberCmd.add0(idKey.getId(), project.getMemberTypeIds());
    return idKey;
  }

  /**
   * Deletes a project from the system (soft delete).
   * <p>
   * Validates project existence and user permissions before marking the project as deleted.
   * <p>
   * Moves project to trash for potential recovery and logs deletion activity
   * for audit trail purposes.
   * <p>
   * Only project owners and administrators can delete projects.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      Project projectDb;

      @Override
      protected void checkParams() {
        // Check if project exists in database
        projectDb = projectRepo.findById(id).orElse(null);

        if (nonNull(projectDb)) {
          // Check user permission to delete the project (owner or admin only)
          projectQuery.checkModifyPermission(projectDb);
        }
      }

      @Override
      protected Void process() {
        if (isNull(projectDb)) {
          return null;
        }

        // Mark project as deleted (soft delete)
        projectDb.setDeleted(true).setDeletedBy(getUserId())
            .setDeletedDate(LocalDateTime.now());
        projectRepo.save(projectDb);

        // TODO: Implement logical deletion of associated resources

        // Move project to trash for potential recovery
        trashProjectCmd.add0(singletonList(toTaskTrash(projectDb)));

        // Log project deletion activity for audit
        activityCmd.add(toActivity(PROJECT, projectDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  /**
   * Imports example data for a project based on specified data types.
   * <p>
   * Imports various types of example data including tags, modules, tasks, cases,
   * services, scenarios, scripts, variables, datasets, mocks, and executions.
   * <p>
   * Each data type is imported conditionally based on the finalDataTypes set.
   * <p>
   * This is an internal helper method used for project initialization.
   */
  public void importOtherExample(Set<ExampleDataType> finalDataTypes, Project project) {
    // Import tags if requested
    if (finalDataTypes.contains(ExampleDataType.TAG)) {
      tagCmd.importExample(project.getId());
    }

    // Import modules if requested
    if (finalDataTypes.contains(ExampleDataType.MODULE)) {
      moduleCmd.importExample(project.getId());
    }

    // Import tasks if requested
    if (finalDataTypes.contains(ExampleDataType.TASK)) {
      taskCmd.importExample(project.getId());
    }

    // Import functional test cases if requested
    if (finalDataTypes.contains(ExampleDataType.FUNC)) {
      funcCaseCmd.importExample(project.getId());
    }

    // Import services if requested
    if (finalDataTypes.contains(ExampleDataType.SERVICES)) {
      servicesCmd.importExample(project.getId());
    }

    // Import scenarios if requested
    if (finalDataTypes.contains(ExampleDataType.SCENARIO)) {
      scenarioCmd.importExample(project.getId());
    }

    // Import scripts if requested
    if (finalDataTypes.contains(ExampleDataType.SCRIPT)) {
      scriptCmd.importExample(project.getId());
    }

    // Import variables if requested
    if (finalDataTypes.contains(ExampleDataType.VARIABLE)) {
      variableCmd.importExample(project.getId());
    }

    // Import datasets if requested
    if (finalDataTypes.contains(ExampleDataType.DATASET)) {
      datasetCmd.importExample(project.getId());
    }

    // Import mock services if requested
    if (finalDataTypes.contains(ExampleDataType.MOCK)) {
      mockServiceCmd.importExample(project.getId());
    }

    // Import test executions if requested
    if (finalDataTypes.contains(ExampleDataType.EXECUTION)) {
      execTestCmd.importExample(project.getId());
    }
  }

  /**
   * Adds the project owner to the member list (internal helper).
   * <p>
   * Ensures the project owner is included in the USER member set.
   * <p>
   * Uses the new owner ID if specified, otherwise uses the existing owner ID.
   */
  private static void addOwnerToMembers(Project project, Project projectDb) {
    Long ownerId = isNull(project.getOwnerId()) ? projectDb.getOwnerId() : project.getOwnerId();
    addOwnerToMembers0(project, ownerId);
  }

  /**
   * Adds the project owner to the member list (internal helper).
   * <p>
   * Ensures the project owner is included in the USER member set.
   * <p>
   * Creates the USER member set if it doesn't exist.
   */
  private static void addOwnerToMembers0(Project project, Long ownerId) {
    LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> members = project.getMemberTypeIds();
    if (members.containsKey(OrgTargetType.USER)) {
      // Add owner to existing USER member set
      members.get(OrgTargetType.USER).add(ownerId);
    } else {
      // Create new USER member set with owner
      LinkedHashSet<Long> memberSet = new LinkedHashSet<>();
      memberSet.add(ownerId);
      project.getMemberTypeIds().put(OrgTargetType.USER, memberSet);
    }
  }

  /**
   * Gets the repository for project entities.
   * <p>
   * Used by the base command class for generic operations.
   * <p>
   * Provides access to the underlying project data store.
   */
  @Override
  protected BaseRepository<Project, Long> getRepository() {
    return this.projectRepo;
  }
}




