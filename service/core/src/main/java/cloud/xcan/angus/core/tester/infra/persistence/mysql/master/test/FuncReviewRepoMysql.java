package cloud.xcan.angus.core.tester.infra.persistence.mysql.master.test;

import cloud.xcan.angus.core.tester.domain.test.review.FuncReviewRepo;
import org.springframework.stereotype.Repository;

@Repository("funcReviewRepo")
public interface FuncReviewRepoMysql extends FuncReviewRepo {

}
