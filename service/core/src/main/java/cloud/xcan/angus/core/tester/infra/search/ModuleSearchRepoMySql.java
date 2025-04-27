package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.module.Module;
import cloud.xcan.angus.core.tester.domain.module.ModuleSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class ModuleSearchRepoMySql extends SimpleSearchRepository<Module>
    implements ModuleSearchRepo {

}
