package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.apis.trash.ApisTrash;
import cloud.xcan.angus.core.tester.domain.apis.trash.ApisTrashSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class ApisTrashSearchRepoMysql extends SimpleSearchRepository<ApisTrash>
    implements ApisTrashSearchRepo {

}
