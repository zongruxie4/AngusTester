package cloud.xcan.angus.core.tester.interfaces.apis.facade.internal;


import static cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisTestAssembler.generateToScript;
import static cloud.xcan.angus.core.tester.interfaces.apis.facade.internal.assembler.ApisTestAssembler.generateToTask;
import static java.util.Collections.singletonList;

import cloud.xcan.angus.core.tester.application.cmd.apis.ApisTestCmd;
import cloud.xcan.angus.core.tester.application.query.apis.ApisTestQuery;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.ApisTestFacade;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.test.ApisTestScriptGenerateDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.test.ApisTestTaskGenerateDto;
import cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.test.TestResultDetailVo;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.ExecResultFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.TaskTestFacade;
import cloud.xcan.angus.model.script.TestType;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.annotation.Resource;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.springframework.stereotype.Component;

/**
 * @author XiaoLong Liu
 */
@Component
public class ApisTestFacadeImpl implements ApisTestFacade {

  @Resource
  private ApisTestCmd apisTestCmd;

  @Resource
  private ApisTestQuery apisTestQuery;

  @Resource
  private TaskTestFacade taskTestFacade;

  @Resource
  private ExecResultFacade execResultFacade;

  @Override
  public void testEnabled(Long apisId, Set<TestType> testTypes, boolean enabled) {
    apisTestCmd.testEnabled(apisId, testTypes, enabled);
  }

  @Override
  public List<TestType> testEnabledFind(Long apisId) {
    return apisTestQuery.findEnabledTestTypes(apisId);
  }

  @Override
  public void scriptGenerate(Long apisId, Set<ApisTestScriptGenerateDto> dto) {
    apisTestCmd.scriptGenerate(apisId, generateToScript(dto));
  }

  @Override
  public void scriptDelete(Long apisId, Set<TestType> testTypes) {
    apisTestCmd.scriptDelete(apisId, testTypes);
  }

  @Override
  public void testTaskGenerate(Long apisId, @Nullable Long taskSprintId, Set<ApisTestTaskGenerateDto> dto) {
    apisTestCmd.testTaskGenerate(apisId, taskSprintId, generateToTask(apisId, dto), false);
  }

  @Override
  public void testTaskRetest(Long apisId) {
    apisTestCmd.testTaskRetest(apisId, true);
  }

  @Override
  public void testTaskReopen(Long apisId) {
    apisTestCmd.testTaskRetest(apisId, false);
  }

  @Override
  public void testTaskDelete(Long apisId, Set<TestType> testTypes) {
    apisTestCmd.testTaskDelete(singletonList(apisId), testTypes);
  }

  @Override
  public void testExecAdd(Long apisId, Set<TestType> testTypes, @Nullable List<Server> servers) {
    apisTestCmd.testExecAdd(apisId, testTypes, servers);
  }

  @Override
  public void testExecAdd(HashSet<Long> apisIds, HashSet<TestType> testTypes,
      @Nullable List<Server> servers) {
    apisTestCmd.testExecAdd(apisIds, testTypes, servers);
  }

  @Override
  public void testCaseExecAdd(Long apisId, LinkedHashSet<Long> caseIds) {
    apisTestCmd.testCaseExecAdd(apisId, caseIds);
  }

  @Override
  public TestResultDetailVo testResultDetail(Long apisId) {
    TestResultDetailVo result = new TestResultDetailVo();
    result.setTestResult(execResultFacade.apisResult(apisId));
    result.setAssocTasks(taskTestFacade.assocList(TaskType.API_TEST, apisId));
    return result;
  }

}
