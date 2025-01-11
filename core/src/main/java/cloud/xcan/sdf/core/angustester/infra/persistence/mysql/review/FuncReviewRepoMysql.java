package cloud.xcan.sdf.core.angustester.infra.persistence.mysql.review;

import cloud.xcan.sdf.core.angustester.domain.func.review.FuncReviewRepo;
import org.springframework.stereotype.Repository;

@Repository("funcReviewRepo")
public interface FuncReviewRepoMysql extends FuncReviewRepo {

}
