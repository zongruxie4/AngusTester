package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.test;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.api.angusctrl.exec.vo.result.ExecTestResultVo;
import cloud.xcan.sdf.core.angustester.interfaces.task.facade.vo.TaskAssocVo;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class TestResultDetailVo {

  private ExecTestResultVo testResult;

  private Map<ScriptType, List<TaskAssocVo>> assocTasks;

}
