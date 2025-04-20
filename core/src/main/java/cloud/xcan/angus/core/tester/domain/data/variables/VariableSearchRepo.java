package cloud.xcan.angus.core.tester.domain.data.variables;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface VariableSearchRepo extends CustomBaseRepository<Variable> {

}
