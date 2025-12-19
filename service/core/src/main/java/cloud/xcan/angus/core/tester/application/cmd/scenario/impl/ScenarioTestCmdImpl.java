package cloud.xcan.angus.core.tester.application.cmd.scenario.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SCENARIO;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.SUB_DISABLED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.SUB_ENABLED;
import static cloud.xcan.angus.core.utils.AngusUtils.overrideExecServerParameter;
import static cloud.xcan.angus.model.element.type.TestTargetType.PLUGIN_HTTP_NAME;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static java.util.Objects.nonNull;

import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.exec.ExecCmd;
import cloud.xcan.angus.core.tester.application.cmd.scenario.ScenarioTestCmd;
import cloud.xcan.angus.core.tester.application.cmd.script.ScriptCmd;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioAuthQuery;
import cloud.xcan.angus.core.tester.application.query.scenario.ScenarioQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.domain.scenario.Scenario;
import cloud.xcan.angus.core.tester.domain.scenario.ScenarioRepo;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.model.element.http.Http;
import cloud.xcan.angus.model.element.type.TestTargetType;
import cloud.xcan.angus.model.script.AngusScript;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.script.pipeline.Arguments;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

/**
 * Command implementation for scenario test management.
 * <p>
 * Provides methods for enabling/disabling, generating, retesting, deleting, and executing scenario
 * test tasks.
 * <p>
 * Ensures permission checks, activity logging, and batch operations with transaction management.
 */
@Service
public class ScenarioTestCmdImpl implements ScenarioTestCmd {

  @Resource
  private ScenarioQuery scenarioQuery;
  @Resource
  private ScenarioAuthQuery scenarioAuthQuery;
  @Resource
  private ScenarioRepo scenarioRepo;
  @Resource
  private ScriptQuery scriptQuery;
  @Resource
  private ScriptCmd scriptCmd;
  @Resource
  private ActivityCmd activityCmd;
  @Resource
  private ExecCmd execCmd;

  /**
   * Enable or disable scenario test types.
   * <p>
   * Checks existence and permission, updates test type status, and logs activity.
   */
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
          scenarioDb.setTestFunc(enabled);
          activityCmd.add(toActivity(SCENARIO, scenarioDb, enabled
              ? SUB_ENABLED : SUB_DISABLED, TestType.FUNCTIONAL));
        }
        if (testTypes.contains(TestType.PERFORMANCE)) {
          scenarioDb.setTestPerf(enabled);
          activityCmd.add(toActivity(SCENARIO, scenarioDb, enabled
              ? SUB_ENABLED : SUB_DISABLED, TestType.PERFORMANCE));
        }
        if (testTypes.contains(TestType.STABILITY)) {
          scenarioDb.setTestStability(enabled);
          activityCmd.add(toActivity(SCENARIO, scenarioDb, enabled
              ? SUB_ENABLED : SUB_DISABLED, TestType.STABILITY));
        }

        scenarioRepo.save(scenarioDb);
        return null;
      }
    }.execute();
  }

  /**
   * Add execution servers to a scenario test.
   * <p>
   * Checks existence and permission, updates script server configuration, and triggers execution.
   */
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
        execCmd.addByRemoteScript(null, scenarioDb.getScriptId(), null,
            null, new Arguments(), null);
        return null;
      }
    }.execute();
  }

}
