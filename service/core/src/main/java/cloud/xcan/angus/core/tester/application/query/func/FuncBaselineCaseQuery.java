package cloud.xcan.angus.core.tester.application.query.func;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaselineCase;
import cloud.xcan.angus.core.tester.domain.func.baseline.FuncBaselineCaseInfo;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface FuncBaselineCaseQuery {

  List<FuncBaselineCase> detail(Long caseId);

  Page<FuncBaselineCaseInfo> list(Long baselineId,
      GenericSpecification<FuncBaselineCaseInfo> spec, PageRequest pageable);

  List<FuncBaselineCase> checkAndFind(Long caseId);

}
