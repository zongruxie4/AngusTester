package cloud.xcan.angus.core.tester.domain.apis.cases;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.model.element.http.ApisCaseType;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisCaseRepo extends BaseRepository<ApisCase, Long> {

  @Query(value = "SELECT name FROM apis_case WHERE name IN ?1 AND apis_id = ?2", nativeQuery = true)
  List<String> findNamesByNameInAndApisId(Set<String> names, Long apisId);

  int countByApisIdAndNameAndIdNot(Long apisId, String name, Long id);

  boolean existsByApisIdAndName(Long apisId, String name);

  int countByApisId(Long apisId);

  List<ApisCase> findByApisId(Long apisId);

  List<ApisCase> findByServicesIdAndType(Long servicesId, ApisCaseType caseType);

  Long countByServicesId(Long servicesId);

  @Modifying
  @Query(value = "DELETE FROM apis_case WHERE id IN ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

  @Modifying
  @Query(value = "DELETE FROM apis_case WHERE apis_id IN ?1", nativeQuery = true)
  void deleteByApisIdIn(List<Long> apiIds);


}
