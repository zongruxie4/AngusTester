package cloud.xcan.angus.api.commonlink.exec.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class TestResultCount {

  @Schema(description = "Number of apis or scenarios")
  private long totalNum;

  @Schema(description = "Number of apis or scenarios that enabled the test")
  private long enabledTestNum;

  @Schema(description = "Number of tested apis or scenarios")
  private long testedNum;

  @Schema(description = "Number of untested apis or scenarios")
  private long unTestedNum;

  @Schema(description = "Number of apis or scenarios that passed the test")
  private long testPassedNum;

  @Schema(description = "Number of apis or scenarios that not passed the test")
  private long testUnPassedNum;

  //@Schema(description = "Details of apis test results, including untested apis")
  //private List<ExecResultSummary> apiResults;

}
