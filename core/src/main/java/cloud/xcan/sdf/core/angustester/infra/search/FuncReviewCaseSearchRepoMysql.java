package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.func.review.cases.FuncReviewCase;
import cloud.xcan.sdf.core.angustester.domain.func.review.cases.FuncReviewCaseSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class FuncReviewCaseSearchRepoMysql extends SimpleSearchRepository<FuncReviewCase>
    implements FuncReviewCaseSearchRepo {

}

