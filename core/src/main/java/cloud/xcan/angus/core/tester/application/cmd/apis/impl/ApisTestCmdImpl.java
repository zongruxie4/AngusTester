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
import static cloud.xcan.angus.core.tester.domain.task.TaskType.API_TEST;
import static cloud.xcan.angus.core.utils.AngusUtils.overrideExecServerParameter;
import static cloud.xcan.angus.model.script.TestType.PERFORMANCE;
import static cloud.xcan.angus.model.script.TestType.STABILITY;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.api.commonlink.apis.ApiPermission;
import cloud.xcan.angus.api.ctrl.exec.ExecRemote;
import cloud.xcan.angus.api.ctrl.exec.dto.ExecAddByScriptDto;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisCaseCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisTestCmd;
import cloud.xcan.angus.core.tester.application.cmd.script.ScriptCmd;
import cloud.xcan.angus.core.tester.application.cmd.task.TaskCmd;
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
import cloud.xcan.angus.core.tester.domain.task.Task;
import cloud.xcan.angus.core.tester.domain.task.TaskRepo;
import cloud.xcan.angus.core.tester.domain.task.TaskStatus;
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
 * @author XiaoLong Liu
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
  private ExecRemote execRemote;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void testEnabled(Long apisId, Set<TestType> testTypes, boolean enabled) {
    new BizTemplate<Void>() {
      Apis apisDb;

      @Override
      protected void checkParams() {
        apisDb = apisQuery.checkAndFind(apisId);
        apisAuthQuery.checkTestAuth(getUserId(), apisId);
      }

      @Override
      protected Void process() {
        if (testTypes.contains(TestType.FUNCTIONAL)) {
          apisDb.setTestFunc(enabled);
          activityCmd.add(toActivity(API, apisDb,
              enabled ? SUB_ENABLED : SUB_DISABLED, TestType.FUNCTIONAL));
        }
        if (testTypes.contains(PERFORMANCE)) {
          apisDb.setTestPerf(enabled);
          activityCmd.add(toActivity(API, apisDb,
              enabled ? SUB_ENABLED : SUB_DISABLED, TestType.PERFORMANCE));
        }
        if (testTypes.contains(STABILITY)) {
          apisDb.setTestStability(enabled);
          activityCmd.add(toActivity(API, apisDb,
              enabled ? SUB_ENABLED : SUB_DISABLED, TestType.STABILITY));
        }
        apisRepo.save(apisDb);
        return null;
      }
    }.execute();
  }

  /**
   * When the api test script does not exist, the corresponding script will be generated. If the
   * test script already exists, the current test information will be ignore.
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void scriptGenerate(Long apisId, List<Script> scripts) {
    new BizTemplate<Void>() {

      @Override
      protected void checkParams() {
        // Check the apis exists
        // apisQuery.checkAndFindBaseInfo(apisId); -> Do in apisQuery.findDeRefById(apisId);

        // Check the apis test permission
        apisAuthQuery.checkTestAuth(getUserId(), apisId);
      }

      @Override
      protected Void process() {
        Map<ScriptType, ScriptInfo> scriptsDbMap = scriptQuery.findInfoBySource(ScriptSource.API,
            apisId).stream().collect(Collectors.toMap(ScriptInfo::getType, x -> x));
        if (!scripts.stream().allMatch(x -> scriptsDbMap.containsKey(x.getType()))) {
          Apis apisDb = apisQuery.findDeRefById(apisId);
          apisTestCmd.scriptGenerate0(apisDb, null, scripts);
        }
        return null;
      }
    }.execute();
  }

  /**
   * When the api test script does not exist, the corresponding script will be generated. If the
   * test script already exists, the current test information will be ignore.
   *
   * @param serverMap Specify server configuration when executing tests.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void scriptGenerate0(Apis apisDb, Map<String, Server> serverMap, List<Script> scripts) {
    List<Variable> variables = variableTargetQuery.findVariables(apisDb.getId(), API.getValue());
    List<Dataset> datasets = datasetTargetQuery.findDatasets(apisDb.getId(), API.getValue());

    // The api currentServer that did not trigger save is empty
    if (isNull(apisDb.getCurrentServer()) || !apisDb.getCurrentServer().isValidUrl()) {
      // Set available servers
      apisQuery.setAndGetAvailableServers(apisDb);
    }

    // Replace authentication references
    if (nonNull(apisDb.getAuthentication()) && apisDb.isAuthSchemaRef()
        && apisDb.includeSchemaRef(apisDb.getAuthentication().get$ref())){
      apisQuery.setAndGetRefAuthentication(apisDb);
    }

    // Find existing scripts
    Map<ScriptType, ScriptInfo> scriptsDbMap = scriptQuery.findInfoBySource(ScriptSource.API,
        apisDb.getId()).stream().collect(Collectors.toMap(ScriptInfo::getType, x -> x));

    // Save non-existent apis scripts, ignore existing apis scripts
    // Build Http or WebSocket script, include apisId, plugin, http task
    for (Script script : scripts) {
      // Ignore existing
      if (scriptsDbMap.containsKey(script.getType())) {
        continue;
      }
      initScriptAndCases(apisDb, serverMap, script, variables, datasets);
    }
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void scriptDelete(Long apisId, Set<TestType> testTypes) {
    new BizTemplate<Void>() {
      ApisBaseInfo apisDb;

      @Override
      protected void checkParams() {
        // Check the apis exists
        apisDb = apisQuery.checkAndFindBaseInfo(apisId);

        // Check the test permission
        apisAuthQuery.checkTestAuth(getUserId(), apisId);
      }

      @Override
      protected Void process() {
        // Delete apis test script
        scriptCmd.deleteBySource(ScriptSource.API, List.of(apisId),
            testTypes.stream().map(TestType::toScriptType).collect(Collectors.toList()));

        // Delete script activity
        activityCmd.add(toActivity(API, apisDb, TARGET_SCRIPT_DELETED));
        return null;
      }
    }.execute();
  }

  @Override
  public void scriptDelete0(Long apisId, Set<TestType> testTypes) {
    // Delete apis test script
    scriptCmd.deleteBySource(ScriptSource.API, List.of(apisId),
        testTypes.stream().map(TestType::toScriptType).collect(Collectors.toList()));
  }

  /**
   * When the api test task does not exist, the corresponding task will be generated. If the test
   * task already exists, the current test information will be overwritten.
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void testTaskGenerate(Long apisId, @Nullable Long sprintId, List<Task> tasks,
      boolean ignoreApisPermission) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // Check the apis test permission -> Exists in taskCmd#generate()
        // apisAuthQuery.checkTestAuth(getUserId(), apisId);

        // Check the task permission -> Exists in taskCmd#generate()
        // Project sprintDb = taskSprintQuery.checkAndFind(sprintId);
        // taskProjectAuthQuery.checkAddTaskAuth(getUserId(), sprintDb.getProjectId());
      }

      @Override
      protected Void process() {
        // Generate tasks
        Object target = taskCmd.generate(sprintId, API_TEST, apisId, tasks, ignoreApisPermission);

        // Save activity
        activityCmd.add(toActivity(API, (ActivityResource) target, TARGET_TASK_GEN));
        return null;
      }
    }.execute();
  }

  /**
   * Retest or reopen the task
   *
   * @param apisId      Apis ID
   * @param restart Restart is true, Reopen is false
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void testTaskRetest(Long apisId, Boolean restart) {
    new BizTemplate<Void>() {
      ApisBaseInfo apisDb = null;

      @Override
      protected void checkParams() {
        // Check the associated apis exists
        apisDb = apisQuery.checkAndFindBaseInfo(apisId);
        assertTrue(!apisDb.isWebSocket(), TASK_WEBSOCKET_NOT_SUPPORT_GEN_TASK);

        // Check the apis test permission
        Long userId = getUserId();
        apisAuthQuery.checkTestAuth(userId, apisId);
      }

      @Override
      protected Void process() {
        List<Task> tasksDb = taskRepo.findByTargetIdIn(List.of(apisId));
        assertResourceNotFound(tasksDb, TASK_APIS_NOT_EXISTED_T, new Object[]{apisDb.getName()});

        // Only open the finished status
        if (!restart) {
          tasksDb = tasksDb.stream().filter(t -> TaskStatus.isFinished(t.getStatus()))
              .collect(Collectors.toList());
        }
        if (isNotEmpty(tasksDb)) {
          taskCmd.retest0ByTarget(restart, tasksDb);

          activityCmd.add(toActivity(API, apisDb, restart ? TARGET_TASK_RESTART
              : TARGET_TASK_REOPEN));
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void testTaskDelete(List<Long> apisIds, Set<TestType> testTypes) {
    new BizTemplate<Void>() {
      List<ApisBaseInfo> apisDb;

      @Override
      protected void checkParams() {
        // Check the apis exists
        apisDb = apisQuery.checkAndFindBaseInfos(apisIds);

        // Batch check test permission
        apisAuthQuery.batchCheckPermission(apisIds, ApiPermission.TEST);
      }

      @Override
      protected Void process() {
        List<Long> taskIds = isEmpty(testTypes)
            ? taskRepo.findIdsByTargetIdIn(apisIds)
            : taskRepo.findIdsByTargetIdInAndTestTypeIn(apisIds,
                testTypes.stream().map(TestType::getValue).collect(Collectors.toList()));
        if (isEmpty(taskIds)) {
          return null;
        }

        // Delete apis test task
        taskCmd.delete0ByTarget(taskIds);

        activityCmd.addAll(toActivities(API, apisDb, TARGET_TASK_DELETED));
        return null;
      }
    }.execute();
  }

  @Override
  public void testExecAdd(Long apisId, Set<TestType> testTypes, @Nullable List<Server> servers) {
    new BizTemplate<Void>() {
      Apis apisDb;

      @Override
      protected void checkParams() {
        // Check and find apis
        apisDb = apisQuery.findDeRefById(apisId);

        // Check the test permission
        apisAuthQuery.checkTestAuth(getUserId(), apisId);
      }

      @Override
      protected Void process() {
        testExecAdd0(apisDb, testTypes, servers);
        return null;
      }
    }.execute();
  }

  @Override
  public void testExecAdd(HashSet<Long> apisIds, HashSet<TestType> testTypes,
      @Nullable List<Server> servers) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // NOOP
      }

      @Override
      protected Void process() {
        for (Long apisId : apisIds) {
          testExecAdd(apisId, testTypes, servers);
        }
        return null;
      }
    }.execute();
  }

  @Override
  public void testCaseExecAdd(Long apisId, LinkedHashSet<Long> caseIds) {
    new BizTemplate<Void>() {
      Apis apisDb = null;

      @Override
      protected void checkParams() {
        // Check the associated apis exists
        apisDb = apisQuery.checkAndFind(apisId);

        // Check the apis test permission
        apisAuthQuery.checkTestAuth(getUserId(), apisId);
      }

      @Override
      protected Void process() {
        List<ApisCase> casesDb = apisCaseQuery.findByApisId(apisId);
        if (isNotEmpty(casesDb)) {
          for (ApisCase case0 : casesDb) {
            case0.setEnabled(caseIds.contains(case0.getId()));
          }

          // Synchronize testing cases to script
          long scriptId = scriptCmd.syncApisCaseToScript(apisDb, casesDb);

          // Create case functional testing execution
          // Note: Execution must be completed after the syncApisCaseToScript() transaction is committed.
          execRemote.addByScript(new ExecAddByScriptDto().setScriptId(scriptId))
              .orElseContentThrow();
        }
        return null;
      }
    }.execute();
  }

  @Override
  public void testExecAdd0(Apis apisDb, Set<TestType> testTypes, List<Server> servers) {
    Map<ScriptType, Script> scriptsDbMap = scriptQuery.findBySource(ScriptSource.API,
        apisDb.getId()).stream().collect(Collectors.toMap(Script::getType, x -> x));
    Map<String, Server> serverMap = isEmpty(servers) ? Collections.emptyMap()
        : servers.stream().collect(Collectors.toMap(Server::getUrl, x -> x));

    for (TestType testType : testTypes) {
      ScriptType scriptType = testType.toScriptType();
      if (scriptsDbMap.containsKey(scriptType)) {
        // Override exec server configuration parameter
        if (isNotEmpty(servers)) {
          AngusScript angusScript = scriptQuery.checkAndParse(
              scriptsDbMap.get(scriptType).getContent(), false);
          if (nonNull(angusScript.getTask()) && isNotEmpty(angusScript.getTask().getPipelines())) {
            List<Http> https = (List<Http>) angusScript.getTask().getPipelines();
            for (Http http : https) {
              // Override exec server configuration parameter in http
              overrideExecServerParameter(serverMap, http);
            }
          }
          if (nonNull(angusScript.getConfiguration())
              && isNotEmpty(angusScript.getConfiguration().getVariables())) {
            List<cloud.xcan.angus.model.element.variable.Variable> variables
                = angusScript.getConfiguration().getVariables();
            // Override exec server configuration parameter in variables
            overrideExecServerParameter(serverMap, variables);
          }
          scriptCmd.update0(scriptsDbMap.get(scriptType), angusScript);
        }
      } else {
        // Generate scripts when it does not exist.
        IndicatorPerf indicatorPerf = testType.equals(PERFORMANCE)
            ? indicatorPerfQuery.detailAndDefault(API, apisDb.getId()) : null;
        IndicatorStability indicatorStability = testType.equals(STABILITY)
            ? indicatorStabilityQuery.detailAndDefault(API, apisDb.getId()) : null;
        List<Script> scripts = ApisTestConverter.startToScript(apisDb, Set.of(testType),
            indicatorPerf, indicatorStability);
        apisTestCmd.scriptGenerate0(apisDb, serverMap, scripts);
      }
    }

    List<Long> scriptIds = scriptQuery.findInfoBySource(ScriptSource.API, apisDb.getId())
        .stream().filter(x -> testTypes.contains(TestType.of(x.getType())))
        .map(ScriptInfo::getId).collect(Collectors.toList());
    if (isNotEmpty(scriptIds)) {
      for (Long scriptId : scriptIds) {
        // Note: Execution must be completed after the scriptGenerated0() transaction is committed.
        execRemote.addByScript(new ExecAddByScriptDto().setScriptId(scriptId))
            .orElseContentThrow();
      }
    }
  }

  @Transactional(rollbackFor = Exception.class)
  public void initScriptAndCases(Apis apisDb, Map<String, Server> serverMap,
      Script script, List<Variable> variables, List<Dataset> datasets) {
    script.setProjectId(apisDb.getProjectId());
    script.setSourceId(apisDb.getId());
    if (script.getType().isFunctionalTesting() /*&& apisDb.getTestFunc()*/) {
      IndicatorFunc indicatorFunc = indicatorFuncQuery.detailAndDefault(API, apisDb.getId());
      // Note: Deleting a script does not delete testing cases, and each time a script is generated, all testing cases need to be loaded
      Map<ApisCaseType, List<ApisCase>> typeCasesMap = apisCaseQuery.findByApisId(apisDb.getId())
          .stream().collect(Collectors.groupingBy(ApisCase::getType));
      assembleAddApisScript(apisDb, serverMap, indicatorFunc, typeCasesMap, script,
          variables, datasets);
      if (isNotEmpty(script.getAngusScript().getTask().getPipelines())) {
        apisCaseCmd.add(apisDb.getId(), script.getAngusScript().getTask().getPipelines()
            .stream().filter(x -> !((Http) x).isPersistent()) // Add new cases
            .map(x -> httpToFuncCase(apisDb, (Http) x)).collect(Collectors.toList()));
      }
      scriptCmd.add(script, script.getAngusScript(), false);
    } else if (script.getType().isPerformanceTesting() /*&& apisDb.getTestPerf()*/) {
      assembleAddApisScript(apisDb, serverMap, null, null, script, variables, datasets);
      scriptCmd.add(script, script.getAngusScript(), false);
    } else if (script.getType().isStabilityTesting() /*&& apisDb.getTestStability()*/) {
      assembleAddApisScript(apisDb, serverMap, null, null, script, variables, datasets);
      scriptCmd.add(script, script.getAngusScript(), false);
    }
  }
}
