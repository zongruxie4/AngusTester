package cloud.xcan.sdf.core.angustester.domain.node;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.jpa.repository.NameJoinRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface NodeRepo extends BaseRepository<Node, Long>, NameJoinRepository<Node, Long> {

  List<Node> findByIpIn(Collection<String> ips);

  @Query(value = "SELECT * FROM node WHERE expired_flag = 1 AND deleted_flag = 0 and source = ?1 LIMIT ?2", nativeQuery = true)
  List<Node> findExpiredByNotDeletedSource(String source, int count);

  @Query(value = "SELECT * FROM node WHERE sync_flag = 0 AND source = 'ONLINE_BUY' LIMIT ?1", nativeQuery = true)
  List<Node> findPurchuseAndNotSync(int count);

  @Override
  List<Node> findByIdIn(Collection<Long> ids);

  @Query(value = "SELECT n.* FROM node n WHERE n.deleted_flag = 0 AND n.source = 'ONLINE_BUY' AND n.sync_flag = ?1 AND n.created_date <= ?2", nativeQuery = true)
  List<Node> findBySyncFlagAndCreatedDateIsBefore(Boolean syncFlag, LocalDateTime createDate);

  @Query(value = "SELECT * FROM node WHERE sync_flag = 1 AND install_agent_flag is null LIMIT ?1", nativeQuery = true)
  List<Node> findUninstallAgentNodes(int size);

  List<Node> findByTenantIdAndFreeFlag(Long ownerTenantId, boolean freeFlag);

  @Query(value = "SELECT n.* FROM node n, node_role nr WHERE n.id = nr.node_id AND n.tenant_id = ?1 AND n.free_flag = ?2 AND nr.role = ?3 ", nativeQuery = true)
  List<Node> findByTenantIdAndFreeFlagAndRole(Long ownerTenantId, boolean freeFlag, String role);

  int countByTenantId(Long optTenantId);

  int countByOrderId(Long orderId);

  @Modifying
  @Query(value = "UPDATE node n SET n.name=?2 WHERE n.id=?1", nativeQuery = true)
  void updateNameById(Long id, String name);

  @Modifying
  @Query(value = "UPDATE node SET expired_flag = 1 WHERE instance_expired_date <= ?1", nativeQuery = true)
  void updateExpired(LocalDateTime date);

  @Modifying
  @Query(value = "UPDATE node SET expired_flag = 0, deleted_flag = 0, instance_expired_date = ?3 WHERE tenant_id = ?1 AND order_id = ?2", nativeQuery = true)
  void updateNotExpired(Long tenantId, Long originalOrderId, LocalDateTime expiredDate);

  @Modifying
  @Query(value = "UPDATE node SET deleted_flag = 1 WHERE instance_id IN ?1", nativeQuery = true)
  void deleteByInstanceIdIn(Collection<String> instanceIds);

}
