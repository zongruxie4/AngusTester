package cloud.xcan.sdf.core.angustester.domain.node.role;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface NodeRoleRepo extends BaseRepository<NodeRole, Long> {

  List<NodeRole> findByNodeIdIn(Collection<Long> ids);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM node_role WHERE node_id IN ?1", nativeQuery = true)
  void deleteByNodeIdIn(Collection<Long> nodeIds);

}
