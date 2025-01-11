package cloud.xcan.sdf.core.angustester.domain.func.review.cases;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncReviewCaseSearchRepo extends CustomBaseRepository<FuncReviewCase> {

}
