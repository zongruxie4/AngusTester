package cloud.xcan.angus.core.tester.domain.services.sync;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ServicesSyncRepo extends BaseRepository<ServicesSync, Long> {

  List<ServicesSync> findByServiceIdIn(Collection<Long> serviceIds);

  List<ServicesSync> findByServiceId(Long serviceId);

  List<ServicesSync> findByServiceIdAndNameIn(Long serviceId, Collection<String> names);

  @Query(value = "SELECT count(*) FROM services_sync WHERE service_id = ?1", nativeQuery = true)
  int countByServiceId(Long serviceId);

  @Query(value = "SELECT count(*) FROM services_sync WHERE service_id = ?1 AND name NOT IN ?2", nativeQuery = true)
  int countByServiceIdAndNameInNot(Long serviceId, List<String> names);

  @Query(value = "SELECT name FROM services_sync WHERE service_id = ?1 AND name IN ?2", nativeQuery = true)
  List<String> findNameByServiceIdAndNameIn(Long serviceId, Set<String> names);

  @Query(value = "SELECT name FROM services_sync WHERE service_id = ?1", nativeQuery = true)
  List<String> findNameByServiceId(Long serviceId);

  @Modifying
  @Query(value = "DELETE FROM services_sync WHERE service_id in ?1", nativeQuery = true)
  void deleteByServiceIdIn(Collection<Long> serviceIds);

  @Modifying
  @Query(value = "DELETE FROM services_sync WHERE service_id = ?1 AND name IN ?2", nativeQuery = true)
  void deleteByServiceIdAndNameIn(Long serviceId, List<String> names);

}
