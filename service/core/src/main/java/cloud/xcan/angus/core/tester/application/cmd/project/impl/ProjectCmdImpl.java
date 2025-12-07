package cloud.xcan.angus.core.tester.application.cmd.project.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.PROJECT;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_AFTER_HOURS;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_BEFORE_HOURS;
import static cloud.xcan.angus.api.commonlink.TesterConstant.SAMPLE_PROJECT_FILE;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertNotEmpty;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
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

import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.api.commonlink.tag.OrgTargetType;
import cloud.xcan.angus.api.commonlink.user.User;
import cloud.xcan.angus.api.manager.UserManager;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
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
import cloud.xcan.angus.core.tester.application.query.data.DatasetQuery;
import cloud.xcan.angus.core.tester.application.query.data.VariableQuery;
import cloud.xcan.angus.core.tester.application.query.issue.TaskQuery;
import cloud.xcan.angus.core.tester.application.query.mock.MockServiceQuery;
import cloud.xcan.angus.core.tester.application.query.project.ModuleQuery;
import cloud.xcan.angus.core.tester.application.query.project.ProjectQuery;
import cloud.xcan.angus.core.tester.application.query.project.TagQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.application.query.test.FuncCaseQuery;
import cloud.xcan.angus.core.tester.domain.ExampleDataType;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.data.dataset.Dataset;
import cloud.xcan.angus.core.tester.domain.data.variables.Variable;
import cloud.xcan.angus.core.tester.domain.issue.Task;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.domain.project.Project;
import cloud.xcan.angus.core.tester.domain.project.ProjectRepo;
import cloud.xcan.angus.core.tester.domain.project.ProjectType;
import cloud.xcan.angus.core.tester.domain.project.module.Module;
import cloud.xcan.angus.core.tester.domain.project.tag.Tag;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.test.cases.FuncCase;
import cloud.xcan.angus.core.tester.infra.util.BIDUtils;
import cloud.xcan.angus.core.tester.infra.util.BIDUtils.BIDKey;
import cloud.xcan.angus.core.tester.infra.util.ProjectExportFileUtils;
import cloud.xcan.angus.core.tester.infra.util.ProjectImportFileUtils;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.remote.message.ProtocolException;
import cloud.xcan.angus.remote.search.SearchCriteria;
import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.utils.FileUtils;
import cloud.xcan.angus.spec.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.Resource;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Command implementation for project management operations.
 * <p>
 * Provides comprehensive CRUD operations for projects including creation, modification, deletion,
 * import/export, and member management.
 * <p>
 * Implements business logic validation, permission checks, activity logging, and transaction
 * management for all project operations.
 * <p>
 * Supports example data import, member management, trash functionality, and comprehensive activity
 * tracking.
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
  @Resource
  private TagQuery tagQuery;
  @Resource
  private ModuleQuery moduleQuery;
  @Resource
  private TaskQuery taskQuery;
  @Resource
  private FuncCaseQuery funcCaseQuery;
  @Resource
  private ServicesQuery servicesQuery;
  @Resource
  private ScenarioQuery scenarioQuery;
  @Resource
  private ScriptQuery scriptQuery;
  @Resource
  private VariableQuery variableQuery;
  @Resource
  private DatasetQuery datasetQuery;
  @Resource
  private MockServiceQuery mockServiceQuery;

  /**
   * Adds a new project to the system.
   * <p>
   * Performs comprehensive validation including quota limits, project name uniqueness, and member
   * existence checks.
   * <p>
   * Optionally imports example data based on project configuration and logs creation activity for
   * audit trail purposes.
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

        // Validate project name and version combination is unique within the tenant
        projectQuery.checkAddNameAndVersionExists(project.getName(), project.getVersion());

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
   * Validates project existence, user permissions, name uniqueness, and member existence before
   * updating project details.
   * <p>
   * Updates project information, manages member changes, and logs modification activity for audit
   * trail purposes.
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

        // Validate project name and version combination is unique if changed
        if (isNotEmpty(project.getName())) {
          projectQuery.checkUpdateNameAndVersionExists(project.getId(), project.getName(),
              project.getVersion());
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
   * Validates project existence, user permissions, name uniqueness, and member existence before
   * replacing project details.
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

          // Validate project name and version combination is unique if changed
          if (isNotEmpty(project.getName())) {
            projectQuery.checkUpdateNameAndVersionExists(project.getId(), project.getName(),
                project.getVersion());
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
   * Parses example project template from resources and creates a new project with the specified
   * name and type.
   * <p>
   * Imports comprehensive example data including tags, modules, tasks, cases, services, scenarios,
   * scripts, variables, datasets, mocks, and executions.
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
   * Creates a new project with example data, sets up project timeline, and establishes member
   * associations.
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
   * Moves project to trash for potential recovery and logs deletion activity for audit trail
   * purposes.
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
   * Imports various types of example data including tags, modules, tasks, cases, services,
   * scenarios, scripts, variables, datasets, mocks, and executions.
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
   * Imports project data from ZIP or TAR archive file.
   * <p>
   * Extracts archive file, classifies files by business type, and imports project and related
   * business data (tags, modules, tasks, cases, services, scenarios, scripts, variables, datasets,
   * mocks, executions).
   * <p>
   * Supports JSON format for business data and YAML format for scripts. Scripts support multiple
   * files, while other business types import only the first matching file.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> imports(ProjectType projectType, String name, MultipartFile file) {
    return new BizTemplate<IdKey<Long, Object>>() {
      @Override
      protected void checkParams() {
        // Check if project creation exceeds quota limits
        projectQuery.checkQuota(1);

        // Validate project name and version combination is unique within the tenant
        projectQuery.checkAddNameAndVersionExists(name, "V1.0");

        // Validate file name
        String fileName = file.getOriginalFilename();
        assertNotEmpty(fileName, "File name is required");
        String lowerFileName = fileName.toLowerCase();
        assertTrue(lowerFileName.endsWith(".zip") || lowerFileName.endsWith(".tar")
                || lowerFileName.endsWith(".tar.gz") || lowerFileName.endsWith(".tgz"),
            "不支持的文件格式，仅支持 ZIP (.zip) 或 TAR (.tar, .tar.gz, .tgz) 格式");
      }

      @Override
      @SneakyThrows
      protected IdKey<Long, Object> process() {
        String fileName = file.getOriginalFilename();
        File tmpPath = ProjectImportFileUtils.getImportTmpPath(fileName);
        File importFile = new File(tmpPath.getPath() + File.separator + fileName);

        try {
          // Transfer uploaded file to temporary location
          file.transferTo(importFile);

          // Extract archive files
          List<File> extractedFiles = ProjectImportFileUtils.extractArchiveFiles(tmpPath,
              importFile);

          // Find project file
          File projectFile = ProjectImportFileUtils.findProjectFile(extractedFiles);
          assertNotEmpty(projectFile, "未找到项目信息文件（文件名需包含'项目'或'project'）");

          // Parse project from JSON
          String projectJson = FileUtils.readFileToString(projectFile, StandardCharsets.UTF_8);
          Project project = JsonUtils.convert(projectJson, new TypeReference<Project>() {
          });

          // Set project properties
          project.setId(BIDUtils.getId(BIDKey.projectId));
          project.setType(projectType);
          project.setName(name);
          project.setOwnerId(getUserId());
          project.setStartDate(LocalDateTime.now());
          project.setDeadlineDate(LocalDateTime.now().plusHours(SAMPLE_AFTER_HOURS));
          if (isEmpty(project.getVersion())) {
            project.setVersion("V1.0");
          }

          // Create project
          IdKey<Long, Object> idKey = projectCmd.add0(project);

          // Classify files by business type
          Map<ExampleDataType, List<File>> classifiedFiles
              = ProjectImportFileUtils.classifyFilesByType(extractedFiles);

          // Import business data based on file classification
          importBusinessData(idKey.getId(), classifiedFiles);

          // Log project creation activity for audit
          activityCmd.add(toActivity(PROJECT, project, ActivityType.CREATED));

          return idKey;
        } finally {
          // Clean up temporary files
          FileUtils.deleteQuietly(tmpPath);
        }
      }
    }.execute();
  }

  /**
   * Imports business data from classified files.
   */
  @SneakyThrows
  private void importBusinessData(Long projectId,
      Map<ExampleDataType, List<File>> classifiedFiles) {
    // Import tags
    if (classifiedFiles.containsKey(ExampleDataType.TAG)) {
      File tagFile = classifiedFiles.get(ExampleDataType.TAG).get(0);
      String tagJson = FileUtils.readFileToString(tagFile, StandardCharsets.UTF_8);
      List<Tag> tags = JsonUtils.convert(tagJson, new TypeReference<List<Tag>>() {
      });
      Set<String> tagNames = tags.stream().map(Tag::getName).collect(Collectors.toSet());
      tagCmd.add(projectId, tagNames);
    }

    // Import modules
    if (classifiedFiles.containsKey(ExampleDataType.MODULE)) {
      File moduleFile = classifiedFiles.get(ExampleDataType.MODULE).get(0);
      String moduleJson = FileUtils.readFileToString(moduleFile, StandardCharsets.UTF_8);
      List<Module> modules = JsonUtils.convert(moduleJson, new TypeReference<List<Module>>() {
      });
      for (Module module : modules) {
        module.setProjectId(projectId);
        // Reset audit fields to null
        module.setTenantId(null);
        module.setCreatedBy(null);
        module.setCreatedDate(null);
        module.setLastModifiedBy(null);
        module.setLastModifiedDate(null);
      }
      moduleCmd.add(projectId, modules);
    }

    // Import tasks
    if (classifiedFiles.containsKey(ExampleDataType.TASK)) {
      File taskFile = classifiedFiles.get(ExampleDataType.TASK).get(0);
      String taskJson = FileUtils.readFileToString(taskFile, StandardCharsets.UTF_8);
      List<Task> tasks = JsonUtils.convert(taskJson, new TypeReference<List<Task>>() {
      });
      for (Task task : tasks) {
        task.setProjectId(projectId);
        // Clear ID to create new task
        task.setId(null);
        task.setParentTaskId(null);
        task.setSprintId(null);
        // Reset audit fields to null
        task.setTenantId(null);
        task.setCreatedBy(null);
        task.setCreatedDate(null);
        task.setLastModifiedBy(null);
        task.setLastModifiedDate(null);
        // Reset count fields to 0
        task.setFailNum(0);
        task.setTotalNum(0);
        taskCmd.add(task);
      }
    }

    // Import functional test cases
    if (classifiedFiles.containsKey(ExampleDataType.FUNC)) {
      File funcCaseFile = classifiedFiles.get(ExampleDataType.FUNC).get(0);
      String funcCaseJson = FileUtils.readFileToString(funcCaseFile, StandardCharsets.UTF_8);
      List<FuncCase> funcCases = JsonUtils.convert(funcCaseJson,
          new TypeReference<List<FuncCase>>() {
          });
      for (FuncCase funcCase : funcCases) {
        funcCase.setProjectId(projectId);
        // Clear ID to create new case
        funcCase.setId(null);
        funcCase.setPlanId(null);
        // Reset audit fields to null
        funcCase.setTenantId(null);
        funcCase.setCreatedBy(null);
        funcCase.setCreatedDate(null);
        funcCase.setLastModifiedBy(null);
        funcCase.setLastModifiedDate(null);
        // Reset count fields to 0
        funcCase.setReviewNum(0);
        funcCase.setReviewFailNum(0);
        funcCase.setTestNum(0);
        funcCase.setTestFailNum(0);
      }
      funcCaseCmd.add(funcCases);
    }

    // Import services
    if (classifiedFiles.containsKey(ExampleDataType.SERVICES)) {
      File serviceFile = classifiedFiles.get(ExampleDataType.SERVICES).get(0);
      String serviceJson = FileUtils.readFileToString(serviceFile, StandardCharsets.UTF_8);
      // Extract service name from filename (remove extension)
      String serviceName = serviceFile.getName().replaceAll("\\.(json)$", "");
      // Services import requires file or content, use content parameter
      // Default to OPENAPI import source, can be adjusted based on file content if needed
      try {
        servicesCmd.imports(projectId, null, serviceName, ApiImportSource.OPENAPI,
            StrategyWhenDuplicated.IGNORE, false, null, serviceJson);
      } catch (Exception e) {
        log.error("Failed to import service file: " + serviceFile.getName(), e);
        throw ProtocolException.of(
            "导入服务文件失败: " + serviceFile.getName() + ", 原因: " + e.getMessage());
      }
    }

    // Import scenarios
    if (classifiedFiles.containsKey(ExampleDataType.SCENARIO)) {
      File scenarioFile = classifiedFiles.get(ExampleDataType.SCENARIO).get(0);
      String scenarioJson = FileUtils.readFileToString(scenarioFile, StandardCharsets.UTF_8);
      List<Scenario> scenarios = JsonUtils.convert(scenarioJson,
          new TypeReference<List<Scenario>>() {
          });
      for (Scenario scenario : scenarios) {
        Script script = null;
        if (isNotEmpty(scenario.getScriptContent())) {
          script = new Script()
              .setProjectId(projectId)
              .setName(scenario.getName() + "_script")
              .setContent(scenario.getScriptContent());
          scriptCmd.imports(script);
        }

        scenario.setProjectId(projectId);
        scenario.setScriptId(script != null ? script.getId() : null);
        // Clear ID to create new scenario
        scenario.setId(null);
        // Reset audit fields to null
        scenario.setCreatedBy(null);
        scenario.setCreatedDate(null);
        scenario.setLastModifiedBy(null);
        scenario.setLastModifiedDate(null);
        scenarioCmd.add(scenario);
      }
    }

    // Import scripts (YAML files) - support multiple files
    if (classifiedFiles.containsKey(ExampleDataType.SCRIPT)) {
      List<File> scriptFiles = classifiedFiles.get(ExampleDataType.SCRIPT);
      for (File scriptFile : scriptFiles) {
        String scriptContent = FileUtils.readFileToString(scriptFile, StandardCharsets.UTF_8);
        try {
          String scriptName = scriptFile.getName().replaceAll("\\.(yaml|yml)$", "");
          Script script = new Script()
              .setProjectId(projectId)
              .setName(scriptName)
              .setContent(scriptContent);
          scriptCmd.imports(script);
        } catch (Exception e) {
          log.error("Failed to import script file: " + scriptFile.getName(), e);
          throw ProtocolException.of(
              "导入脚本文件失败: " + scriptFile.getName() + ", 原因: " + e.getMessage());
        }
      }
    }

    // Import variables
    if (classifiedFiles.containsKey(ExampleDataType.VARIABLE)) {
      File variableFile = classifiedFiles.get(ExampleDataType.VARIABLE).get(0);
      String variableJson = FileUtils.readFileToString(variableFile, StandardCharsets.UTF_8);
      try {
        variableCmd.imports(projectId, StrategyWhenDuplicated.IGNORE, variableJson, null);
      } catch (Exception e) {
        log.error("Failed to import variable file: " + variableFile.getName(), e);
        throw ProtocolException.of(
            "导入变量文件失败: " + variableFile.getName() + ", 原因: " + e.getMessage());
      }
    }

    // Import datasets
    if (classifiedFiles.containsKey(ExampleDataType.DATASET)) {
      File datasetFile = classifiedFiles.get(ExampleDataType.DATASET).get(0);
      String datasetJson = FileUtils.readFileToString(datasetFile, StandardCharsets.UTF_8);
      try {
        datasetCmd.imports(projectId, StrategyWhenDuplicated.IGNORE, datasetJson, null);
      } catch (Exception e) {
        log.error("Failed to import dataset file: " + datasetFile.getName(), e);
        throw ProtocolException.of(
            "导入数据集文件失败: " + datasetFile.getName() + ", 原因: " + e.getMessage());
      }
    }

    // Import mock services
    if (classifiedFiles.containsKey(ExampleDataType.MOCK)) {
      File mockFile = classifiedFiles.get(ExampleDataType.MOCK).get(0);
      String mockJson = FileUtils.readFileToString(mockFile, StandardCharsets.UTF_8);
      List<MockService> mockServices = JsonUtils.convert(mockJson,
          new TypeReference<List<MockService>>() {
          });
      for (MockService mockService : mockServices) {
        mockService.setProjectId(projectId);
        // Clear ID to create new mock service
        mockService.setId(null);
        // Reset audit fields to null
        mockService.setCreatedBy(null);
        mockService.setCreatedDate(null);
        mockService.setLastModifiedBy(null);
        mockService.setLastModifiedDate(null);
        IdKey<Long, Object> mockIdKey = mockServiceCmd.add(mockService);
        // Import mock service content if available
        if (isNotEmpty(mockService.getImportText())) {
          mockServiceCmd.imports(mockIdKey.getId(), StrategyWhenDuplicated.IGNORE, false,
              mockService.getImportText(), null);
        }
      }
    }
  }

  /**
   * Exports project data to ZIP or TAR archive.
   * <p>
   * Exports all business data (tags, modules, tasks, cases, services, scenarios, scripts,
   * variables, datasets, mock services) associated with the project to a compressed archive.
   * <p>
   * Scripts are exported to a 'scripts' subdirectory. All other business data is exported as JSON
   * files in the root directory.
   *
   * @param projectId the ID of the project to export
   * @param format    the export format: "zip" (default) or "tar"/"tar.gz"
   * @return the exported archive file
   */
  @SneakyThrows
  @Override
  public File export(Long projectId, String format) {
    return new BizTemplate<File>() {
      Project project;
      File exportDir;
      File archiveFile;

      @Override
      protected void checkParams() {
        project = projectQuery.checkAndFind(projectId);
      }

      @Override
      protected File process() {
        // Create temporary export directory
        String projectName = project.getName() != null
            ? project.getName().replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "_")
            : "Project";
        exportDir = ProjectExportFileUtils.createTempExportDir(projectName);
        File scriptsDir = ProjectExportFileUtils.createScriptsDir(exportDir);

        try {
          // Export project information
          String projectJson = JsonUtils.toJson(project);
          ProjectExportFileUtils.writeJsonFile(exportDir, "Project", projectJson);

          // Export tags
          exportTags(projectId, exportDir);

          // Export modules
          exportModules(projectId, exportDir);

          // Export tasks
          exportTasks(projectId, exportDir);

          // Export functional test cases
          exportFuncCases(projectId, exportDir);

          // Export services
          exportServices(projectId, exportDir);

          // Export scenarios
          List<Long> scriptIds = exportScenarios(projectId, exportDir);

          // Export scripts (to scripts subdirectory)
          exportScripts(projectId, scriptsDir, scriptIds);

          // Export variables
          exportVariables(projectId, exportDir);

          // Export datasets
          exportDatasets(projectId, exportDir);

          // Create archive file
          String archiveFileName = projectName + "-" + System.currentTimeMillis();
          String lowerFormat = format != null ? format.toLowerCase() : "zip";
          boolean gzip = lowerFormat.equals("tar.gz") || lowerFormat.equals("tgz");

          if (lowerFormat.equals("zip") || lowerFormat.equals("tar") || gzip) {
            archiveFile = new File(exportDir.getParent(), archiveFileName
                + (gzip ? ".tar.gz" : lowerFormat.equals("tar") ? ".tar" : ".zip"));
            if (lowerFormat.equals("zip")) {
              ProjectExportFileUtils.createZipArchive(exportDir, archiveFile);
            } else {
              ProjectExportFileUtils.createTarArchive(exportDir, archiveFile, gzip);
            }
          } else {
            throw ProtocolException.of(
                "不支持的文件格式，仅支持 ZIP (.zip) 或 TAR (.tar, .tar.gz) 格式");
          }

          // Log export activity
          activityCmd.add(toActivity(PROJECT, project, ActivityType.EXPORT));

          return archiveFile;
        } finally {
          // Clean up temporary export directory
          if (exportDir != null && exportDir.exists()) {
            FileUtils.deleteQuietly(exportDir);
          }
        }
      }
    }.execute();
  }

  /**
   * Exports tags for a project.
   */
  @SneakyThrows
  private void exportTags(Long projectId, File exportDir) {
    try {
      Set<SearchCriteria> criteria = Set.of(SearchCriteria.equal("projectId", projectId));
      GenericSpecification<Tag> spec = new GenericSpecification<>(criteria);
      PageRequest pageable = PageRequest.of(0, 10000);
      Page<Tag> page = tagQuery.list(spec, pageable, false, null);
      if (page.hasContent()) {
        String tagsJson = JsonUtils.toJson(page.getContent());
        ProjectExportFileUtils.writeJsonFile(exportDir, "Tag", tagsJson);
      }
    } catch (Exception e) {
      log.warn("Failed to export tags for project: " + projectId, e);
    }
  }

  /**
   * Exports modules for a project.
   */
  @SneakyThrows
  private void exportModules(Long projectId, File exportDir) {
    try {
      Set<SearchCriteria> criteria = Set.of(SearchCriteria.equal("projectId", projectId));
      GenericSpecification<Module> spec = new GenericSpecification<>(criteria);
      List<Module> modules = moduleQuery.find(spec, false, null);
      if (isNotEmpty(modules)) {
        String modulesJson = JsonUtils.toJson(modules);
        ProjectExportFileUtils.writeJsonFile(exportDir, "Module", modulesJson);
      }
    } catch (Exception e) {
      log.warn("Failed to export modules for project: " + projectId, e);
    }
  }

  /**
   * Exports tasks for a project.
   */
  @SneakyThrows
  private void exportTasks(Long projectId, File exportDir) {
    try {
      Set<SearchCriteria> criteria = Set.of(SearchCriteria.equal("projectId", projectId));
      GenericSpecification<Task> spec = new GenericSpecification<>(criteria);
      PageRequest pageable = PageRequest.of(0, 10000);
      Page<Task> page = taskQuery.list(false, spec, pageable, false, null);
      if (page.hasContent()) {
        String tasksJson = JsonUtils.toJson(page.getContent());
        ProjectExportFileUtils.writeJsonFile(exportDir, "Task", tasksJson);
      }
    } catch (Exception e) {
      log.warn("Failed to export tasks for project: " + projectId, e);
    }
  }

  /**
   * Exports functional test cases for a project.
   */
  @SneakyThrows
  private void exportFuncCases(Long projectId, File exportDir) {
    try {
      List<FuncCase> funcCases = funcCaseQuery.findAllByProjectId(projectId);
      if (!funcCases.isEmpty()) {
        String tasksJson = JsonUtils.toJson(funcCases);
        ProjectExportFileUtils.writeJsonFile(exportDir, "FuncCase", tasksJson);
      }
    } catch (Exception e) {
      log.warn("Failed to export functional test cases for project: " + projectId, e);
    }
  }

  /**
   * Exports services for a project.
   */
  @SneakyThrows
  private void exportServices(Long projectId, File exportDir) {
    try {
      Set<SearchCriteria> criteria = Set.of(SearchCriteria.equal("projectId", projectId));
      GenericSpecification<Services> spec = new GenericSpecification<>(criteria);
      PageRequest pageable = PageRequest.of(0, 10000);
      Page<Services> page = servicesQuery.list(spec, pageable, false, null);
      if (page.hasContent()) {
        // Export services using ServicesCmd.exportProject method
        // For now, export basic service information as JSON
        String servicesJson = JsonUtils.toJson(page.getContent());
        ProjectExportFileUtils.writeJsonFile(exportDir, "Service", servicesJson);
      }
    } catch (Exception e) {
      log.warn("Failed to export services for project: " + projectId, e);
    }
  }

  /**
   * Exports scenarios for a project.
   */
  @SneakyThrows
  private List<Long> exportScenarios(Long projectId, File exportDir) {
    List<Long> scriptIds = new ArrayList<>();
    try {
      Set<SearchCriteria> criteria = Set.of(SearchCriteria.equal("projectId", projectId));
      GenericSpecification<Scenario> spec = new GenericSpecification<>(criteria);
      PageRequest pageable = PageRequest.of(0, 10000);
      Page<Scenario> page = scenarioQuery.list(spec, pageable, false, null);
      if (page.hasContent()) {
        List<Script> scripts = scriptQuery.findAllById(page.getContent().stream()
            .map(Scenario::getScriptId).filter(Objects::nonNull).collect(Collectors.toSet()));
        Map<Long, Script> scriptMap = scripts.stream()
            .collect(Collectors.toMap(Script::getId, x -> x));
        for (Scenario scenario : page.getContent()) {
          if (scenario.getScriptId() != null && scriptMap.containsKey(scenario.getScriptId())) {
            scriptIds.add(scenario.getScriptId());
            scenario.setScriptContent(scriptMap.get(scenario.getScriptId()).getContent());
          }
        }
        String scenariosJson = JsonUtils.toJson(page.getContent());
        ProjectExportFileUtils.writeJsonFile(exportDir, "Scenario", scenariosJson);
      }
    } catch (Exception e) {
      log.warn("Failed to export scenarios for project: " + projectId, e);
    }
    return scriptIds;
  }

  /**
   * Exports scripts for a project to scripts subdirectory.
   */
  @SneakyThrows
  private void exportScripts(Long projectId, File scriptsDir, List<Long> scriptIds) {
    try {
      List<Script> scripts = isEmpty(scriptIds)
          ? scriptQuery.findByProjectId(projectId)
          : scriptQuery.findByProjectIdAndIdNot(projectId,
              scriptIds); // Exclude already exported scripts
      if (scripts.isEmpty()) {
        return;
      }
      for (Script script : scripts) {
        ProjectExportFileUtils.writeYamlFile(scriptsDir, script.getName(), script.getContent());
      }
    } catch (Exception e) {
      log.warn("Failed to export scripts for project: " + projectId, e);
    }
  }

  /**
   * Exports variables for a project.
   */
  @SneakyThrows
  private void exportVariables(Long projectId, File exportDir) {
    try {
      Set<SearchCriteria> criteria = Set.of(SearchCriteria.equal("projectId", projectId));
      GenericSpecification<Variable> spec = new GenericSpecification<>(criteria);
      PageRequest pageable = PageRequest.of(0, 10000);
      org.springframework.data.domain.Page<Variable> page =
          variableQuery.list(spec, pageable, false, null);
      if (page.hasContent()) {
        String variablesJson = JsonUtils.toJson(page.getContent());
        ProjectExportFileUtils.writeJsonFile(exportDir, "Variable", variablesJson);
      }
    } catch (Exception e) {
      log.warn("Failed to export variables for project: " + projectId, e);
    }
  }

  /**
   * Exports datasets for a project.
   */
  @SneakyThrows
  private void exportDatasets(Long projectId, File exportDir) {
    try {
      Set<SearchCriteria> criteria = Set.of(SearchCriteria.equal("projectId", projectId));
      GenericSpecification<Dataset> spec = new GenericSpecification<>(criteria);
      PageRequest pageable = PageRequest.of(0, 10000);
      Page<Dataset> page = datasetQuery.list(spec, pageable, false, null);
      if (page.hasContent()) {
        String datasetsJson = JsonUtils.toJson(page.getContent());
        ProjectExportFileUtils.writeJsonFile(exportDir, "Dataset", datasetsJson);
      }
    } catch (Exception e) {
      log.warn("Failed to export datasets for project: " + projectId, e);
    }
  }

  @Override
  protected BaseRepository<Project, Long> getRepository() {
    return this.projectRepo;
  }
}




