package cloud.xcan.angus.api.commonlink.exec;

import cloud.xcan.angus.model.script.ScriptSource;
import cloud.xcan.angus.model.script.configuration.ScriptType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class TestResultInfo {

  private ScriptType scriptType;

  private Long scriptId;

  private ScriptSource scriptSource;

  private Long scriptSourceId;

  private boolean passed;

  private String failureMessage;

  private int testNum;

  private int testFailureNum;

  private Long execId;

  /**
   * For task redundancy fields
   */
  private String execName;

  private Long execBy;

  private LocalDateTime lastExecStartDate;

  private LocalDateTime lastExecEndDate;

}
