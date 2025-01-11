package cloud.xcan.sdf.core.angustester.infra.search;


import cloud.xcan.sdf.core.angustester.domain.apis.share.ApisShare;
import cloud.xcan.sdf.core.angustester.domain.apis.share.ApisShareSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ApisShareSearchRepoMysql extends SimpleSearchRepository<ApisShare>
    implements ApisShareSearchRepo {

}
