package cloud.xcan.angus.core.tester.domain.mock.service;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface MockServiceRepo extends BaseRepository<MockService, Long> {

  List<MockService> findAllByNameIn(Collection<String> names);

  Optional<MockService> findByAssocServiceId(Long serviceId);

  @Query(value = "SELECT auth FROM mock_service WHERE id = ?1", nativeQuery = true)
  Boolean findAuthById(Long id);

  @Query(value = "SELECT service_dns_id FROM mock_service WHERE id IN ?1 AND service_dns_id IS NOT null", nativeQuery = true)
  Set<Long> findDnsIdsByIdIn(Collection<Long> ids);

  @Query(value = "SELECT id as id, assoc_service_id as assocServiceId FROM mock_service WHERE assoc_service_id IN ?1", nativeQuery = true)
  List<MockServiceAssocP> findServiceMockIdsMap(Collection<Long> serviceIds);

  long countAllByTenantId(Long tenantId);

  long countByName(String name);

  boolean existsByAssocServiceId(Long serviceId);

  boolean existsByServiceDomainAndNodeIdNot(String serviceDomain, Long nodeId);

  boolean existsByServiceDomainAndServicePort(String serviceDomain, Integer servicePort);

  boolean existsByNodeIdAndServicePort(Long nodeId, int servicePort);

  @Modifying
  @Query(value = "UPDATE mock_service s SET s.auth=?2 WHERE s.id=?1", nativeQuery = true)
  void updateAuthById(Long id, Boolean enabled);

  @Modifying
  @Query(value = "UPDATE mock_service s SET s.assoc_service_id = null WHERE s.assoc_service_id IN ?1", nativeQuery = true)
  void updateAssocServiceIdToNull(Collection<Long> serviceIds);

  @Modifying
  @Query(value = "UPDATE mock_service s SET s.assoc_service_id = null WHERE s.id = ?1", nativeQuery = true)
  void updateAssocServiceIdToNullByMockServiceId(Long id);

  @Modifying
  @Query(value = "UPDATE mock_service s SET s.assoc_service_id = ?2 WHERE s.id = ?1", nativeQuery = true)
  void updateAssocById(Long mockServiceId, Long serviceId);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM mock_service WHERE id IN ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

}
