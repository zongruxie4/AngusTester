package cloud.xcan.angus.core.tester.interfaces.task.facade.internal;

import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.core.biz.JoinSupplier;
import cloud.xcan.angus.core.biz.NameJoin;
import cloud.xcan.angus.core.tester.application.query.task.TaskQuery;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultDetailSummary;
import cloud.xcan.angus.core.tester.domain.task.TaskInfo;
import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.core.tester.interfaces.exec.facade.ExecResultFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.TaskTestFacade;
import cloud.xcan.angus.core.tester.interfaces.task.facade.internal.assembler.TaskAssembler;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.TaskAssocVo;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.angus.remote.message.ProtocolException;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class TaskTestFacadeImpl implements TaskTestFacade {

  @Resource
  private TaskQuery taskQuery;

  @Resource
  private ExecResultFacade execResultFacade;

  @Resource
  private JoinSupplier joinSupplier;

  @Override
  public Map<ScriptType, List<TaskAssocVo>> assocList(TaskType taskType, Long targetId) {
    List<TaskAssocVo> assocVos = joinSupplier.execute(() -> getAssocVos(taskType, targetId));
    return isEmpty(assocVos) ? null : assocVos.stream()
        .collect(Collectors.groupingBy(TaskAssocVo::getScriptType));
  }

  @Override
  public ExecTestResultDetailSummary testResult(TaskType taskType, Long targetId, TestType testType) {
    if (taskType.isApiTest()) {
      return execResultFacade.apisResultByScriptType(targetId, testType.toScriptType().getValue());
    } else if (taskType.isScenarioTest()) {
      return execResultFacade.scenarioResultByScriptType(targetId,
          testType.toScriptType().getValue());
    } else {
      throw ProtocolException.of("Unsupported task type: " + taskType);
    }
  }

  @NameJoin
  public List<TaskAssocVo> getAssocVos(TaskType taskType, Long targetId) {
    List<TaskInfo> tasks = taskQuery.assocList(taskType, targetId);
    return isEmpty(tasks) ? null : tasks.stream().map(TaskAssembler::toTaskAssocVo)
        .toList();
  }
}
