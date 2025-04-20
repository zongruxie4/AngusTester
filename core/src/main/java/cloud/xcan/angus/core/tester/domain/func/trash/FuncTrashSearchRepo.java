package cloud.xcan.angus.core.tester.domain.func.trash;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncTrashSearchRepo extends CustomBaseRepository<FuncTrash> {


}
