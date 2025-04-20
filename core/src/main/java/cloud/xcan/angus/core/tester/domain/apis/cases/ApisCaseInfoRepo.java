package cloud.xcan.angus.core.tester.domain.apis.cases;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.model.element.http.ApisCaseType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisCaseInfoRepo extends BaseRepository<ApisCaseInfo, Long> {

  @Query(value = "SELECT * FROM apis_case WHERE name IN ?1 AND apis_id = ?2", nativeQuery = true)
  Collection<ApisCaseInfo> findAllByNameInAndApisId(Collection<String> names, Long apisId);

  @Query(value = "SELECT * FROM apis_case WHERE id IN ?1", nativeQuery = true)
  List<ApisCaseInfo> findAll0ByIdIn(Collection<Long> ids);

  int countByApisIdIn(Collection<Long> apiIds);

  boolean existsByApisIdAndType(Long apisId, ApisCaseType type);

  @Query(value = "SELECT id FROM apis_case WHERE services_id = ?1 ", nativeQuery = true)
  List<Long> findIdByServiceId(Long serviceId);

}
