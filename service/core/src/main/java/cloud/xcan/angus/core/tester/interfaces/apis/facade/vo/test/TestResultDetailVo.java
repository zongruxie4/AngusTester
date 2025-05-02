package cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.test;

import cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.result.ExecTestResultVo;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.TaskAssocVo;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class TestResultDetailVo {

  private ExecTestResultVo testResult;

  private Map<ScriptType, List<TaskAssocVo>> assocTasks;

}
