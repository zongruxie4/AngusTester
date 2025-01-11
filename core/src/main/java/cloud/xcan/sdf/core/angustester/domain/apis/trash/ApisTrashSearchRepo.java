package cloud.xcan.sdf.core.angustester.domain.apis.trash;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisTrashSearchRepo extends CustomBaseRepository<ApisTrash> {


}
