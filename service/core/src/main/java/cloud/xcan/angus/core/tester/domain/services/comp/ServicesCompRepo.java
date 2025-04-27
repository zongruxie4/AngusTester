package cloud.xcan.angus.core.tester.domain.services.comp;

import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ServicesCompRepo extends BaseRepository<ServicesComp, Long> {

  List<ServicesComp> findByServiceId(Long serviceId);

  ServicesComp findByServiceIdAndRef(Long serviceId, String ref);

  ServicesComp findByServiceIdAndTypeAndKey(Long serviceId, ServicesCompType type, String key);

  List<ServicesComp> findByServiceIdAndType(Long serviceId, ServicesCompType type);

  List<ServicesComp> findByServiceIdAndTypeAndKeyIn(Long serviceId, ServicesCompType type,
      Collection<String> keys);

  List<ServicesComp> findByServiceIdAndRefIn(Long serviceId, Set<String> refs);

  @Modifying
  @Query(value = "DELETE FROM services_comp WHERE service_id = ?1 AND ref IN ?2", nativeQuery = true)
  void deleteByServiceIdAndRefIn(Long serviceId, Collection<String> refs);

  @Modifying
  @Query(value = "DELETE FROM services_comp WHERE service_id = ?1", nativeQuery = true)
  void deleteByServiceId(Long serviceId);

  @Modifying
  @Query(value = "DELETE FROM services_comp WHERE service_id IN ?1", nativeQuery = true)
  void deleteByServiceIdIn(List<Long> serviceIds);

  @Modifying
  @Query(value = "DELETE FROM services_comp WHERE service_id IN ?1 AND type = ?2", nativeQuery = true)
  void deleteByServiceIdAndType(Long serviceId, String type);

  @Modifying
  @Query(value = "DELETE FROM services_comp WHERE service_id IN ?1 AND type = ?2 AND `key` IN ?3 ", nativeQuery = true)
  void deleteByServiceIdAndTypeAndKey(Long serviceId, String type, Set<String> keys);

}
