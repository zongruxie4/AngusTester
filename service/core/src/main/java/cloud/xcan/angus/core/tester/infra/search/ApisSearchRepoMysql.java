package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class ApisSearchRepoMysql extends SimpleSearchRepository<Apis>
    implements ApisSearchRepo {

}
