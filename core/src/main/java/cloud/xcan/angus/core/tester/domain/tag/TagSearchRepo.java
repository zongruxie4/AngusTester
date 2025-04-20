package cloud.xcan.angus.core.tester.domain.tag;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TagSearchRepo extends CustomBaseRepository<Tag> {

}
