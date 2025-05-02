package cloud.xcan.angus.core.tester.domain.exec.result.summary;

import cloud.xcan.angus.api.commonlink.exec.result.TestResultSummary;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecTestResultSummary {

  /**
   * A null value means not tested, no test results.
   */
  private Boolean passed;

  private TestResultSummary resultSummary;

  private List<ScriptType> enabledTestTypes;

  private Map<ScriptType, ExecTestResultDetailSummary> resultDetailVoMap;

}
