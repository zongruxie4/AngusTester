package cloud.xcan.angus.core.tester.domain.config.node;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.jpa.repository.NameJoinRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface NodeRepo extends BaseRepository<Node, Long>, NameJoinRepository<Node, Long> {

  List<Node> findByIpIn(Collection<String> ips);

  @Query(value = "SELECT * FROM node WHERE expired = 1 AND deleted = 0 and source = ?1 LIMIT ?2", nativeQuery = true)
  List<Node> findExpiredByNotDeletedSource(String source, int count);

  @Query(value = "SELECT * FROM node WHERE sync = 0 AND source = 'ONLINE_BUY' LIMIT ?1", nativeQuery = true)
  List<Node> findPurchuseAndNotSync(int count);

  @Override
  List<Node> findByIdIn(Collection<Long> ids);

  @Query(value = "SELECT n.* FROM node n WHERE n.deleted = 0 AND n.source = 'ONLINE_BUY' AND n.sync = ?1 AND n.created_date <= ?2", nativeQuery = true)
  List<Node> findBySyncAndCreatedDateIsBefore(Boolean sync, LocalDateTime createDate);

  @Query(value = "SELECT * FROM node WHERE sync = 1 AND install_agent is null LIMIT ?1", nativeQuery = true)
  List<Node> findUninstallAgentNodes(int size);

  List<Node> findByTenantIdAndFree(Long ownerTenantId, boolean free);

  @Query(value = "SELECT DISTINCT n.* FROM node n, node_role nr WHERE n.id = nr.node_id AND n.tenant_id = ?1 AND n.free = ?2 AND nr.role = ?3 ", nativeQuery = true)
  List<Node> findByTenantIdAndFreeAndRole(Long ownerTenantId, boolean free, String role);

  @Query(value = "SELECT DISTINCT n.* FROM node n, node_role nr WHERE n.id = nr.node_id AND n.tenant_id = ?1 AND n.free = 0 AND nr.role = ?2 ", nativeQuery = true)
  List<Node> findByTenantIdAndRole(Long ownerTenantId, String value);

  int countByTenantId(Long optTenantId);

  int countByOrderId(Long orderId);

  int countByIdInAndFree(Collection<Long> ids, boolean free);

  @Modifying
  @Query(value = "UPDATE node n SET n.name=?2 WHERE n.id=?1", nativeQuery = true)
  void updateNameById(Long id, String name);

  @Modifying
  @Query(value = "UPDATE node SET expired = 1 WHERE instance_expired_date <= ?1", nativeQuery = true)
  void updateExpired(LocalDateTime date);

  @Modifying
  @Query(value = "UPDATE node SET expired = 0, deleted = 0, instance_expired_date = ?3 WHERE tenant_id = ?1 AND order_id = ?2", nativeQuery = true)
  void updateNotExpired(Long tenantId, Long originalOrderId, LocalDateTime expiredDate);

  @Transactional
  @Modifying
  @Query(value = "UPDATE node SET install_agent = 1 WHERE id = ?1", nativeQuery = true)
  void updateInstalled(String id);

  @Modifying
  @Query(value = "UPDATE node SET deleted = 1 WHERE instance_id IN ?1", nativeQuery = true)
  void deleteByInstanceIdIn(Collection<String> instanceIds);
}
