package cloud.xcan.sdf.core.angustester.domain.apis.auth;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ApisAuthRepo extends BaseRepository<ApisAuth, Long> {

  List<ApisAuth> findAllByApisIdAndAuthObjectIdIn(Long apisId, List<Long> orgIds);

  List<ApisAuth> findAllByAuthObjectIdIn(Collection<Long> orgIds);

  List<ApisAuth> findAllByApisIdInAndAuthObjectIdIn(Collection<Long> appIds,
      Collection<Long> orgIds);

  Long countByApisIdAndAuthObjectIdAndAuthObjectTypeAndCreatorFlag(Long apisId, Long authObjectId,
      AuthObjectType authObjectType, Boolean creatorFlag);

  Long countByApisIdAndAuthObjectIdAndAuthObjectType(Long apisId, Long authObjectId,
      AuthObjectType authObjectType);

  @Modifying
  @Query(value = "DELETE FROM apis_auth WHERE apis_id in ?1", nativeQuery = true)
  void deleteByApisIdIn(List<Long> apisIds);

  @Modifying
  @Query(value = "DELETE FROM apis_auth WHERE apis_id in ?1 AND creator_flag = ?2", nativeQuery = true)
  void deleteByApisIdInAndCreatorFlag(Collection<Long> apisIds, boolean creatorFlag);

  @Modifying
  @Query(value = "DELETE FROM apis_auth WHERE apis_id in ?1 AND auth_object_id in ?2 AND creator_flag = ?3", nativeQuery = true)
  void deleteByApisIdInAndAuthObjectIdInAndCreatorFlag(Collection<Long> apisIds,
      Collection<Long> authObjectIds, boolean creatorFlag);
}
