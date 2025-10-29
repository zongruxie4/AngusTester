package cloud.xcan.angus.core.tester.domain.test.review;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncReviewSearchRepo extends CustomBaseRepository<FuncReview> {


}
