package cloud.xcan.sdf.core.angustester.interfaces.task.facade;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.api.angusctrl.exec.vo.result.ExecTestResultDetailVo;
import cloud.xcan.sdf.core.angustester.domain.task.TaskType;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskAssocVo;
import cloud.xcan.sdf.model.script.TestType;
import java.util.List;
import java.util.Map;

public interface TaskTestFacade {

  Map<ScriptType, List<TaskAssocVo>> assocList(TaskType taskType, Long targetId);

  ExecTestResultDetailVo testResult(TaskType taskType, Long targetId, TestType testType);

}
