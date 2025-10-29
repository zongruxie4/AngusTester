package cloud.xcan.angus.core.tester.infra.persistence.postgres.master.review;

import cloud.xcan.angus.core.tester.domain.test.review.FuncReviewRepo;
import org.springframework.stereotype.Repository;

@Repository("funcReviewRepo")
public interface FuncReviewRepoPostgres extends FuncReviewRepo {

}
