package cloud.xcan.angus.core.tester.application.cmd.services.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SERVICE;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.application.converter.TaskConverter.generateToServicesTask;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_SECURITY_CASE_NOT_FOUND;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_SMOKE_CASE_NOT_FOUND;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TARGET_SCRIPT_DELETED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TARGET_SCRIPT_GEN;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TARGET_TASK_DELETED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TARGET_TASK_GEN;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TARGET_TASK_REOPEN;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TARGET_TASK_RESTART;
import static cloud.xcan.angus.model.script.ScriptSource.API;
import static cloud.xcan.angus.model.script.ScriptSource.SERVICE_SECURITY;
import static cloud.xcan.angus.model.script.ScriptSource.SERVICE_SMOKE;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Collections.singletonList;

import cloud.xcan.angus.api.ctrl.exec.ExecRemote;
import cloud.xcan.angus.api.ctrl.exec.dto.ExecAddByScriptDto;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisTestCmd;
import cloud.xcan.angus.core.tester.application.cmd.script.ScriptCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesTestCmd;
import cloud.xcan.angus.core.tester.application.cmd.task.TaskCmd;
import cloud.xcan.angus.core.tester.application.query.apis.ApisCaseQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisRepo;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCase;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.testing.TestTaskSetting;
import cloud.xcan.angus.core.tester.domain.task.Task;
import cloud.xcan.angus.core.tester.domain.task.TaskRepo;
import cloud.xcan.angus.core.tester.domain.task.TaskStatus;
import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
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
          apisRepo.updateTestFuncByService(serviceId, enabled);
          activityCmd.add(toActivity(SERVICE, servicesDb, enabled
              ? ActivityType.SUB_ENABLED : ActivityType.SUB_DISABLED, TestType.FUNCTIONAL));
        }
        if (testTypes.contains(TestType.PERFORMANCE)) {
          apisRepo.updateTestPerfByService(serviceId, enabled);
          activityCmd.add(toActivity(SERVICE, servicesDb, enabled
              ? ActivityType.SUB_ENABLED : ActivityType.SUB_DISABLED, TestType.PERFORMANCE));
        }
        if (testTypes.contains(TestType.STABILITY)) {
          apisRepo.updateTestStabilityByService(serviceId, enabled);
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
  public void retestTaskRestart(Long serviceId, Boolean restart) {
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
          if (!restart) {
            tasksDb = tasksDb.stream().filter(t -> TaskStatus.isFinished(t.getStatus()))
                .collect(Collectors.toList());
          }
          if (ObjectUtils.isNotEmpty(tasksDb)) {
            taskCmd.retest0ByTarget(restart, tasksDb);
          }
        }
        activityCmd.add(toActivity(SERVICE, serviceDb, restart
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
