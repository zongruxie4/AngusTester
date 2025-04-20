package cloud.xcan.angus.core.tester.infra.search;


import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.apis.share.ApisShare;
import cloud.xcan.angus.core.tester.domain.apis.share.ApisShareSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class ApisShareSearchRepoMysql extends SimpleSearchRepository<ApisShare>
    implements ApisShareSearchRepo {

}
