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
import cloud.xcan.angus.core.tester.application.cmd.func.FuncCaseCmd;
import cloud.xcan.angus.core.tester.application.cmd.mock.MockServiceCmd;
import cloud.xcan.angus.core.tester.application.cmd.module.ModuleCmd;
import cloud.xcan.angus.core.tester.application.cmd.project.ProjectCmd;
import cloud.xcan.angus.core.tester.application.cmd.project.ProjectMemberCmd;
import cloud.xcan.angus.core.tester.application.cmd.project.ProjectTrashCmd;
import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioCmd;
import cloud.xcan.angus.core.tester.application.cmd.script.ScriptCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesCmd;
import cloud.xcan.angus.core.tester.application.cmd.tag.TagCmd;
import cloud.xcan.angus.core.tester.application.cmd.task.TaskCmd;
import cloud.xcan.angus.core.tester.application.query.common.CommonQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.domain.ExampleDataType;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.project.ProjectRepo;
import cloud.xcan.angus.core.tester.domain.project.ProjectType;
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
 * Command implementation for project management.
 * <p>
 * Provides methods for adding, updating, replacing, importing, deleting, and managing project members and example data.
 * <p>
 * Ensures permission checks, activity logging, and batch operations with transaction management.
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
   * Add a new project.
   * <p>
   * Checks quota, name uniqueness, and member existence before inserting.
   * <p>
   * Optionally imports example data and logs creation activity.
   */
  @Override
  public IdKey<Long, Object> add(Project project) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Check the project quota
        projectQuery.checkQuota(1);

        // Check the name replication
        projectQuery.checkAddNameExists(project.getName());

        // Check the members exists
        commonQuery.checkOrgExists(project.getMemberTypeIds());
      }

      @Override
      protected IdKey<Long, Object> process() {
        IdKey<Long, Object> idKey = projectCmd.add0(project);

        // Create other example data
        if (project.isImportExample()) {
          ProjectType type = nullSafe(project.getType(), ProjectType.AGILE);
          Set<ExampleDataType> finalDataTypes = getSafeExampleDataTypes(type, null);
          try {
            importOtherExample(finalDataTypes, project);
          } catch (Exception e) {
            log.error("Import example data exception", e);
          }
        }

        // Add created project activity
        activityCmd.add(toActivity(PROJECT, project, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  /**
   * Add a new project (internal, with owner/member setup).
   * <p>
   * Inserts project, adds owner to members, and saves members.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add0(Project project) {
    // Save project
    IdKey<Long, Object> idKey = insert(project);

    // Add owner to members
    addOwnerToMembers0(project, project.getOwnerId());
    // Save members
    projectMemberCmd.add0(idKey.getId(), project.getMemberTypeIds());
    return idKey;
  }

  /**
   * Update an existing project.
   * <p>
   * Checks existence, permission, name uniqueness, and member existence before updating.
   * <p>
   * Updates members, saves project, and logs update activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void update(Project project) {
    new BizTemplate<Void>() {
      Project projectDb;

      @Override
      protected void checkParams() {
        // Check the project exists
        projectDb = projectQuery.checkAndFind(project.getId());

        // Check the only allow owner and admin to modify
        projectQuery.checkModifyPermission(projectDb);

        // Check the name replication
        if (isNotEmpty(project.getName())) {
          projectQuery.checkUpdateNameExists(project.getId(), project.getName());
        }

        // Check the members exists
        commonQuery.checkOrgExists(project.getMemberTypeIds());
      }

      @Override
      protected Void process() {
        // Update members
        if (isNotEmpty(project.getMemberTypeIds())) {
          addOwnerToMembers(project, projectDb);
          projectMemberCmd.replace0(projectDb.getId(), project.getMemberTypeIds());
        }

        // Save project
        projectRepo.save(copyPropertiesIgnoreNull(project, projectDb));

        // Add created project activity
        activityCmd.add(toActivity(PROJECT, project, ActivityType.UPDATED));
        return null;
      }
    }.execute();
  }

  /**
   * Replace (add or update) a project.
   * <p>
   * Checks existence, permission, name uniqueness, and member existence before replacing.
   * <p>
   * Replaces members, saves project, and logs update activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> replace(Project project) {
    return new BizTemplate<IdKey<Long, Object>>() {
      Project projectDb;

      @Override
      protected void checkParams() {
        if (nonNull(project.getId())) {
          // Check the project exists
          projectDb = projectQuery.checkAndFind(project.getId());

          // Check the only allow owner and admin to modify
          projectQuery.checkModifyPermission(projectDb);

          // Check the name replication
          if (isNotEmpty(project.getName())) {
            projectQuery.checkUpdateNameExists(project.getId(), project.getName());
          }

          // Check the members exists
          commonQuery.checkOrgExists(project.getMemberTypeIds());
        }
      }

      @Override
      protected IdKey<Long, Object> process() {
        if (isNull(project.getId())) {
          return add(project);
        }

        // Replace members
        addOwnerToMembers(project, projectDb);
        projectMemberCmd.replace0(projectDb.getId(), project.getMemberTypeIds());

        // Save project
        projectRepo.save(copyProperties(project, projectDb));

        // Add created project activity
        activityCmd.add(toActivity(PROJECT, project, ActivityType.UPDATED));
        return new IdKey<Long, Object>().setId(projectDb.getId()).setKey(projectDb.getName());
      }
    }.execute();
  }

  /**
   * Import an example project and its example data.
   * <p>
   * Parses example file, imports project and example data, and logs creation activity.
   */
  @Override
  public IdKey<Long, Object> importExample(@Nullable String name, ProjectType type,
      @Nullable Set<ExampleDataType> dataTypes) {
    return new BizTemplate<IdKey<Long, Object>>() {

      @Override
      protected IdKey<Long, Object> process() {
        // Parse project by example file
        URL resourceUrl = this.getClass().getResource("/samples/project/"
            + getDefaultLanguage().getValue() + "/" + SAMPLE_PROJECT_FILE);
        Project project = parseSample(Objects.requireNonNull(resourceUrl),
            new TypeReference<Project>() {
            }, SAMPLE_PROJECT_FILE);

        // Create project example data
        IdKey<Long, Object> idKey = projectCmd.importProjectExample(name, type, project);

        // Create other example data
        Set<ExampleDataType> finalDataTypes = getSafeExampleDataTypes(type, dataTypes);
        importOtherExample(finalDataTypes, project);

        // Add created project activity
        activityCmd.add(toActivity(PROJECT, project, ActivityType.CREATED));
        return idKey;
      }
    }.execute();
  }

  /**
   * Import a project example (internal, with member setup).
   * <p>
   * Sets up project, members, and inserts into repository.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public @NotNull IdKey<Long, Object> importProjectExample(@Nullable String name,
      ProjectType type, Project project) {
    // Create example project
    project.setId(uidGenerator.getUID()).setType(type)
        .setName(isNotEmpty(name) ? name : project.getName())
        .setOwnerId(getUserId())
        .setStartDate(LocalDateTime.now().minusHours(SAMPLE_BEFORE_HOURS))
        .setDeadlineDate(LocalDateTime.now().minusHours(SAMPLE_AFTER_HOURS));
    IdKey<Long, Object> idKey = insert(project);

    // Create project members
    List<User> users = userManager.findValidByTenantId(getOptTenantId());
    Assert.assertNotEmpty(users, "Tenant users are empty");
    LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> memberTypeIds = new LinkedHashMap<>();
    memberTypeIds.put(OrgTargetType.USER,
        new LinkedHashSet<>(users.stream().map(User::getId).collect(Collectors.toSet())));
    project.setMemberTypeIds(memberTypeIds);
    projectMemberCmd.add0(idKey.getId(), project.getMemberTypeIds());
    return idKey;
  }

  /**
   * Delete a project by ID (logic delete).
   * <p>
   * Checks existence and permission, marks project as deleted, and logs activity.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Long id) {
    new BizTemplate<Void>() {
      Project projectDb;

      @Override
      protected void checkParams() {
        // Check the project existed and authed
        projectDb = projectRepo.findById(id).orElse(null);

        if (nonNull(projectDb)) {
          // Check the only allow owner and admin to delete
          projectQuery.checkModifyPermission(projectDb);
        }
      }

      @Override
      protected Void process() {
        if (isNull(projectDb)) {
          return null;
        }

        // Logic delete
        // After the project is deleted, filter and exclude the associated sub project and task!
        projectDb.setDeleted(true).setDeletedBy(getUserId())
            .setDeletedDate(LocalDateTime.now());
        projectRepo.save(projectDb);

        // TODO Logical deletion of associated resources

        // Add project to ProjectTrash
        trashProjectCmd.add0(singletonList(toTaskTrash(projectDb)));

        // Add delete project activity
        activityCmd.add(toActivity(PROJECT, projectDb, ActivityType.DELETED));
        return null;
      }
    }.execute();
  }

  /**
   * Import example data for a project (internal helper).
   * <p>
   * Imports tags, modules, tasks, cases, services, scenarios, scripts, variables, datasets, mocks, and executions as needed.
   */
  public void importOtherExample(Set<ExampleDataType> finalDataTypes, Project project) {
    if (finalDataTypes.contains(ExampleDataType.TAG)) {
      tagCmd.importExample(project.getId());
    }
    if (finalDataTypes.contains(ExampleDataType.MODULE)) {
      moduleCmd.importExample(project.getId());
    }
    if (finalDataTypes.contains(ExampleDataType.TASK)) {
      taskCmd.importExample(project.getId());
    }
    if (finalDataTypes.contains(ExampleDataType.FUNC)) {
      funcCaseCmd.importExample(project.getId());
    }
    if (finalDataTypes.contains(ExampleDataType.SERVICES)) {
      servicesCmd.importExample(project.getId());
    }
    if (finalDataTypes.contains(ExampleDataType.SCENARIO)) {
      scenarioCmd.importExample(project.getId());
    }
    if (finalDataTypes.contains(ExampleDataType.SCRIPT)) {
      scriptCmd.importExample(project.getId());
    }
    if (finalDataTypes.contains(ExampleDataType.VARIABLE)) {
      variableCmd.importExample(project.getId());
    }
    if (finalDataTypes.contains(ExampleDataType.DATASET)) {
      datasetCmd.importExample(project.getId());
    }
    if (finalDataTypes.contains(ExampleDataType.MOCK)) {
      mockServiceCmd.importExample(project.getId());
    }
    /*if (finalDataTypes.contains(ExampleDataType.REPORT) && !isCommunityEdition()) {
      reportCmd.importExample(project.getId(), finalDataTypes);
    }*/
    if (finalDataTypes.contains(ExampleDataType.EXECUTION)) {
      execTestCmd.importExample(project.getId());
    }
  }

  /**
   * Add owner to project members (internal helper).
   * <p>
   * Ensures the owner is included in the USER member set.
   */
  private static void addOwnerToMembers(Project project, Project projectDb) {
    Long ownerId = isNull(project.getOwnerId()) ? projectDb.getOwnerId() : project.getOwnerId();
    addOwnerToMembers0(project, ownerId);
  }

  /**
   * Add owner to project members (internal helper).
   * <p>
   * Ensures the owner is included in the USER member set.
   */
  private static void addOwnerToMembers0(Project project, Long ownerId) {
    LinkedHashMap<OrgTargetType, LinkedHashSet<Long>> members = project.getMemberTypeIds();
    if (members.containsKey(OrgTargetType.USER)) {
      members.get(OrgTargetType.USER).add(ownerId);
    } else {
      LinkedHashSet<Long> memberSet = new LinkedHashSet<>();
      memberSet.add(ownerId);
      project.getMemberTypeIds().put(OrgTargetType.USER, memberSet);
    }
  }

  /**
   * Get the repository for projects.
   * <p>
   * Used by the base command class for generic operations.
   */
  @Override
  protected BaseRepository<Project, Long> getRepository() {
    return this.projectRepo;
  }
}




