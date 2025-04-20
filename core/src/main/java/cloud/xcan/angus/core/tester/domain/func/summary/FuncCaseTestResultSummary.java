package cloud.xcan.angus.core.tester.domain.func.summary;

import cloud.xcan.angus.core.tester.domain.func.cases.CaseTestResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FuncCaseTestResultSummary {

  private CaseTestResult testResult;

  private long total;

}
