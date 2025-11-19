package cloud.xcan.angus.core.tester.domain.test.cases;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncCaseScenarioRepo extends BaseRepository<FuncCaseScenario, Long> {

}
