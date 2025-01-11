package cloud.xcan.sdf.core.angustester.domain.node;

import cloud.xcan.sdf.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface NodeSearchRepo extends CustomBaseRepository<Node> {


}
