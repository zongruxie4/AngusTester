package cloud.xcan.angus.core.tester.domain.mock.apis;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceSource;
import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface MockApisRepo extends BaseRepository<MockApis, Long> {

  List<MockApis> findByMockServiceId(Long serviceId);

  @Query(value = "SELECT summary FROM mock_apis WHERE mock_service_id = ?1 AND summary IN ?2", nativeQuery = true)
  List<String> findSummariesByMockServiceIdAndSummaryIn(Long serviceId,
      Collection<String> summaries);

  @Query(value = "SELECT * FROM mock_apis WHERE mock_service_id = ?1 AND summary IN ?2", nativeQuery = true)
  List<MockApis> findAllByMockServiceIdAndSummaryIn(Long serviceId, Collection<String> summaries);

  @Query(value = "SELECT * FROM mock_apis ma WHERE ma.mock_service_id = ?1 AND ma.endpoint = ?2 AND ma.method = ?3", nativeQuery = true)
  List<MockApis> findByMockServiceIdAndEndpointAndMethod(Long mockServiceId, String endpoint,
      String method);

  List<MockApis> findAllByMockServiceIdAndSource(Long mockServiceId, MockServiceSource source);

  List<MockApis> findByMockServiceIdAndIdIn(Long mockServiceId, Collection<Long> mockApiIds);

  @Query(value = "SELECT id, endpoint, method FROM mock_apis WHERE mock_service_id = ?1", nativeQuery = true)
  List<MockApisOperationP> findAllOperationByMockServiceId(Long mockServiceId);

  @Query(value = "SELECT DISTINCT s.name FROM mock_service s INNER JOIN mock_apis a ON s.id = a.mock_service_id WHERE a.id = ?1", nativeQuery = true)
  String findMockServiceNameById(Long id);

  @Query(value = "SELECT a.mock_service_id AS serviceId, COUNT(a.id) AS num FROM mock_apis  a WHERE a.mock_service_id IN ?1  GROUP BY a.mock_service_id", nativeQuery = true)
  List<Map<String, BigInteger>> countServiceApis(Collection<Long> sIds);

  MockApis findFirstByMockServiceIdAndAssocApisId(Long mockServiceId, Long apisId);

  List<MockApis> findByMockServiceIdAndEndpointIn(Long serviceId, Collection<String> endpoints);

  MockApis findByAssocApisId(Long id);

  @Query(value = "SELECT id FROM mock_apis WHERE mock_service_id = ?1", nativeQuery = true)
  Set<Long> findAllIdsByMockServiceId(Long id);

  @Query(value = "SELECT assoc_apis_id FROM mock_apis WHERE mock_service_id = ?1", nativeQuery = true)
  Set<Long> findAllApisIdsByMockServiceId(Long id);

  @Query(value = "SELECT count(*) FROM mock_apis", nativeQuery = true)
  long countAll();

  @Query(value = "SELECT count(*) FROM mock_apis WHERE mock_service_id = ?1", nativeQuery = true)
  long countAllByMockServiceId(Long mockServiceId);

  @Query(value = "SELECT SUM(request_num) requestNum, SUM(pushback_num) pushbackNum, SUM(simulate_error_num) simulateErrorNum, SUM(success_num) successNum, SUM(exception_num) exceptionNum FROM mock_apis", nativeQuery = true)
  MockApisCount countAllNum();

  @Query(value = "SELECT SUM(request_num) requestNum, SUM(pushback_num) pushbackNum, SUM(simulate_error_num) simulateErrorNum, SUM(success_num) successNum, SUM(exception_num) exceptionNum FROM mock_apis WHERE mock_service_id = ?1", nativeQuery = true)
  MockApisCount countAllNum(Long mockServiceId);

  @Query(value = "SELECT id mockApisId, mock_service_id mockServiceId, assoc_apis_id assocApisId FROM mock_apis WHERE assoc_apis_id IN ?1", nativeQuery = true)
  List<MockApisAssocP> findApisMockIdsMap(Collection<Long> apisIds);

  boolean existsByMockServiceIdAndSummary(Long mockServiceId, String summary);

  boolean existsByAssocApisId(Long id);

  @Modifying
  @Query(value = "UPDATE mock_apis s SET s.assoc_apis_id = null, s.assoc_service_id = null WHERE s.id IN ?1", nativeQuery = true)
  void updateAssocToNullByIdIn(Collection<Long> ids);

  @Modifying
  @Query(value = "UPDATE mock_apis s SET s.assoc_apis_id = null, s.assoc_service_id = null WHERE s.assoc_apis_id IN ?1", nativeQuery = true)
  void updateAssocToNullByApisIdIn(Collection<Long> apiIds);

  @Modifying
  @Query(value = "UPDATE mock_apis s SET s.assoc_apis_id = null, s.assoc_service_id = null WHERE s.assoc_service_id IN ?1", nativeQuery = true)
  void updateServiceIdAndApisIdToNullByServiceIdIn(Collection<Long> serviceIds);

  @Modifying
  @Query(value = "UPDATE mock_apis s SET s.mock_service_id = ?2 WHERE s.id IN ?1", nativeQuery = true)
  void updateMockServiceById(Collection<Long> ids, Long targetServiceId);

  @Modifying
  @Query(value = "UPDATE mock_apis s SET s.assoc_apis_id = null, s.assoc_service_id = null WHERE s.mock_service_id = ?1", nativeQuery = true)
  void updateAssocToNullByByMockServiceId(Long mockServiceId);

  @Transactional
  @Modifying
  @Query(value =
      "UPDATE mock_apis SET request_num = request_num + ?2, pushback_num = pushback_num + ?3, "
          + "simulate_error_num = simulate_error_num + ?4, success_num = success_num + ?5, exception_num = exception_num + ?6 WHERE id = ?1 ", nativeQuery = true)
  void updateCountById(Long id, long requestNum, long pushbackNum, long simulateErrorNum,
      long successNum, long exceptionNum);

  @Transactional
  @Modifying
  @Query(value = "UPDATE mock_apis s SET s.assoc_apis_id = ?2, s.assoc_service_id = ?3 WHERE s.id = ?1", nativeQuery = true)
  void updateAssocById(Long id, Long apisId, Long projectId);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM mock_apis WHERE mock_service_id IN ?1", nativeQuery = true)
  void deleteByMockServiceIdIn(Collection<Long> serviceIds);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM mock_apis WHERE assoc_apis_id IN ?1", nativeQuery = true)
  void deleteByApisIdIn(Collection<Long> apisIds);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM mock_apis WHERE id IN ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

}
