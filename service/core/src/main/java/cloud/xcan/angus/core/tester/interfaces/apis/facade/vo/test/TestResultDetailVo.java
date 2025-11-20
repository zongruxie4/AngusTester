package cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.test;

import cloud.xcan.angus.core.tester.domain.exec.result.summary.ExecTestResultSummary;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class TestResultDetailVo {

  private ExecTestResultSummary testResult;

}
