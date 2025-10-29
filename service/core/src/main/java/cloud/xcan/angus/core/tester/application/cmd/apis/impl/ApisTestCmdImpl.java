package cloud.xcan.angus.core.tester.application.cmd.apis.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.API;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertTrue;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivities;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.ApisCaseConverter.httpToFuncCase;
import static cloud.xcan.angus.core.tester.application.converter.ApisToAngusModelConverter.assembleAddApisScript;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_APIS_NOT_EXISTED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.TASK_WEBSOCKET_NOT_SUPPORT_GEN_TASK;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.SUB_DISABLED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.SUB_ENABLED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TARGET_SCRIPT_DELETED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TARGET_TASK_DELETED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TARGET_TASK_GEN;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TARGET_TASK_REOPEN;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TARGET_TASK_RESTART;
import static cloud.xcan.angus.core.tester.domain.issue.TaskType.API_TEST;
import static cloud.xcan.angus.core.utils.AngusUtils.overrideExecServerParameter;
import static cloud.xcan.angus.model.script.TestType.PERFORMANCE;
import static cloud.xcan.angus.model.script.TestType.STABILITY;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.apis.ApiPermission;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisCaseCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisTestCmd;
import cloud.xcan.angus.core.tester.application.cmd.exec.ExecCmd;
import cloud.xcan.angus.core.tester.application.cmd.script.ScriptCmd;
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskCmd;
import cloud.xcan.angus.core.tester.application.converter.ApisTestConverter;
import cloud.xcan.angus.core.tester.application.query.apis.ApisAuthQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisCaseQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.data.DatasetTargetQuery;
import cloud.xcan.angus.core.tester.application.query.data.VariableTargetQuery;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorFuncQuery;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorPerfQuery;
import cloud.xcan.angus.core.tester.application.query.indicator.IndicatorStabilityQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.ApisRepo;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCase;
import cloud.xcan.angus.core.tester.domain.data.dataset.Dataset;
import cloud.xcan.angus.core.tester.domain.data.variables.Variable;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorFunc;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorPerf;
import cloud.xcan.angus.core.tester.domain.indicator.IndicatorStability;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.tester.domain.issue.Task;
import cloud.xcan.angus.core.tester.domain.issue.TaskRepo;
import cloud.xcan.angus.core.tester.domain.issue.TaskStatus;
import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.angus.model.element.http.Http;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Implementation of ApisTestCmd for API test execution and script management.
 * </p>
 * <p>
 * Provides comprehensive API testing services including enabling/disabling tests, generating scripts,
 * managing test tasks, and synchronizing cases. Handles permission checks, activity logging, and
 * integration with related modules. Supports functional, performance, and stability testing with
 * script generation and execution management.
 * </p>
 */
@Biz
public class ApisTestCmdImpl implements ApisTestCmd {

  @Resource
  private TaskCmd taskCmd;
  @Resource
  private ApisQuery apisQuery;
  @Resource
  private ApisAuthQuery apisAuthQuery;
  @Resource
  private ApisCaseCmd apisCaseCmd;
  @Resource
  private ApisCaseQuery apisCaseQuery;
  @Resource
  private ApisRepo apisRepo;
  @Resource
  private TaskRepo taskRepo;
  @Resource
  private IndicatorFuncQuery indicatorFuncQuery;
  @Resource
  private IndicatorPerfQuery indicatorPerfQuery;
  @Resource
  private IndicatorStabilityQuery indicatorStabilityQuery;
  @Resource
  private VariableTargetQuery variableTargetQuery;
  @Resource
  private DatasetTargetQuery datasetTargetQuery;
  @Resource
  private ScriptQuery scriptQuery;
  @Resource
  private ScriptCmd scriptCmd;
  @Resource
  private ActivityCmd activityCmd;
  @Resource
  private ApisTestCmd apisTestCmd;
  @Resource
  private ExecCmd execCmd;

  /**
   * <p>
   * Enable or disable test types for an API.
   * </p>
   * <p>
   * Validates permission, updates test flags, and logs activities.
   * </p>
   * @param apisId API ID
   * @param testTypes Set of test types to enable/disable
   * @param enabled Whether to enable or disable the test types
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void testEnabled(Long apisId, Set<TestType> testTypes, boolean enabled) {
    new BizTemplate<Void>() {
      Apis apisDb;

      @Override
      protected void checkParams() {
        // Validate API exists
        apisDb = apisQuery.checkAndFind(apisId);

        // Verify current user has test permission for the API
        apisAuthQuery.checkTestAuth(getUserId(), apisId);
      }

      @Override
      protected Void process() {
        // Update functional testing flag and log activity
        if (testTypes.contains(TestType.FUNCTIONAL)) {
          apisDb.setTestFunc(enabled);
          activityCmd.add(toActivity(API, apisDb,
              enabled ? SUB_ENABLED : SUB_DISABLED, TestType.FUNCTIONAL));
        }

        // Update performance testing flag and log activity
        if (testTypes.contains(PERFORMANCE)) {
          apisDb.setTestPerf(enabled);
          activityCmd.add(toActivity(API, apisDb,
              enabled ? SUB_ENABLED : SUB_DISABLED, TestType.PERFORMANCE));
        }

        // Update stability testing flag and log activity
        if (testTypes.contains(STABILITY)) {
          apisDb.setTestStability(enabled);
          activityCmd.add(toActivity(API, apisDb,
              enabled ? SUB_ENABLED : SUB_DISABLED, TestType.STABILITY));
        }

        // Save updated API configuration
        apisRepo.save(apisDb);
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Generate test scripts for an API if not already present.
   * </p>
   * <p>
   * Validates permission and generates scripts if needed. Checks existing scripts
   * to avoid duplicate generation.
   * </p>
   * @param apisId API ID
   * @param scripts List of scripts to generate
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void scriptGenerate(Long apisId, List<Script> scripts) {
    new BizTemplate<Void>() {

      @Override
      protected void checkParams() {
        // API existence check is performed in apisQuery.findDeRefById(apisId)
        // apisQuery.checkAndFindBaseInfo(apisId); -> Do in apisQuery.findDeRefById(apisId);

        // Verify current user has test permission for the API
        apisAuthQuery.checkTestAuth(getUserId(), apisId);
      }

      @Override
      protected Void process() {
        // Check existing scripts for the API to avoid duplicate generation
        Map<ScriptType, ScriptInfo> scriptsDbMap = scriptQuery.findInfoBySource(ScriptSource.API,
            apisId).stream().collect(Collectors.toMap(ScriptInfo::getType, x -> x));

        // Generate scripts only if not all requested script types already exist
        if (!scripts.stream().allMatch(x -> scriptsDbMap.containsKey(x.getType()))) {
          // Retrieve API with dereferenced content for script generation
          Apis apisDb = apisQuery.findDeRefById(apisId);
          apisTestCmd.scriptGenerate0(apisDb, null, scripts);
        }
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Generate test scripts for an API with server configuration.
   * </p>
   * <p>
   * Validates permission, prepares environment, and generates scripts.
   * </p>
   * @param apisDb API entity
   * @param serverMap Map of server configurations
   * @param scripts List of scripts to generate
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void scriptGenerate0(Apis apisDb, Map<String, Server> serverMap, List<Script> scripts) {
    // Retrieve associated variables and datasets for the API
    List<Variable> variables = variableTargetQuery.findVariables(apisDb.getId(), API.getValue());
    List<Dataset> datasets = datasetTargetQuery.findDatasets(apisDb.getId(), API.getValue());

    // Ensure API has a valid current server configuration
    if (isNull(apisDb.getCurrentServer()) || !apisDb.getCurrentServer().isValidUrl()) {
      // Set available servers from API configuration
      apisQuery.setAndGetAvailableServers(apisDb);
    }

    // Handle authentication schema references
    if (nonNull(apisDb.getAuthentication()) && apisDb.isAuthSchemaRef()
        && apisDb.includeSchemaRef(apisDb.getAuthentication().get$ref())) {
      // Resolve authentication schema references
      apisQuery.setAndGetRefAuthentication(apisDb);
    }

    // Find existing scripts for the API
    Map<ScriptType, ScriptInfo> scriptsDbMap = scriptQuery.findInfoBySource(ScriptSource.API,
        apisDb.getId()).stream().collect(Collectors.toMap(ScriptInfo::getType, x -> x));

    // Generate scripts for each requested type (skip existing ones)
    for (Script script : scripts) {
      // Skip if script already exists
      if (scriptsDbMap.containsKey(script.getType())) {
        continue;
      }

      // Initialize script and associated test cases
      initScriptAndCases(apisDb, serverMap, script, variables, datasets);
    }
  }

  /**
   * <p>
   * Delete test scripts for an API and test types.
   * </p>
   * <p>
   * Validates permission, deletes scripts, and logs the activity.
   * </p>
   * @param apisId API ID
   * @param testTypes Set of test types whose scripts should be deleted
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void scriptDelete(Long apisId, Set<TestType> testTypes) {
    new BizTemplate<Void>() {
      ApisBaseInfo apisDb;

      @Override
      protected void checkParams() {
        // Validate API exists and retrieve basic information
        apisDb = apisQuery.checkAndFindBaseInfo(apisId);

        // Verify current user has test permission for the API
        apisAuthQuery.checkTestAuth(getUserId(), apisId);
      }

      @Override
      protected Void process() {
        // Delete scripts for the specified API and test types
        scriptCmd.deleteBySource(ScriptSource.API, List.of(apisId),
            testTypes.stream().map(TestType::toScriptType).toList());

        // Log script deletion activity
        activityCmd.add(toActivity(API, apisDb, TARGET_SCRIPT_DELETED));
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Delete test scripts for an API and test types without permission check.
   * </p>
   * <p>
   * Directly deletes scripts for the specified test types. This method is used
   * internally when permission checks have already been performed.
   * </p>
   * @param apisId API ID
   * @param testTypes Set of test types whose scripts should be deleted
   */
  @Override
  public void scriptDelete0(Long apisId, Set<TestType> testTypes) {
    // Delete scripts for the specified API and test types (no permission check)
    scriptCmd.deleteBySource(ScriptSource.API, List.of(apisId),
        testTypes.stream().map(TestType::toScriptType).toList());
  }

  /**
   * <p>
   * Generate or reopen test tasks for an API.
   * </p>
   * <p>
   * Validates permission, generates or reopens tasks, and logs activities.
   * Permission checks are delegated to taskCmd.generate() method.
   * </p>
   * @param apisId API ID
   * @param sprintId Sprint ID (optional)
   * @param tasks List of tasks to generate
   * @param ignoreApisPermission Whether to ignore API permission checks
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void testTaskGenerate(Long apisId, @Nullable Long sprintId, List<Task> tasks, boolean ignoreApisPermission) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // API test permission check is performed in taskCmd.generate()
        // apisAuthQuery.checkTestAuth(getUserId(), apisId);

        // Task permission check is performed in taskCmd.generate()
        // Project sprintDb = taskSprintQuery.checkAndFind(sprintId);
        // taskProjectAuthQuery.checkAddTaskAuth(getUserId(), sprintDb.getProjectId());
      }

      @Override
      protected Void process() {
        // Generate test tasks using the task command service
        Object target = taskCmd.generate(sprintId, API_TEST, apisId, tasks, ignoreApisPermission);

        // Log task generation activity
        activityCmd.add(toActivity(API, (ActivityResource) target, TARGET_TASK_GEN));
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Retest or reopen test tasks for an API.
   * </p>
   * <p>
   * Validates permission, filters tasks, and logs activities. WebSocket APIs
   * are not supported for task generation.
   * </p>
   * @param apisId API ID
   * @param restart Whether to restart all tasks or only reopen finished ones
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void testTaskRetest(Long apisId, Boolean restart) {
    new BizTemplate<Void>() {
      ApisBaseInfo apisDb = null;

      @Override
      protected void checkParams() {
        // Validate API exists and retrieve basic information
        apisDb = apisQuery.checkAndFindBaseInfo(apisId);

        // WebSocket APIs are not supported for task generation
        assertTrue(!apisDb.isWebSocket(), TASK_WEBSOCKET_NOT_SUPPORT_GEN_TASK);

        // Verify current user has test permission for the API
        Long userId = getUserId();
        apisAuthQuery.checkTestAuth(userId, apisId);
      }

      @Override
      protected Void process() {
        // Find all tasks associated with the API
        List<Task> tasksDb = taskRepo.findByTargetIdIn(List.of(apisId));
        assertResourceNotFound(tasksDb, TASK_APIS_NOT_EXISTED_T, new Object[]{apisDb.getName()});

        // Filter tasks based on restart parameter
        if (!restart) {
          // Only reopen tasks with finished status
          tasksDb = tasksDb.stream().filter(t -> TaskStatus.isFinished(t.getStatus()))
              .toList();
        }

        // Retest or reopen tasks if any exist
        if (isNotEmpty(tasksDb)) {
          taskCmd.retest0ByTarget(restart, tasksDb);

          // Log appropriate activity based on restart parameter
          activityCmd.add(toActivity(API, apisDb, restart ? TARGET_TASK_RESTART
              : TARGET_TASK_REOPEN));
        }
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Delete test tasks for APIs and test types.
   * </p>
   * <p>
   * Validates permission, deletes tasks, and logs activities.
   * </p>
   * @param apisIds List of API IDs
   * @param testTypes Set of test types whose tasks should be deleted (optional)
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void testTaskDelete(List<Long> apisIds, Set<TestType> testTypes) {
    new BizTemplate<Void>() {
      List<ApisBaseInfo> apisDb;

      @Override
      protected void checkParams() {
        // Validate APIs exist and retrieve basic information
        apisDb = apisQuery.checkAndFindBaseInfos(apisIds);

        // Verify current user has test permission for all APIs
        apisAuthQuery.batchCheckPermission(apisIds, ApiPermission.TEST);
      }

      @Override
      protected Void process() {
        // Find task IDs based on test types filter
        List<Long> taskIds = isEmpty(testTypes)
            ? taskRepo.findIdsByTargetIdIn(apisIds)  // All tasks for the APIs
            : taskRepo.findIdsByTargetIdInAndTestTypeIn(apisIds,  // Tasks for specific test types
                testTypes.stream().map(TestType::getValue).toList());

        // Skip deletion if no tasks found
        if (isEmpty(taskIds)) {
          return null;
        }

        // Delete the identified test tasks
        taskCmd.delete0ByTarget(taskIds);

        // Log task deletion activities
        activityCmd.addAll(toActivities(API, apisDb, TARGET_TASK_DELETED));
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Add test execution for an API and test types.
   * </p>
   * <p>
   * Validates permission, prepares environment, and triggers execution.
   * </p>
   * @param apisId API ID
   * @param testTypes Set of test types to execute
   * @param servers Optional server configurations to override
   */
  @Override
  public void testExecAdd(Long apisId, Set<TestType> testTypes, @Nullable List<Server> servers) {
    new BizTemplate<Void>() {
      Apis apisDb;

      @Override
      protected void checkParams() {
        // Retrieve API with dereferenced content for execution
        apisDb = apisQuery.findDeRefById(apisId);

        // Verify current user has test permission for the API
        apisAuthQuery.checkTestAuth(getUserId(), apisId);
      }

      @Override
      protected Void process() {
        // Prepare and execute tests with the prepared API entity
        testExecAdd0(apisDb, testTypes, servers);
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Add test execution for multiple APIs and test types.
   * </p>
   * <p>
   * Triggers execution for each API and test type in sequence.
   * </p>
   * @param apisIds Set of API IDs
   * @param testTypes Set of test types to execute
   * @param servers Optional server configurations to override
   */
  @Override
  public void testExecAdd(HashSet<Long> apisIds, HashSet<TestType> testTypes, @Nullable List<Server> servers) {
    new BizTemplate<Void>() {

      @Override
      protected Void process() {
        // Execute tests for each API sequentially
        for (Long apisId : apisIds) {
          testExecAdd(apisId, testTypes, servers);
        }
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Add test execution for all cases of an API.
   * </p>
   * <p>
   * Validates permission, updates case status, synchronizes with scripts, and triggers execution.
   * </p>
   * @param apisId API ID
   * @param caseIds Set of case IDs to enable/disable
   */
  @Override
  public void testCaseExecAdd(Long apisId, LinkedHashSet<Long> caseIds) {
    new BizTemplate<Void>() {
      Apis apisDb = null;

      @Override
      protected void checkParams() {
        // Validate API exists and retrieve full details
        apisDb = apisQuery.checkAndFind(apisId);

        // Verify current user has test permission for the API
        apisAuthQuery.checkTestAuth(getUserId(), apisId);
      }

      @Override
      protected Void process() {
        // Retrieve all test cases for the API
        List<ApisCase> casesDb = apisCaseQuery.findByApisId(apisId);
        if (isNotEmpty(casesDb)) {
          // Update enabled status for each case based on caseIds
          for (ApisCase case0 : casesDb) {
            case0.setEnabled(caseIds.contains(case0.getId()));
          }

          // Synchronize test cases to script for execution
          long scriptId = scriptCmd.syncApisCaseToScript(apisDb, casesDb);

          // Create functional testing execution for the synchronized script
          // Note: Execution must be completed after the syncApisCaseToScript() transaction is committed.
          execCmd.addByRemoteScript(null, scriptId, null, null, null, null);
        }
        return null;
      }
    }.execute();
  }

  /**
   * <p>
   * Add test execution for an API and test types with prepared scripts.
   * </p>
   * <p>
   * Prepares scripts, overrides server configuration, and triggers execution.
   * Handles script generation for missing test types and server parameter overrides.
   * </p>
   * @param apisDb API entity with dereferenced content
   * @param testTypes Set of test types to execute
   * @param servers Optional server configurations to override
   */
  @Override
  public void testExecAdd0(Apis apisDb, Set<TestType> testTypes, List<Server> servers) {
    // Find existing scripts for the API
    Map<ScriptType, Script> scriptsDbMap = scriptQuery.findBySource(ScriptSource.API,
        apisDb.getId()).stream().collect(Collectors.toMap(Script::getType, x -> x));

    // Create server configuration map for parameter overrides
    Map<String, Server> serverMap = isEmpty(servers) ? Collections.emptyMap()
        : servers.stream().collect(Collectors.toMap(Server::getUrl, x -> x));

    // Process each test type
    for (TestType testType : testTypes) {
      ScriptType scriptType = testType.toScriptType();
      if (scriptsDbMap.containsKey(scriptType)) {
        // Script exists - override server configuration if provided
        if (isNotEmpty(servers)) {
          // Parse existing script for modification
          AngusScript angusScript = scriptQuery.checkAndParse(
              scriptsDbMap.get(scriptType).getContent(), false);

          // Override server parameters in HTTP pipelines
          if (nonNull(angusScript.getTask()) && isNotEmpty(angusScript.getTask().getPipelines())) {
            List<Http> https = (List<Http>) angusScript.getTask().getPipelines();
            for (Http http : https) {
              overrideExecServerParameter(serverMap, http);
            }
          }

          // Override server parameters in configuration variables
          if (nonNull(angusScript.getConfiguration())
              && isNotEmpty(angusScript.getConfiguration().getVariables())) {
            List<cloud.xcan.angus.model.element.variable.Variable> variables
                = angusScript.getConfiguration().getVariables();
            overrideExecServerParameter(serverMap, variables);
          }

          // Update the modified script
          scriptCmd.update0(scriptsDbMap.get(scriptType), angusScript);
        }
      } else {
        // Script doesn't exist - generate new scripts
        // Retrieve performance indicators for performance testing
        IndicatorPerf indicatorPerf = testType.equals(PERFORMANCE)
            ? indicatorPerfQuery.detailOrDefault(API, apisDb.getId()) : null;

        // Retrieve stability indicators for stability testing
        IndicatorStability indicatorStability = testType.equals(STABILITY)
            ? indicatorStabilityQuery.detailOrDefault(API, apisDb.getId()) : null;

        // Generate scripts with appropriate indicators
        List<Script> scripts = ApisTestConverter.startToScript(apisDb, Set.of(testType),
            indicatorPerf, indicatorStability);
        apisTestCmd.scriptGenerate0(apisDb, serverMap, scripts);
      }
    }

    // Find script IDs for the requested test types and trigger execution
    List<Long> scriptIds = scriptQuery.findInfoBySource(ScriptSource.API, apisDb.getId())
        .stream().filter(x -> testTypes.contains(TestType.of(x.getType())))
        .map(ScriptInfo::getId).toList();

    if (isNotEmpty(scriptIds)) {
      for (Long scriptId : scriptIds) {
        // Note: Execution must be completed after the scriptGenerated0() transaction is committed.
        execCmd.addByRemoteScript(null, scriptId, null, null, null, null);
      }
    }
  }

  /**
   * <p>
   * Initialize script and cases for an API.
   * </p>
   * <p>
   * Prepares script content, synchronizes cases, and saves scripts.
   * Handles different test types (functional, performance, stability) with
   * appropriate indicators and case management.
   * </p>
   * @param apisDb API entity
   * @param serverMap Map of server configurations
   * @param script Script to initialize
   * @param variables Associated variables
   * @param datasets Associated datasets
   */
  @Transactional(rollbackFor = Exception.class)
  public void initScriptAndCases(Apis apisDb, Map<String, Server> serverMap, Script script, List<Variable> variables, List<Dataset> datasets) {
    // Set basic script properties
    script.setProjectId(apisDb.getProjectId());
    script.setSourceId(apisDb.getId());

    if (script.getType().isFunctionalTesting() /*&& apisDb.getTestFunc()*/) {
      // Functional testing script initialization
      // Retrieve functional testing indicators
      IndicatorFunc indicatorFunc = indicatorFuncQuery.detailOrDefault(API, apisDb.getId());

      // Note: Deleting a script does not delete testing cases, and each time a script is generated, all testing cases need to be loaded
      // Group test cases by type for functional testing
      Map<ApisCaseType, List<ApisCase>> typeCasesMap = apisCaseQuery.findByApisId(apisDb.getId())
          .stream().collect(Collectors.groupingBy(ApisCase::getType));

      // Assemble API script with functional testing components
      assembleAddApisScript(apisDb, serverMap, indicatorFunc, typeCasesMap, script,
          variables, datasets);

      // Add new test cases from script pipelines (non-persistent cases)
      if (isNotEmpty(script.getAngusScript().getTask().getPipelines())) {
        apisCaseCmd.add(apisDb.getId(), script.getAngusScript().getTask().getPipelines()
            .stream().filter(x -> !((Http) x).isPersistent()) // Add new cases
            .map(x -> httpToFuncCase(apisDb, (Http) x)).toList());
      }

      // Save the functional testing script
      scriptCmd.add(script, script.getAngusScript(), false);

    } else if (script.getType().isPerformanceTesting() /*&& apisDb.getTestPerf()*/) {
      // Performance testing script initialization
      // Assemble API script without functional testing components
      assembleAddApisScript(apisDb, serverMap, null, null, script, variables, datasets);

      // Save the performance testing script
      scriptCmd.add(script, script.getAngusScript(), false);

    } else if (script.getType().isStabilityTesting() /*&& apisDb.getTestStability()*/) {
      // Stability testing script initialization
      // Assemble API script without functional testing components
      assembleAddApisScript(apisDb, serverMap, null, null, script, variables, datasets);

      // Save the stability testing script
      scriptCmd.add(script, script.getAngusScript(), false);
    }
  }
}
