package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineCaseInfo;
import cloud.xcan.angus.core.tester.domain.test.baseline.FuncBaselineCaseInfoSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class FuncBaselineCaseSearchRepoMySql extends SimpleSearchRepository<FuncBaselineCaseInfo>
    implements FuncBaselineCaseInfoSearchRepo {

}
