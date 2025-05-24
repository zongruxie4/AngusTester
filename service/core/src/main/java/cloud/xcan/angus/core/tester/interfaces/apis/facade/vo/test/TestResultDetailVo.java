package cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.test;

import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultSummary;
import cloud.xcan.angus.core.tester.interfaces.task.facade.vo.TaskAssocVo;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class TestResultDetailVo {

  private ExecTestResultSummary testResult;

  private Map<ScriptType, List<TaskAssocVo>> assocTasks;

}
