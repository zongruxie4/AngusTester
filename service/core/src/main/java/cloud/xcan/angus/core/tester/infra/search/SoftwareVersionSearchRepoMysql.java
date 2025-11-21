package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.project.version.SoftwareVersion;
import cloud.xcan.angus.core.tester.domain.project.version.SoftwareVersionSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class SoftwareVersionSearchRepoMysql extends SimpleSearchRepository<SoftwareVersion>
    implements SoftwareVersionSearchRepo {

}
