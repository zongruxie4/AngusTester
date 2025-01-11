package cloud.xcan.sdf.core.angustester.domain.func.trash;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface FuncTrashSearchRepo extends CustomBaseRepository<FuncTrash> {


}
