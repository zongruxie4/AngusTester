package cloud.xcan.sdf.core.angustester.domain.node.domain;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface NodeDomainRepo extends BaseRepository<NodeDomain, Long> {

  long countByName(String name);

  long countByNameAndIdNot(String name, Long id);

  Optional<NodeDomain> findByName(String name);
}
