package cloud.xcan.angus.core.tester.domain.mock.apis.response;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author XiaoLong Liu
 */
@NoRepositoryBean
public interface MockApisResponseRepo extends BaseRepository<MockApisResponse, Long> {

  List<MockApisResponse> findAllByMockApisId(Long apisId);

  List<MockApisResponse> findAllByMockApisIdAndTenantId(Long apisId, Long tenantId);

  @Query(value = "SELECT id FROM mock_apis_response WHERE mock_apis_id = ?1", nativeQuery = true)
  List<Long> findIdsByApisId(Long apisId);

  @Query(value = "SELECT name FROM mock_apis_response WHERE mock_apis_id = ?1 AND name IN ?2", nativeQuery = true)
  List<String> findNamesByMockApisIdAndNameIn(Long apisId, Collection<String> names);

  List<MockApisResponse> findAllByMockApisIdIn(Collection<Long> apisIds);

  List<MockApisResponse> findAllByNameIn(List<String> names);

  List<MockApisResponse> findByMockServiceId(Long mockServiceId);

  List<MockApisResponse> findByMockServiceIdAndMockApisIdIn(Long mockServiceId,
      Collection<Long> mockApiIds);

  long countAllByMockApisId(Long apisId);

  @Modifying
  @Transactional
  @Query("UPDATE MockApisResponse s SET s.mockServiceId = ?2 WHERE s.mockApisId IN ?1")
  void updateMockServiceById(HashSet<Long> mockApisIds, Long targetServiceId);

  @Transactional // Required
  @Modifying
  @Query(value = "DELETE FROM mock_apis_response WHERE mock_apis_id IN ?1", nativeQuery = true)
  void deleteAllByMockApisIdIn(Collection<Long> apisIds);

  @Transactional // Required
  @Modifying
  @Query(value = "DELETE FROM mock_apis_response WHERE mock_service_id IN ?1", nativeQuery = true)
  void deleteAllByMockServiceIdIn(Collection<Long> serviceIds);

  @Transactional // Required
  @Modifying
  @Query(value = "DELETE FROM mock_apis_response WHERE id IN ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

  @Transactional // Required
  @Modifying
  @Query(value = "DELETE FROM mock_apis_response WHERE mock_apis_id IN ?1", nativeQuery = true)
  void deleteByMockApisIdIn(Collection<Long> mockApisIds);

  @Transactional // Required
  @Modifying
  void deleteAllByMockApisIdAndIdIn(Long apisId, Collection<Long> responseIds);

}
