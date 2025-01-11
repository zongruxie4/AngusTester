package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.data.variables.Variable;
import cloud.xcan.sdf.core.angustester.domain.data.variables.VariableSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class VariableSearchRepoMysql extends SimpleSearchRepository<Variable>
    implements VariableSearchRepo {

}
