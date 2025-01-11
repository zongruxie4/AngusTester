package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.func.review.FuncReview;
import cloud.xcan.sdf.core.angustester.domain.func.review.FuncReviewSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class FuncReviewSearchRepoMysql extends SimpleSearchRepository<FuncReview> implements
    FuncReviewSearchRepo {

}

