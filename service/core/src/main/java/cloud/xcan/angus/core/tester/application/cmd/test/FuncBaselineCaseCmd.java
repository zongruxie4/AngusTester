package cloud.xcan.angus.core.tester.application.cmd.test;

import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineCase;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface FuncBaselineCaseCmd {

  void add(Long baselineId, HashSet<Long> caseIds);

  void establishBaseline(Set<Long> caseIds, List<FuncBaselineCase> baselineCases);

  void delete(Long baselineId, HashSet<Long> caseIds);

}
