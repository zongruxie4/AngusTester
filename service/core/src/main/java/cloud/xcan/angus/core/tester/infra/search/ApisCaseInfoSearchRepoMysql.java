package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCaseInfo;
import cloud.xcan.angus.core.tester.domain.apis.cases.ApisCaseInfoSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class ApisCaseInfoSearchRepoMysql extends SimpleSearchRepository<ApisCaseInfo>
    implements ApisCaseInfoSearchRepo {

}
