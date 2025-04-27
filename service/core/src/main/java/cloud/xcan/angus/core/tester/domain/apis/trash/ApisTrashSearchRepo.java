package cloud.xcan.angus.core.tester.domain.apis.trash;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisTrashSearchRepo extends CustomBaseRepository<ApisTrash> {


}
