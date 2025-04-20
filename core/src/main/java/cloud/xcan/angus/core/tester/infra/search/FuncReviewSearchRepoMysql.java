package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.func.review.FuncReview;
import cloud.xcan.angus.core.tester.domain.func.review.FuncReviewSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class FuncReviewSearchRepoMysql extends SimpleSearchRepository<FuncReview> implements
    FuncReviewSearchRepo {

}

