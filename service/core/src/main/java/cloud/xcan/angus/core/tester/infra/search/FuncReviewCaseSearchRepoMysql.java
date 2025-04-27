package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.func.review.cases.FuncReviewCase;
import cloud.xcan.angus.core.tester.domain.func.review.cases.FuncReviewCaseSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class FuncReviewCaseSearchRepoMysql extends SimpleSearchRepository<FuncReviewCase>
    implements FuncReviewCaseSearchRepo {

}

