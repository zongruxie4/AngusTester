package cloud.xcan.angus.core.tester.domain.test.trash;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncTrashSearchRepo extends CustomBaseRepository<FuncTrash> {


}
