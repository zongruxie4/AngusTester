package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.data.variables.Variable;
import cloud.xcan.angus.core.tester.domain.data.variables.VariableSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class VariableSearchRepoMysql extends SimpleSearchRepository<Variable>
    implements VariableSearchRepo {

}
