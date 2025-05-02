package cloud.xcan.angus.api.commonlink.exec.result;

import cloud.xcan.angus.spec.annotations.Unused;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Unused
@Setter
@Getter
@Accessors(chain = true)
public class AssertionSummary {

  private int totalNum;
  private long successNum;
  private long failureNum;
  private long ignoreNum;
  private long disabledNum;

}
