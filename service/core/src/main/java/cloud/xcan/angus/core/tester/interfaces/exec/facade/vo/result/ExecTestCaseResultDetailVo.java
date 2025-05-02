package cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.result;

import cloud.xcan.angus.api.commonlink.exec.ExecStatus;
import cloud.xcan.angus.api.commonlink.exec.result.AssertionSummary;
import cloud.xcan.angus.api.commonlink.exec.result.ExecSampleResultContent;
import cloud.xcan.angus.model.element.http.ApisCaseType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecTestCaseResultDetailVo {

  private Long id;

  private Long execId;

  private String execName;

  private ExecStatus execStatus;

  private String plugin;

  private Long scriptId;

  //@Column(name = "service_id") -> Changes will occur after movement
  //private Long serviceId;

  private Long apisId;

  private Long caseId;

  private String caseName;

  private ApisCaseType caseType;

  private Boolean enabled;

  /**
   * Note: A null value means not executed, disabled.
   */
  private Boolean passed;

  private String failureMessage;

  private int testNum;

  private int testFailureNum;

  private AssertionSummary assertionSummary;

  private List<ExecSampleResultContent> sampleContent;

  private Long execBy;

  private String execByName;

  private LocalDateTime lastExecDate;

  private LocalDateTime createdDate;

}
