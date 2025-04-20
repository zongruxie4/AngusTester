package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.data.dataset.Dataset;
import cloud.xcan.angus.core.tester.domain.data.dataset.DatasetSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class DatasetSearchRepoMysql extends SimpleSearchRepository<Dataset>
    implements DatasetSearchRepo {

}
