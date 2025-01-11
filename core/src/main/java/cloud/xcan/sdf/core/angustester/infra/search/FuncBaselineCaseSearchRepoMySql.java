package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineCaseInfoSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class FuncBaselineCaseSearchRepoMySql extends SimpleSearchRepository<FuncBaselineCaseInfo>
    implements FuncBaselineCaseInfoSearchRepo {

}
