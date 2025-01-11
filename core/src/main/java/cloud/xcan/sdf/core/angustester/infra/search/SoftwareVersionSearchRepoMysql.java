package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.version.SoftwareVersion;
import cloud.xcan.sdf.core.angustester.domain.version.SoftwareVersionSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class SoftwareVersionSearchRepoMysql extends SimpleSearchRepository<SoftwareVersion>
    implements SoftwareVersionSearchRepo {

}
