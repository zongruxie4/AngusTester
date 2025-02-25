package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.apis.design.ApisDesignInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.design.ApisDesignInfoSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ApisDesignInfoSearchRepoMysql extends SimpleSearchRepository<ApisDesignInfo>
    implements ApisDesignInfoSearchRepo {


}
