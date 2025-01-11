package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.apis.trash.ApisTrash;
import cloud.xcan.sdf.core.angustester.domain.apis.trash.ApisTrashSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ApisTrashSearchRepoMysql extends SimpleSearchRepository<ApisTrash>
    implements ApisTrashSearchRepo {

}
