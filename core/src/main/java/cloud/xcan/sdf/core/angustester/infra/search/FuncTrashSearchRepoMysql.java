package cloud.xcan.sdf.core.angustester.infra.search;

import cloud.xcan.sdf.core.angustester.domain.func.trash.FuncTrash;
import cloud.xcan.sdf.core.angustester.domain.func.trash.FuncTrashSearchRepo;
import cloud.xcan.sdf.core.jpa.repository.SimpleSearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public class FuncTrashSearchRepoMysql extends SimpleSearchRepository<FuncTrash> implements
    FuncTrashSearchRepo {

}
