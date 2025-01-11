package cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal;

import static cloud.xcan.sdf.spec.utils.ObjectUtils.isEmpty;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.api.angusctrl.exec.ExecResultRemote;
import cloud.xcan.sdf.api.angusctrl.exec.vo.result.ExecTestResultDetailVo;
import cloud.xcan.sdf.model.script.TestType;
import cloud.xcan.sdf.api.message.CommProtocolException;
import cloud.xcan.sdf.core.angustester.application.query.task.TaskQuery;
import cloud.xcan.sdf.core.angustester.domain.task.TaskInfo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.TaskTestFacade;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.internal.assembler.TaskAssembler;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskAssocVo;
import cloud.xcan.sdf.core.biz.JoinSupplier;
import cloud.xcan.sdf.core.biz.NameJoin;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class TaskTestFacadeImpl implements TaskTestFacade {

  @Resource
  private TaskQuery taskQuery;

  @Resource
  private ExecResultRemote execResultRemote;

  @Resource
  private JoinSupplier joinSupplier;

  @Override
  public Map<ScriptType, List<TaskAssocVo>> assocList(TaskType taskType, Long targetId) {
    List<TaskAssocVo> assocVos = joinSupplier.execute(() -> getAssocVos(taskType, targetId));
    return isEmpty(assocVos) ? null : assocVos.stream()
        .collect(Collectors.groupingBy(TaskAssocVo::getScriptType));
  }

  @Override
  public ExecTestResultDetailVo testResult(TaskType taskType, Long targetId, TestType testType) {
    if (taskType.isApiTest()) {
      return execResultRemote.apisResultByScriptType(targetId, testType.toScriptType().getValue())
          .orElseContentThrow();
    } else if (taskType.isScenarioTest()) {
      return execResultRemote.scenarioResultByScriptType(targetId,
          testType.toScriptType().getValue()).orElseContentThrow();
    } else {
      throw CommProtocolException.of("Unsupported task type: " + taskType);
    }
  }

  @NameJoin
  public List<TaskAssocVo> getAssocVos(TaskType taskType, Long targetId) {
    List<TaskInfo> tasks = taskQuery.assocList(taskType, targetId);
    return isEmpty(tasks) ? null : tasks.stream().map(TaskAssembler::toTaskAssocVo)
        .collect(Collectors.toList());
  }
}
