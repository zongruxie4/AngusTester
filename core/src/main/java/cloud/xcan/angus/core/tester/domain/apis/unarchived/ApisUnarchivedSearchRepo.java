package cloud.xcan.angus.core.tester.domain.apis.unarchived;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisUnarchivedSearchRepo extends CustomBaseRepository<ApisUnarchived> {

}
