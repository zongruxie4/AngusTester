package cloud.xcan.sdf.api.angustester.exec.dto;

import cloud.xcan.sdf.api.commonlink.exec.TestCaseResultInfo;
import cloud.xcan.sdf.api.commonlink.exec.TestResultInfo;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class TestResultUpdateDto {

  /**
   * Apis and scenario test results.
   */
  private TestResultInfo testResult;

  /**
   * Apis use case test results
   */
  private List<TestCaseResultInfo> caseResults;

}

