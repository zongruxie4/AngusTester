package cloud.xcan.angus.core.tester.infra.persistence.mysql.review;

import cloud.xcan.angus.core.tester.domain.func.review.FuncReviewRepo;
import org.springframework.stereotype.Repository;

@Repository("funcReviewRepo")
public interface FuncReviewRepoMysql extends FuncReviewRepo {

}
