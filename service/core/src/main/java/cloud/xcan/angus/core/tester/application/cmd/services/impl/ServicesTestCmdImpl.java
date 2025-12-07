package cloud.xcan.angus.core.tester.application.cmd.services.impl;

import static cloud.xcan.angus.api.commonlink.CombinedTargetType.SERVICE;
import static cloud.xcan.angus.core.biz.ProtocolAssert.assertResourceNotFound;
import static cloud.xcan.angus.core.tester.application.converter.ActivityConverter.toActivity;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_SECURITY_CASE_NOT_FOUND;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_SMOKE_CASE_NOT_FOUND;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TARGET_SCRIPT_DELETED;
import static cloud.xcan.angus.core.tester.domain.activity.ActivityType.TARGET_SCRIPT_GEN;
import static cloud.xcan.angus.model.script.ScriptSource.API;
import static cloud.xcan.angus.model.script.ScriptSource.SERVICE_SECURITY;
import static cloud.xcan.angus.model.script.ScriptSource.SERVICE_SMOKE;
import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static java.util.Collections.singletonList;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.tester.application.cmd.activity.ActivityCmd;
import cloud.xcan.angus.core.tester.application.cmd.apis.ApisTestCmd;
import cloud.xcan.angus.core.tester.application.cmd.exec.ExecCmd;
import cloud.xcan.angus.core.tester.application.cmd.issue.TaskCmd;
import cloud.xcan.angus.core.tester.application.cmd.script.ScriptCmd;
import cloud.xcan.angus.core.tester.application.cmd.services.ServicesTestCmd;
import cloud.xcan.angus.core.tester.application.query.apis.ApisCaseQuery;
import cloud.xcan.angus.core.tester.application.query.apis.ApisQuery;
import cloud.xcan.angus.core.tester.application.query.script.ScriptQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesAuthQuery;
import cloud.xcan.angus.core.tester.application.query.services.ServicesQuery;
import cloud.xcan.angus.core.tester.domain.activity.ActivityType;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisRepo;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCase;
import cloud.xcan.angus.core.tester.domain.issue.TaskRepo;
import cloud.xcan.angus.core.tester.domain.script.Script;
import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.model.element.http.ApisCaseType;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.model.script.pipeline.Arguments;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of service testing command operations.
 *
 * <p>This class provides comprehensive functionality for managing service-level
 * testing operations, including test configuration, script generation, task management, and
 * execution control.</p>
 *
 * <p>It handles various test types including functional, performance, stability,
 * smoke, and security testing with proper authorization and activity logging.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Test type enablement and configuration</li>
 *   <li>Script generation and management</li>
 *   <li>Test task creation and execution</li>
 *   <li>Smoke and security testing</li>
 *   <li>Activity logging for audit trails</li>
 * </ul></p>
 */
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
  private ExecCmd execCmd;

  /**
   * Enables or disables specific test types for a service.
   *
   * <p>This method controls the availability of different test types
   * (functional, performance, stability) for all APIs within a service.</p>
   *
   * <p>The method logs the enablement/disablement activity for audit purposes.</p>
   *
   * @param serviceId the ID of the service
   * @param testTypes the set of test types to configure
   * @param enabled   whether to enable or disable the test types
   * @throws IllegalArgumentException if validation fails
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void testEnabled(Long serviceId, Set<TestType> testTypes, Boolean enabled) {
    new BizTemplate<Void>() {
      Services servicesDb;

      @Override
      protected void checkParams() {
        // Verify service exists
        servicesDb = servicesQuery.checkAndFind(serviceId);
        // Verify user has testing permissions
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

  /**
   * Generates test scripts for all APIs in a service.
   *
   * <p>This method creates test scripts for all APIs within a service
   * based on the specified script types. It only generates scripts that don't already exist for
   * each API.</p>
   *
   * <p>The method logs script generation activity for audit purposes.</p>
   *
   * @param serviceId the ID of the service
   * @param scripts   the list of script configurations to generate
   * @throws IllegalArgumentException if validation fails
   */
  @Override
  //@Transactional(rollbackFor = Exception.class)
  public void scriptGenerate(Long serviceId, List<Script> scripts) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Verify user has testing permissions
        servicesAuthQuery.checkTestAuth(getUserId(), serviceId);
        // Verify service exists
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

  /**
   * Deletes test scripts for all APIs in a service.
   *
   * <p>This method removes test scripts for all APIs within a service
   * based on the specified test types.</p>
   *
   * <p>The method logs script deletion activity for audit purposes.</p>
   *
   * @param serviceId the ID of the service
   * @param testTypes the set of test types to delete scripts for
   * @throws IllegalArgumentException if validation fails
   */
  @Override
  @Transactional(rollbackFor = Exception.class)
  public void scriptDelete(Long serviceId, Set<TestType> testTypes) {
    new BizTemplate<Void>() {
      Services serviceDb;

      @Override
      protected void checkParams() {
        // Verify user has testing permissions
        servicesAuthQuery.checkTestAuth(getUserId(), serviceId);
        // Verify service exists
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
   * Adds test executions for all APIs in a service.
   *
   * <p>This method creates test executions for all APIs within a service
   * based on the specified test types and server configurations. Only enabled test types are
   * considered for execution.</p>
   *
   * @param servicesId the ID of the service
   * @param testTypes  the set of test types to execute
   * @param servers    the list of server configurations for testing
   * @throws IllegalArgumentException if validation fails
   */
  //@Transactional(rollbackFor = Exception.class)
  @Override
  public void testExecAdd(Long servicesId, Set<TestType> testTypes, List<Server> servers) {
    new BizTemplate<Void>() {

      @Override
      protected void checkParams() {
        // Verify user has testing permissions
        servicesAuthQuery.checkTestAuth(getUserId(), servicesId);
        // Verify service exists
        servicesQuery.checkAndFind(servicesId);
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

  /**
   * Adds smoke test execution for a service.
   *
   * <p>This method creates smoke test execution for a service by
   * synchronizing smoke test cases to scripts and creating execution tasks. It validates that smoke
   * test cases exist before execution.</p>
   *
   * @param servicesId the ID of the service
   * @param servers    the list of server configurations for testing (optional)
   * @throws IllegalArgumentException if validation fails or smoke cases not found
   */
  @Override
  public void testSmokeExecAdd(Long servicesId, @Nullable List<Server> servers) {
    new BizTemplate<Void>() {
      Services serviceDb;
      List<ApisCase> casesDb;

      @Override
      protected void checkParams() {
        // Verify user has testing permissions
        servicesAuthQuery.checkTestAuth(getUserId(), servicesId);
        // Verify service exists
        serviceDb = servicesQuery.checkAndFind(servicesId);
        // Verify smoke test cases exist
        casesDb = apisCaseQuery.findByServicesIdAndType(servicesId, ApisCaseType.SMOKE);
        assertResourceNotFound(casesDb, SERVICE_SMOKE_CASE_NOT_FOUND, new Object[]{});
      }

      @Override
      protected Void process() {
        // Synchronize testing cases to script
        long scriptId = scriptCmd.syncServiceCaseToScript(serviceDb, SERVICE_SMOKE, casesDb,
            servers);

        // Create case functional testing execution
        execCmd.addByRemoteScript(null, scriptId, null, null, new Arguments(), null);
        return null;
      }
    }.execute();
  }

  /**
   * Adds security test execution for a service.
   *
   * <p>This method creates security test execution for a service by
   * synchronizing security test cases to scripts and creating execution tasks. It validates that
   * security test cases exist before execution.</p>
   *
   * @param servicesId the ID of the service
   * @param servers    the list of server configurations for testing (optional)
   * @throws IllegalArgumentException if validation fails or security cases not found
   */
  @Override
  public void testSecurityExecAdd(Long servicesId, @Nullable List<Server> servers) {
    new BizTemplate<Void>() {
      Services serviceDb;
      List<ApisCase> casesDb;

      @Override
      protected void checkParams() {
        // Verify user has testing permissions
        servicesAuthQuery.checkTestAuth(getUserId(), servicesId);
        // Verify service exists
        serviceDb = servicesQuery.checkAndFind(servicesId);
        // Verify security test cases exist
        casesDb = apisCaseQuery.findByServicesIdAndType(servicesId, ApisCaseType.SECURITY);
        assertResourceNotFound(casesDb, SERVICE_SECURITY_CASE_NOT_FOUND, new Object[]{});
      }

      @Override
      protected Void process() {
        // Synchronize testing cases to script
        long scriptId = scriptCmd.syncServiceCaseToScript(serviceDb, SERVICE_SECURITY, casesDb,
            servers);

        // Create case functional testing execution
        execCmd.addByRemoteScript(null, scriptId, null, null, new Arguments(), null);
        return null;
      }
    }.execute();
  }

}
