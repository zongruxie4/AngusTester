package cloud.xcan.sdf.core.angustester.application.cmd.services.impl;

import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.SERVICE;
import static cloud.xcan.sdf.model.script.ScriptSource.API;
import static cloud.xcan.sdf.model.script.ScriptSource.SERVICE_SECURITY;
import static cloud.xcan.sdf.model.script.ScriptSource.SERVICE_SMOKE;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.application.converter.TaskConverter.generateToServicesTask;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.SERVICE_SECURITY_CASE_NOT_FOUND;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.SERVICE_SMOKE_CASE_NOT_FOUND;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TARGET_SCRIPT_DELETED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TARGET_SCRIPT_GEN;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TARGET_TASK_DELETED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TARGET_TASK_GEN;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TARGET_TASK_REOPEN;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TARGET_TASK_RESTART;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static java.util.Collections.singletonList;

import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.api.angusctrl.exec.ExecRemote;
import cloud.xcan.sdf.api.angusctrl.exec.dto.ExecAddByScriptDto;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.apis.ApisTestCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.script.ScriptCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.services.ServicesTestCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.task.TaskCmd;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisCaseQuery;
import cloud.xcan.sdf.core.angustester.application.query.apis.ApisQuery;
import cloud.xcan.sdf.core.angustester.application.query.data.VariableQuery;
import cloud.xcan.sdf.core.angustester.application.query.script.ScriptQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.services.ServicesQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityType;
import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisRepo;
import cloud.xcan.sdf.core.angustester.domain.apis.cases.ApisCase;
import cloud.xcan.sdf.core.angustester.domain.script.Script;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptInfo;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.angustester.domain.services.testing.TestTaskSetting;
import cloud.xcan.sdf.core.angustester.domain.task.Task;
import cloud.xcan.sdf.core.angustester.domain.task.TaskRepo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
public class ServicesTestCmdImpl implements ServicesTestCmd {

  @Resource
  private ServicesQuery servicesQuery;

  @Resource
  private ServicesAuthQuery servicesAuthQuery;

  @Resource
  private ApisRepo apisRepo;

  @Resource
  private TaskRepo taskRepo;

  @Resource
  private ApisQuery apisQuery;

  @Resource
  private ApisCaseQuery apisCaseQuery;

  @Resource
  private ApisTestCmd apisTestCmd;

  @Resource
  private ScriptQuery scriptQuery;

  @Resource
  private TaskCmd taskCmd;

  @Resource
  private ScriptCmd scriptCmd;

  @Resource
  private VariableQuery variableQuery;

  @Resource
  private ActivityCmd activityCmd;

  @Resource
  private ExecRemote execRemote;

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void testEnabled(Long serviceId, Set<TestType> testTypes, Boolean enabled) {
    new BizTemplate<Void>() {
      Services servicesDb;

      @Override
      protected void checkParams() {
        servicesDb = servicesQuery.checkAndFind(serviceId);
        servicesAuthQuery.checkTestAuth(getUserId(), serviceId);
      }

      @Override
      protected Void process() {
        if (testTypes.contains(TestType.FUNCTIONAL)) {
          apisRepo.updateTestFuncFlagByService(serviceId, enabled);
          activityCmd.add(toActivity(SERVICE, servicesDb, enabled
              ? ActivityType.SUB_ENABLED : ActivityType.SUB_DISABLED, TestType.FUNCTIONAL));
        }
        if (testTypes.contains(TestType.PERFORMANCE)) {
          apisRepo.updateTestPerfFlagByService(serviceId, enabled);
          activityCmd.add(toActivity(SERVICE, servicesDb, enabled
              ? ActivityType.SUB_ENABLED : ActivityType.SUB_DISABLED, TestType.PERFORMANCE));
        }
        if (testTypes.contains(TestType.STABILITY)) {
          apisRepo.updateTestStabilityFlagByService(serviceId, enabled);
          activityCmd.add(toActivity(SERVICE, servicesDb, enabled
              ? ActivityType.SUB_ENABLED : ActivityType.SUB_DISABLED, TestType.STABILITY));
        }
        return null;
      }
    }.execute();
  }

  @Override
  //@Transactional(rollbackFor = Exception.class)
  public void scriptGenerate(Long serviceId, List<Script> scripts) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Check the services test permissions?
        servicesAuthQuery.checkTestAuth(getUserId(), serviceId);
        // Check the services exists
        serviceDb = servicesQuery.checkAndFind(serviceId);
      }

      @Override
      protected Void process() {
        List<Long> apiIds = apisRepo.findAllIdByServiceIdIn(singletonList(serviceId));
        if (isEmpty(apiIds)) {
          return null;
        }

        for (Long apiId : apiIds) {
          Map<ScriptType, ScriptInfo> scriptsDbMap = scriptQuery.findInfoBySource(API, apiId)
              .stream().collect(Collectors.toMap(ScriptInfo::getType, x -> x));
          if (!scripts.stream().allMatch(x -> scriptsDbMap.containsKey(x.getType()))) {
            Apis apisDb = apisQuery.findDeRefById(apiId);
            apisTestCmd.scriptGenerate0(apisDb, null, scripts);
          }
        }

        activityCmd.add(toActivity(SERVICE, serviceDb, TARGET_SCRIPT_GEN));
        return null;
      }
    }.execute();
  }

  @Override
  @Transactional(rollbackFor = Exception.class)
  public void scriptDelete(Long serviceId, Set<TestType> testTypes) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Check the services test permissions?
        servicesAuthQuery.checkTestAuth(getUserId(), serviceId);
        // Check the services exists
        serviceDb = servicesQuery.checkAndFind(serviceId);
      }

      @Override
      protected Void process() {
        List<Long> apiIds = apisRepo.findAllIdByServiceIdIn(singletonList(serviceId));
        if (isEmpty(apiIds)) {
          return null;
        }
        for (Long apiId : apiIds) {
          apisTestCmd.scriptDelete0(apiId, testTypes);
        }
        activityCmd.add(toActivity(SERVICE, serviceDb, TARGET_SCRIPT_DELETED));
        return null;
      }
    }.execute();
  }

  /**
   * Only generate api test tasks under the current services
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void testTaskGenerate(Long serviceId, Long sprintId, List<TestTaskSetting> testings) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Check the services test permissions?
        servicesAuthQuery.checkTestAuth(getUserId(), serviceId);
        // Check the services exists
        serviceDb = servicesQuery.checkAndFind(serviceId);
      }

      @Override
      protected Void process() {
        List<Long> apiIds = apisRepo.findAllIdByServiceIdIn(singletonList(serviceId));
        if (isEmpty(apiIds)) {
          return null;
        }

        for (Long apiId : apiIds) {
          List<Task> tasks = generateToServicesTask(apiId, testings);
          apisTestCmd.testTaskGenerate(apiId, sprintId, tasks, false);
        }

        activityCmd.add(toActivity(SERVICE, serviceDb, TARGET_TASK_GEN));
        return null;
      }
    }.execute();
  }

  /**
   * Only retest api test tasks under the current services
   */
  @Override
  public void retestTaskRestart(Long serviceId, Boolean restartFlag) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Check the services test permissions?
        Long userId = getUserId();
        servicesAuthQuery.checkTestAuth(userId, serviceId);
        // Check the services exists
        serviceDb = servicesQuery.checkAndFind(serviceId);
      }

      @Override
      protected Void process() {
        List<Long> apiIds = apisRepo.findAllIdByServiceIdIn(singletonList(serviceId));
        if (isEmpty(apiIds)) {
          return null;
        }
        for (Long apiId : apiIds) {
          List<Task> tasksDb = taskRepo.findByTargetIdIn(singletonList(apiId));
          if (isEmpty(tasksDb)) {
            continue;
          }
          // Only open the finished status
          if (!restartFlag) {
            tasksDb = tasksDb.stream().filter(t -> TaskStatus.isFinished(t.getStatus()))
                .collect(Collectors.toList());
          }
          if (ObjectUtils.isNotEmpty(tasksDb)) {
            taskCmd.retest0ByTarget(restartFlag, tasksDb);
          }
        }
        activityCmd.add(toActivity(SERVICE, serviceDb, restartFlag
            ? TARGET_TASK_RESTART : TARGET_TASK_REOPEN));
        return null;
      }
    }.execute();
  }

  @Override
  public void testTaskDelete(Long serviceId, Set<TestType> testTypes) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Check and get services
        serviceDb = servicesQuery.checkAndFind(serviceId);

        // Check the test permission
        Long userId = getUserId();
        servicesAuthQuery.checkTestAuth(userId, serviceId);
      }

      @Override
      protected Void process() {
        List<Long> taskIds = isEmpty(testTypes)
            ? taskRepo.findTaskIdByTargetParentId(serviceId)
            : taskRepo.findTaskIdByTargetParentIdAndTestTypeIn(serviceId,
                testTypes.stream().map(TestType::getValue).collect(Collectors.toList()));
        if (isEmpty(taskIds)) {
          return null;
        }

        // Delete apis test task
        taskCmd.delete0ByTarget(taskIds);

        activityCmd.add(toActivity(SERVICE, serviceDb, TARGET_TASK_DELETED));
        return null;
      }
    }.execute();
  }

  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void testExecAdd(Long servicesId, Set<TestType> testTypes, List<Server> servers) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Check the services test permissions?
        servicesAuthQuery.checkTestAuth(getUserId(), servicesId);
        // Check the services exists
        serviceDb = servicesQuery.checkAndFind(servicesId);
      }

      @Override
      protected Void process() {
        List<Long> apiIds = apisRepo.findAllIdByServiceIdIn(singletonList(servicesId));
        if (isEmpty(apiIds)) {
          return null;
        }

        for (Long apiId : apiIds) {
          Apis apisDb = apisQuery.findDeRefById(apiId);
          // Note: Ignoring test types that are not enabled
          Set<TestType> needTestTypes = apisDb.needTestTypes();
          needTestTypes.retainAll(testTypes);
          if (!needTestTypes.isEmpty()) {
            apisTestCmd.testExecAdd0(apisDb, needTestTypes, servers);
          }
        }
        return null;
      }
    }.execute();
  }

  @Override
  public void testSmokeExecAdd(Long servicesId, @Nullable List<Server> servers) {
    new BizTemplate<Void>() {
      Services serviceDb;
      List<ApisCase> casesDb;

      @Override
      protected void checkParams() {
        // Check the services test permissions?
        servicesAuthQuery.checkTestAuth(getUserId(), servicesId);
        // Check the services exists
        serviceDb = servicesQuery.checkAndFind(servicesId);
        // Check the smoke cases exists
        casesDb = apisCaseQuery.findByServicesIdAndType(servicesId, ApisCaseType.SMOKE);
        assertResourceNotFound(casesDb, SERVICE_SMOKE_CASE_NOT_FOUND, new Object[]{});
      }

      @Override
      protected Void process() {
        // Synchronize testing cases to script
        long scriptId = scriptCmd.syncServiceCaseToScript(serviceDb, SERVICE_SMOKE, casesDb,
            servers);

        // Create case functional testing execution
        execRemote.addByScript(new ExecAddByScriptDto().setScriptId(scriptId)).orElseContentThrow();
        return null;
      }
    }.execute();
  }

  @Override
  public void testSecurityExecAdd(Long servicesId, @Nullable List<Server> servers) {
    new BizTemplate<Void>() {
      Services serviceDb;
      List<ApisCase> casesDb;

      @Override
      protected void checkParams() {
        // Check the services test permissions?
        servicesAuthQuery.checkTestAuth(getUserId(), servicesId);
        // Check the services exists
        serviceDb = servicesQuery.checkAndFind(servicesId);
        // Check the smoke cases exists
        casesDb = apisCaseQuery.findByServicesIdAndType(servicesId, ApisCaseType.SECURITY);
        assertResourceNotFound(casesDb, SERVICE_SECURITY_CASE_NOT_FOUND, new Object[]{});
      }

      @Override
      protected Void process() {
        // Synchronize testing cases to script
        long scriptId = scriptCmd.syncServiceCaseToScript(serviceDb, SERVICE_SECURITY, casesDb,
            servers);

        // Create case functional testing execution
        execRemote.addByScript(new ExecAddByScriptDto().setScriptId(scriptId)).orElseContentThrow();
        return null;
      }
    }.execute();
  }

}
