package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineInfo;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineInfoSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class FuncBaselineSearchRepoMySql extends SimpleSearchRepository<FuncBaselineInfo>
    implements FuncBaselineInfoSearchRepo {

}
