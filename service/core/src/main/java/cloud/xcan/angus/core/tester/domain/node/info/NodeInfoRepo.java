package cloud.xcan.angus.core.tester.domain.node.info;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface NodeInfoRepo extends BaseRepository<NodeInfo, Long> {

  @Override
  @Transactional
  NodeInfo save(NodeInfo nodeInfo);

  @Query(value = "SELECT DISTINCT id FROM node_info WHERE id IN ?1", nativeQuery = true)
  List<Long> findIdsByIdIn(Collection<Long> ids);

  @Query(value = "SELECT * FROM node_info WHERE tenant_id = ?1", nativeQuery = true)
  List<NodeInfo> findAllByTenantId(Long tenantId);

  @Query(value = "SELECT * FROM node_info WHERE id NOT IN (SELECT node_id FROM exec_node)", nativeQuery = true)
  List<NodeInfo> findNodeIdsByNotInExec();

  @Query(value = "SELECT ni.* FROM node_info ni, node n WHERE ni.id = n.id AND ni.tenant_id = ?1 AND n.ip = ?2", nativeQuery = true)
  NodeInfo findByTenantIdAndIp(Long tenantId, String ip);

  @Query(value = "SELECT count(*) FROM node_info WHERE tenant_id = ?1", nativeQuery = true)
  long countByTenantId(Long tenantId);

  @Transactional
  @Modifying
  @Query(value = "UPDATE node_info SET agent_installed = 1 WHERE id = ?1", nativeQuery = true)
  void updateInstalled(String id);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM node_info WHERE id IN ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

}
