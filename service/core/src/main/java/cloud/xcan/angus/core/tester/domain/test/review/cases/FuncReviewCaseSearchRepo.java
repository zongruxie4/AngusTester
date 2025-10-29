package cloud.xcan.angus.core.tester.domain.test.review.cases;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncReviewCaseSearchRepo extends CustomBaseRepository<FuncReviewCase> {

}
