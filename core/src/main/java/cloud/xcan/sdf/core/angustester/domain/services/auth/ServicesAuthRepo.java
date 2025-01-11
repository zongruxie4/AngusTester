package cloud.xcan.sdf.core.angustester.domain.services.auth;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
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

  Long countByServiceIdAndAuthObjectIdAndAuthObjectTypeAndCreatorFlag(Long serviceId,
      Long authObjectId, AuthObjectType authObjectType, Boolean creatorFlag);

  List<ServicesAuth> findAllByAuthObjectIdIn(Collection<Long> authObjectIds);

  long countByServiceIdAndAuthObjectIdAndAuthObjectType(Long pid, Long authObjectId,
      AuthObjectType authObjectType);

  @Modifying
  @Query(value = "DELETE FROM services_auth WHERE service_id in ?1", nativeQuery = true)
  void deleteByServiceIdIn(Collection<Long> serviceIds);

  @Modifying
  @Query(value = "DELETE FROM services_auth WHERE service_id = ?1 AND creator_flag = ?2", nativeQuery = true)
  void deleteByServiceIdAndCreatorFlag(Long serviceId, boolean creatorFlag);

  @Modifying
  @Query(value = "DELETE FROM services_auth WHERE service_id = ?1 AND auth_object_id =?2 AND creator_flag = ?3", nativeQuery = true)
  void deleteByServiceIdAndAuthObjectIdAndCreatorFlag(Long serviceId, Long authObjectId,
      boolean creatorFlag);

}
