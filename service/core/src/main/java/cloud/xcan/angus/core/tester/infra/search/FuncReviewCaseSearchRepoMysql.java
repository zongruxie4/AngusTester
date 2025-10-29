package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.test.review.cases.FuncReviewCase;
import cloud.xcan.angus.core.tester.domain.test.review.cases.FuncReviewCaseSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class FuncReviewCaseSearchRepoMysql extends SimpleSearchRepository<FuncReviewCase>
    implements FuncReviewCaseSearchRepo {

}

