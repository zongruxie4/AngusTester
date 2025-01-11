package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.apis.cases.ApisCaseInfo;
import cloud.xcan.sdf.core.angustester.domain.apis.cases.ApisCaseInfoSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ApisCaseInfoSearchRepoMysql extends SimpleSearchRepository<ApisCaseInfo>
    implements ApisCaseInfoSearchRepo {

}
