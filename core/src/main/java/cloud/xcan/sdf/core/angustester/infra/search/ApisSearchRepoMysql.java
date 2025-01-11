package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.core.angustester.domain.apis.ApisSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ApisSearchRepoMysql extends SimpleSearchRepository<Apis>
    implements ApisSearchRepo {

}
