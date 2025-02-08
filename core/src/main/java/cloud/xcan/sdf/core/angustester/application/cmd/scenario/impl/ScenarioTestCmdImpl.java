package cloud.xcan.sdf.core.angustester.application.cmd.scenario.impl;

import static cloud.xcan.angus.core.utils.AngusUtils.overrideExecServerParameter;
import static cloud.xcan.angus.model.element.type.TestTargetType.PLUGIN_HTTP_NAME;
import static cloud.xcan.sdf.api.commonlink.CombinedTargetType.SCENARIO;
import static cloud.xcan.sdf.core.angustester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.sdf.core.angustester.domain.TesterCoreMessage.TASK_SCE_NOT_EXISTED_T;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.SUB_DISABLED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.SUB_ENABLED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TARGET_TASK_DELETED;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TARGET_TASK_GEN;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TARGET_TASK_REOPEN;
import static cloud.xcan.sdf.core.angustester.domain.activity.ActivityType.TARGET_TASK_RESTART;
import static cloud.xcan.sdf.core.angustester.domain.task.TaskType.SCENARIO_TEST;
import static cloud.xcan.sdf.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserId;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.model.element.http.Http;
import cloud.xcan.angus.model.element.type.TestTargetType;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.sdf.api.angusctrl.exec.ExecRemote;
import cloud.xcan.sdf.api.angusctrl.exec.dto.ExecAddByScriptDto;
import cloud.xcan.sdf.core.angustester.application.cmd.activity.ActivityCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.scenario.ScenarioTestCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.script.ScriptCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.task.TaskCmd;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioAuthQuery;
import cloud.xcan.sdf.core.angustester.application.query.scenario.ScenarioQuery;
import cloud.xcan.sdf.core.angustester.application.query.script.ScriptQuery;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.scenario.Scenario;
import cloud.xcan.sdf.core.angustester.domain.scenario.ScenarioRepo;
import cloud.xcan.sdf.core.angustester.domain.script.Script;
import cloud.xcan.sdf.core.angustester.domain.task.Task;
import cloud.xcan.sdf.core.angustester.domain.task.TaskRepo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskStatus;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.spec.utils.ObjectUtils;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author xiaolong.liu
 */
@Biz
public class ScenarioTestCmdImpl implements ScenarioTestCmd {

  @Resource
  private TaskCmd taskCmd;

  @Resource
  private ScenarioQuery scenarioQuery;

  @Resource
  private ScenarioAuthQuery scenarioAuthQuery;

  @Resource
  private ScenarioRepo scenarioRepo;

  @Resource
  private TaskRepo taskRepo;

  @Resource
  private ScriptQuery scriptQuery;

  @Resource
  private ScriptCmd scriptCmd;

  @Resource
  private ActivityCmd activityCmd;

  @Resource
  private ExecRemote execRemote;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void testEnabled(Long scenarioId, Set<TestType> testTypes, Boolean enabled) {
    new BizTemplate<Void>() {
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        scenarioDb = scenarioQuery.checkAndFind(scenarioId);
        scenarioAuthQuery.checkTestAuth(getUserId(), scenarioId);
      }

      @Override
      protected Void process() {
        if (testTypes.contains(TestType.FUNCTIONAL)) {
          scenarioDb.setTestFuncFlag(enabled);
          activityCmd.add(toActivity(SCENARIO, scenarioDb, enabled
              ? SUB_ENABLED : SUB_DISABLED, TestType.FUNCTIONAL));
        }
        if (testTypes.contains(TestType.PERFORMANCE)) {
          scenarioDb.setTestPerfFlag(enabled);
          activityCmd.add(toActivity(SCENARIO, scenarioDb, enabled
              ? SUB_ENABLED : SUB_DISABLED, TestType.PERFORMANCE));
        }
        if (testTypes.contains(TestType.STABILITY)) {
          scenarioDb.setTestStabilityFlag(enabled);
          activityCmd.add(toActivity(SCENARIO, scenarioDb, enabled
              ? SUB_ENABLED : SUB_DISABLED, TestType.STABILITY));
        }

        scenarioRepo.save(scenarioDb);
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void testTaskGenerate(Long scenarioId, @Nullable Long sprintId, List<Task> tasks) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        // Check the scenario test permission -> Exists in taskCmd#generate()
        // scenarioAuthQuery.checkTestAuth(getUserId(), apisId);

        // Check the task permission -> Exists in taskCmd#generate()
        // Project planDb = taskSprintQuery.checkAndFind(sprintId);
        // taskProjectAuthQuery.checkAddTaskAuth(getUserId(), planDb.getProjectId());
      }

      @Override
      protected Void process() {
        // Generate and start test tasks
        Object target = taskCmd.generate(sprintId, SCENARIO_TEST, scenarioId, tasks, false);

        activityCmd.add(toActivity(SCENARIO, (ActivityResource) target, TARGET_TASK_GEN));
        return null;
      }
    }.execute();
  }

  /**
   * Retest the task or reopen the task
   *
   * @param scenarioId  Scenario ID
   * @param restartFlag Restart is true, Reopen is false
   */
  @Transactional(rollbackFor = Exception.class)
  @Override
  public void testTaskRetest(Long scenarioId, Boolean restartFlag) {
    new BizTemplate<Void>() {
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        // Check and find scenario
        scenarioDb = scenarioQuery.checkAndFind0(scenarioId);

        // Check the apis test permission
        scenarioAuthQuery.checkTestAuth(getUserId(), scenarioId);
      }

      @Override
      protected Void process() {
        List<Task> tasksDb = taskRepo.findByTargetIdIn(List.of(scenarioId));
        assertResourceNotFound(tasksDb, TASK_SCE_NOT_EXISTED_T, new Object[]{scenarioDb.getName()});

        // Only open the finished status
        if (!restartFlag) {
          tasksDb = tasksDb.stream().filter(t -> TaskStatus.isFinished(t.getStatus()))
              .collect(Collectors.toList());
        }

        if (isNotEmpty(tasksDb)) {
          taskCmd.retest0ByTarget(restartFlag, tasksDb);

          activityCmd.add(toActivity(SCENARIO, scenarioDb, restartFlag ? TARGET_TASK_RESTART
              : TARGET_TASK_REOPEN));
        }
        return null;
      }
    }.execute();
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void testTaskDelete(Long scenarioId, Set<TestType> testTypes) {
    new BizTemplate<Void>() {
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        // Check and find scenario
        scenarioDb = scenarioQuery.checkAndFind0(scenarioId);

        // Batch check test permission
        scenarioAuthQuery.checkTestAuth(getUserId(), scenarioId);
      }

      @Override
      protected Void process() {
        List<Long> taskIds = ObjectUtils.isEmpty(testTypes)
            ? taskRepo.findIdsByTargetIdIn(List.of(scenarioId))
            : taskRepo.findIdsByTargetIdInAndTestTypeIn(List.of(scenarioId),
                testTypes.stream().map(TestType::getValue).collect(Collectors.toList()));
        if (ObjectUtils.isEmpty(taskIds)) {
          return null;
        }
        // Delete scenario test task
        taskCmd.delete0ByTarget(taskIds);

        activityCmd.add(toActivity(SCENARIO, scenarioDb, TARGET_TASK_DELETED));
        return null;
      }
    }.execute();
  }

  @Override
  public void testExecAdd(Long scenarioId, List<Server> servers) {
    new BizTemplate<Void>() {
      Scenario scenarioDb;

      @Override
      protected void checkParams() {
        // Check and find scenario
        scenarioDb = scenarioQuery.checkAndFind0(scenarioId);

        // Batch check test permission
        scenarioAuthQuery.checkTestAuth(getUserId(), scenarioId);
      }

      @Override
      protected Void process() {
        // Check only HTTP servers is supported
        if (PLUGIN_HTTP_NAME.equals(scenarioDb.getPlugin()) && isNotEmpty(servers)) {
          Script scriptDb = scriptQuery.checkAndFind(scenarioDb.getScriptId());
          AngusScript angusScript = scriptQuery.checkAndParse(scriptDb.getContent(), false);
          // Override exec server configuration parameter in variables
          Map<String, Server> serverMap = isEmpty(servers) ? Collections.emptyMap()
              : servers.stream().collect(Collectors.toMap(Server::getUrl, x -> x));
          overrideExecServerParameter(serverMap, angusScript.getConfiguration().getVariables());
          if (nonNull(angusScript.getTask()) && isNotEmpty(angusScript.getTask().getPipelines())) {
            for (TestTargetType pipeline : angusScript.getTask().getPipelines()) {
              if (pipeline instanceof Http) {
                overrideExecServerParameter(serverMap, (Http) pipeline);
              }
            }
          }
          scriptCmd.update0(scriptDb, angusScript);
        }

        // Note: Execution must be completed after the scriptCmd.update0() transaction is committed.
        execRemote.addByScript(new ExecAddByScriptDto().setScriptId(scenarioDb.getScriptId()))
            .orElseContentThrow();
        return null;
      }
    }.execute();
  }

}
