package cloud.xcan.angus.core.tester.infra.search;

import cloud.xcan.angus.core.jpa.repository.SimpleSearchRepository;
import cloud.xcan.angus.core.tester.domain.test.trash.FuncTrash;
import cloud.xcan.angus.core.tester.domain.test.trash.FuncTrashSearchRepo;
import org.springframework.stereotype.Repository;

@Repository
public class FuncTrashSearchRepoMysql extends SimpleSearchRepository<FuncTrash> implements
    FuncTrashSearchRepo {

}
