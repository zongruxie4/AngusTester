package cloud.xcan.angus.core.tester.domain.node;

import cloud.xcan.angus.core.jpa.repository.CustomBaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface NodeSearchRepo extends CustomBaseRepository<Node> {


}
