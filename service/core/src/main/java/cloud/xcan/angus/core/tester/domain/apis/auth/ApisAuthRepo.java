package cloud.xcan.angus.core.tester.domain.apis.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
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

  Long countByApisIdAndAuthObjectIdAndAuthObjectTypeAndCreator(Long apisId, Long authObjectId,
      AuthObjectType authObjectType, Boolean creator);

  Long countByApisIdAndAuthObjectIdAndAuthObjectType(Long apisId, Long authObjectId,
      AuthObjectType authObjectType);

  @Modifying
  @Query(value = "DELETE FROM apis_auth WHERE apis_id in ?1", nativeQuery = true)
  void deleteByApisIdIn(List<Long> apisIds);

  @Modifying
  @Query(value = "DELETE FROM apis_auth WHERE apis_id in ?1 AND creator = ?2", nativeQuery = true)
  void deleteByApisIdInAndCreator(Collection<Long> apisIds, boolean creator);

  @Modifying
  @Query(value = "DELETE FROM apis_auth WHERE apis_id in ?1 AND auth_object_id in ?2 AND creator = ?3", nativeQuery = true)
  void deleteByApisIdInAndAuthObjectIdInAndCreator(Collection<Long> apisIds,
      Collection<Long> authObjectIds, boolean creator);
}
