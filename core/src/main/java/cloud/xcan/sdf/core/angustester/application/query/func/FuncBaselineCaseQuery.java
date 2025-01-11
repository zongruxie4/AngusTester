package cloud.xcan.sdf.core.angustester.application.query.func;

import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineCase;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineCaseInfo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface FuncBaselineCaseQuery {

  List<FuncBaselineCase> detail(Long caseId);

  Page<FuncBaselineCaseInfo> list(Long baselineId,
      GenericSpecification<FuncBaselineCaseInfo> spec, PageRequest pageable);

  List<FuncBaselineCase> checkAndFind(Long caseId);

}
