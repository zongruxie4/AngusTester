package cloud.xcan.sdf.core.angustester.domain.data.variables;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface VariableSearchRepo extends CustomBaseRepository<Variable> {

}
