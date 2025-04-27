package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.data.datasource.Datasource;
import cloud.xcan.angus.core.tester.domain.data.datasource.DatasourceSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class DatasourceSearchRepoMysql extends SimpleSearchRepository<Datasource>
    implements DatasourceSearchRepo {

}
