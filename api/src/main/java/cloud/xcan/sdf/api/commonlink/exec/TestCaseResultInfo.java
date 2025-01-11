package cloud.xcan.sdf.api.commonlink.exec;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class TestCaseResultInfo {

  //private ScriptSource scriptSource = ScriptSource.APIS;

  private Long caseId;

  /**
   * Note: A null value means not executed, disabled.
   */
  private Boolean passed;

  private String failureMessage;

  private int testNum;

  private int testFailureNum;

  private Long execId;

  /**
   * For task redundancy fields
   */
  private String execName;

  private Long execBy;

  private LocalDateTime lastExecDate;

}