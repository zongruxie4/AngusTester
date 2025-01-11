package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineInfo;
import cloud.xcan.sdf.core.angustester.domain.func.baseline.FuncBaselineInfoSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class FuncBaselineSearchRepoMySql extends SimpleSearchRepository<FuncBaselineInfo>
    implements FuncBaselineInfoSearchRepo {

}
