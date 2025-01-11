package cloud.xcan.sdf.core.angustester.infra.persistence.postgres.review;

import cloud.xcan.sdf.core.angustester.domain.func.review.FuncReviewRepo;
import org.springframework.stereotype.Repository;

@Repository("funcReviewRepo")
public interface FuncReviewRepoPostgres extends FuncReviewRepo {

}
