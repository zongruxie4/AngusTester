package cloud.xcan.angus.core.tester.infra.persistence.postgres.review;

import cloud.xcan.angus.core.tester.domain.func.review.FuncReviewRepo;
import org.springframework.stereotype.Repository;

@Repository("funcReviewRepo")
public interface FuncReviewRepoPostgres extends FuncReviewRepo {

}
