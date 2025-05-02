package cloud.xcan.angus.api.commonlink.exec.result;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecTargetResultInfo {

  private Long scriptId;

  private Long scenarioId;

  private Long apisId;

  private String apisName;

  private Long caseId;

  private Long lastExecId;

  private String lastExecName;

  private Boolean passed;

  private String failureMessage;

  private int testNum;

  private int testFailureNum;

  private Long execBy;

  private String execByName;

  private LocalDateTime lastExecDate;

}
