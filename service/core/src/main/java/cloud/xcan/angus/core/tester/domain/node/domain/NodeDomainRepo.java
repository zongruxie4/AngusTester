package cloud.xcan.angus.core.tester.domain.node.domain;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Optional;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface NodeDomainRepo extends BaseRepository<NodeDomain, Long> {

  long countByName(String name);

  long countByNameAndIdNot(String name, Long id);

  Optional<NodeDomain> findByName(String name);
}
