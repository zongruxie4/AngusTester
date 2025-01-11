package cloud.xcan.sdf.core.angustester.domain.apis.unarchived;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisUnarchivedSearchRepo extends CustomBaseRepository<ApisUnarchived> {

}
