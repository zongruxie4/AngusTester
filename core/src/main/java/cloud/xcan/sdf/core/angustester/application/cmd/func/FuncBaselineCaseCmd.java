package cloud.xcan.sdf.core.angustester.application.cmd.func;

import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineCase;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public interface FuncBaselineCaseCmd {

  void add(Long baselineId, HashSet<Long> caseIds);

  void establishBaseline(Set<Long> caseIds, List<FuncBaselineCase> baselineCases);

  void delete(Long baselineId, HashSet<Long> caseIds);

}
