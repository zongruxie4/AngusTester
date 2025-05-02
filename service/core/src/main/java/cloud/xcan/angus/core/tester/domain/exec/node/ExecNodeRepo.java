package cloud.xcan.angus.core.tester.domain.exec.node;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface ExecNodeRepo extends BaseRepository<ExecNode, Long> {

  List<ExecNode> findByExecId(Long execId);

  List<ExecNode> findByNodeIdIn(Collection<Long> nodeIds);

  @Query(value = "SELECT node_id nodeId FROM exec_node WHERE node_id IN (?1)", nativeQuery = true)
  List<Long> findNodeIdByNodeIdIn(Collection<Long> nodeIds);

  @Query(value = "SELECT node_id nodeId, count(node_id) execNum FROM exec_node WHERE node_id IN (?1) GROUP BY node_id", nativeQuery = true)
  List<NodeExecNum> countByNodeIdIn(Collection<Long> nodeIds);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM exec_node WHERE exec_id = ?1", nativeQuery = true)
  void deleteByExecId(Long execId);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM exec_node WHERE node_id IN ?1", nativeQuery = true)
  void deleteByNodeIdIn(Collection<Long> nodeIds);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM exec_node WHERE exec_id IN (SELECT id FROM exec WHERE `status` IN ('STOPPED','FAILED','COMPLETED','TIMEOUT'))", nativeQuery = true)
  void deleteFinishNode();

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM exec_node WHERE exec_id NOT IN (SELECT id FROM exec)", nativeQuery = true)
  void deleteClearNode();
}
