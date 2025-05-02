package cloud.xcan.angus.core.tester.interfaces.task.facade;

import cloud.xcan.angus.core.tester.domain.task.TaskType;
import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultDetailSummary;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.TaskAssocVo;
import cloud.xcan.angus.model.script.TestType;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import java.util.List;
import java.util.Map;

public interface TaskTestFacade {

  Map<ScriptType, List<TaskAssocVo>> assocList(TaskType taskType, Long targetId);

  ExecTestResultDetailSummary testResult(TaskType taskType, Long targetId, TestType testType);

}
