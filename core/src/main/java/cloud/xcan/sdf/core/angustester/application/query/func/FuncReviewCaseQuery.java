package cloud.xcan.sdf.core.angustester.application.query.func;

import cloud.xcan.sdf.core.angustester.domain.func.cases.FuncCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.review.FuncReview;
import cloud.xcan.sdf.core.angustester.domain.func.review.cases.FuncReviewCase;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface FuncReviewCaseQuery {

  FuncReviewCase detail(Long caseId);

  Page<FuncReviewCase> list(GenericSpecification<FuncReviewCase> spec, PageRequest pageable);

  void checkReviewCaseValid(FuncReview funcReviewDb, List<FuncCaseInfo> funcCasesDb);

  FuncReviewCase checkAndFind(Long id);

  List<FuncReviewCase> checkAndFind(Collection<Long> ids);

  void setCaseInfo(List<FuncReviewCase> reviewCases);

}
