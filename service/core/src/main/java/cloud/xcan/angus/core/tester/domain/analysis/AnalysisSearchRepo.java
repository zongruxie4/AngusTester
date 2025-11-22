package cloud.xcan.angus.core.tester.domain.analysis;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface AnalysisSearchRepo extends CustomBaseRepository<Analysis> {

}
