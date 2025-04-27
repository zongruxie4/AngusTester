package cloud.xcan.angus.core.tester.domain.services.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ServicesAuthRepo extends BaseRepository<ServicesAuth, Long> {

  List<ServicesAuth> findByServiceIdAndAuthObjectIdIn(Long serviceId,
      Collection<Long> authObjectIds);

  List<ServicesAuth> findByServiceIdInAndAuthObjectIdIn(Collection<Long> serviceIds,
      List<Long> orgIds);

  Long countByServiceIdAndAuthObjectIdAndAuthObjectTypeAndCreator(Long serviceId,
      Long authObjectId, AuthObjectType authObjectType, Boolean creator);

  List<ServicesAuth> findAllByAuthObjectIdIn(Collection<Long> authObjectIds);

  long countByServiceIdAndAuthObjectIdAndAuthObjectType(Long pid, Long authObjectId,
      AuthObjectType authObjectType);

  @Modifying
  @Query(value = "DELETE FROM services_auth WHERE service_id in ?1", nativeQuery = true)
  void deleteByServiceIdIn(Collection<Long> serviceIds);

  @Modifying
  @Query(value = "DELETE FROM services_auth WHERE service_id = ?1 AND creator = ?2", nativeQuery = true)
  void deleteByServiceIdAndCreator(Long serviceId, boolean creator);

  @Modifying
  @Query(value = "DELETE FROM services_auth WHERE service_id = ?1 AND auth_object_id =?2 AND creator = ?3", nativeQuery = true)
  void deleteByServiceIdAndAuthObjectIdAndCreator(Long serviceId, Long authObjectId,
      boolean creator);

}
