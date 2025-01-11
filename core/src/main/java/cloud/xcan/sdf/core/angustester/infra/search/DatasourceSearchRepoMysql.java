package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.data.datasource.Datasource;
import cloud.xcan.sdf.core.angustester.domain.data.datasource.DatasourceSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class DatasourceSearchRepoMysql extends SimpleSearchRepository<Datasource>
    implements DatasourceSearchRepo {

}
