package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignInfo;
import cloud.xcan.angus.core.tester.domain.apis.design.ApisDesignInfoSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class ApisDesignInfoSearchRepoMysql extends SimpleSearchRepository<ApisDesignInfo>
    implements ApisDesignInfoSearchRepo {


}
