package cloud.xcan.angus.api.commonlink.exec.result;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecTargetSummary {

  private long totalNum;
  private long disabledNum;

  private long successNum;
  private long failNum;

}
