package cloud.xcan.angus.core.tester.domain.apis;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import io.swagger.v3.oas.models.servers.Server;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisRepo extends BaseRepository<Apis, Long> {

  List<Apis> findByServiceId(Long serviceId);

  Page<Apis> findByServiceId(Long serviceId, Pageable pageable);

  @Query(value = "SELECT service_id FROM apis WHERE id in ?1 AND deleted = 0 AND service_deleted = 0", nativeQuery = true)
  Set<Long> findAllServiceIdByIdIn(Collection<Long> ids);

  @Query(value = "SELECT id FROM apis WHERE service_id in ?1", nativeQuery = true)
  List<Long> findAll0IdByServiceIdIn(List<Long> serviceIds);

  @Query(value = "SELECT id FROM apis WHERE service_id in ?1 AND deleted = 0 AND service_deleted = 0", nativeQuery = true)
  List<Long> findAllIdByServiceIdIn(List<Long> serviceIds);

  @Query(value = "SELECT id FROM apis WHERE service_id = ?1 AND id IN ?2 AND deleted = 0 AND service_deleted = 0", nativeQuery = true)
  List<Long> findAllIdByServiceIdAndIdIn(Long serviceId, Collection<Long> apisIds);

  @Query(value = "SELECT * FROM apis WHERE service_id = ?1 AND id IN ?2 AND deleted = 0 AND service_deleted = 0", nativeQuery = true)
  List<Apis> findAllByServiceIdAndIdIn(Long serviceId, Collection<Long> apisIds);

  long countByServiceId(Long serviceId);

  List<Apis> findAllByIdInAndServiceIdIn(Collection<Long> ids, Collection<Long> serviceIds);

  @Query(value = "SELECT a.service_id AS serviceId, COUNT(a.id) AS num FROM apis a WHERE a.service_id IN ?1  GROUP BY a.service_id", nativeQuery = true)
  List<Map<String, Long>> countServiceApis(Set<Long> pIds);

  @Query(value = "SELECT count(*) FROM apis WHERE tenant_id = ?1", nativeQuery = true)
  long countByTenantId(Long tenantId);

  @Query(value = "SELECT DISTINCT service_id FROM apis WHERE service_id IN ?1 AND deleted = 0", nativeQuery = true)
  List<Long> findServiceIdByServiceIdIn(Collection<Long> serviceIds);

  //@Query(value = "SELECT count(*) FROM apis WHERE summary = ?1 limit 1", nativeQuery = true) <- Fix:: class java.math.BigInteger cannot be cast to class java.lang.Boolean
  boolean existsByServiceIdAndSummary(Long serviceId, String summary);

  //@Query(value = "SELECT count(*) FROM apis WHERE operationId = ?1 limit 1", nativeQuery = true) <- Fix:: class java.math.BigInteger cannot be cast to class java.lang.Boolean
  boolean existsByOperationId(String operationId);

  @Modifying
  @Query(value = "UPDATE apis a SET a.auth=?2 WHERE a.id=?1", nativeQuery = true)
  void updateAuthById(Long id, Boolean enabled);

  @Modifying
  @Query(value = "UPDATE apis a SET a.service_auth=?2 WHERE a.service_id=?1", nativeQuery = true)
  void updateServiceAuthByServiceId(Long serviceId, Boolean enabled);

  @Modifying
  @Query(value = "UPDATE apis a SET a.status=?2 WHERE a.id=?1", nativeQuery = true)
  void updateStatusById(Long id, String status);

  @Modifying
  @Query(value = "UPDATE apis a SET a.status=?2 WHERE a.service_id=?1", nativeQuery = true)
  void updateStatusByServiceId(Long serviceId, String status);

  @Modifying
  @Query(value = "UPDATE apis a SET a.deleted=?2, a.deleted_by =?3, a.deleted_date = ?4 WHERE a.id in ?1", nativeQuery = true)
  void updateDeleteStatus(Collection<Long> ids, Boolean deleted, Long deletedBy,
      LocalDateTime deletedDate);

  @Modifying
  @Query(value = "UPDATE apis a SET a.service_deleted=?2 WHERE a.service_id in ?1", nativeQuery = true)
  void updateServiceDeleteStatusByService(Collection<Long> serviceIds, boolean deleted);

  @Modifying
  @Query(value = "UPDATE apis a SET a.deleted = false, a.deleted_by = -1, a.deleted_date = null WHERE a.id in ?1", nativeQuery = true)
  void updateToUndeletedStatusByIdIn(Collection<Long> ids);

  @Modifying
  @Query(value = "UPDATE apis a SET a.summary=?2 WHERE a.id = ?1", nativeQuery = true)
  void updateSummaryById(Long id, String summary);

  @Modifying
  @Query(value = "UPDATE Apis a SET a.currentServer=?2 WHERE a.currentServerId = ?1")
  void updateCurrentServerById(Long currentServerId, Server server);

  @Modifying
  @Query(value = "UPDATE apis a SET a.service_id =?2 WHERE a.id in ?1", nativeQuery = true)
  void updateServiceById(Collection<Long> ids, Long serviceId);

  @Modifying
  @Query(value = "UPDATE apis a SET a.test_func =?2 WHERE a.service_id = ?1", nativeQuery = true)
  void updateTestFuncByService(Long serviceId, Boolean enabled);

  @Modifying
  @Query(value = "UPDATE apis a SET a.test_perf =?2 WHERE a.service_id = ?1", nativeQuery = true)
  void updateTestPerfByService(Long serviceId, Boolean enabled);

  @Modifying
  @Query(value = "UPDATE apis a SET a.test_stability =?2 WHERE a.service_id = ?1", nativeQuery = true)
  void updateTestStabilityByService(Long serviceId, Boolean enabled);

  @Modifying
  @Query(value = "DELETE FROM apis WHERE id in ?1", nativeQuery = true)
  void deleteAllByIdIn(List<Long> ids);

}
