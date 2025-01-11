package cloud.xcan.sdf.core.angustester.domain.tag;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface TagSearchRepo extends CustomBaseRepository<Tag> {

}
