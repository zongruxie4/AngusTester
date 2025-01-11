package cloud.xcan.sdf.core.angustester.domain.script.auth;

import cloud.xcan.sdf.api.enums.AuthObjectType;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScriptAuthRepo extends BaseRepository<ScriptAuth, Long> {

  List<ScriptAuth> findAllByScriptIdAndAuthObjectIdIn(Long scriptId, List<Long> orgIds);

  List<ScriptAuth> findAllByAuthObjectIdIn(Collection<Long> orgIds);

  List<ScriptAuth> findAllByScriptIdInAndAuthObjectIdIn(Collection<Long> scriptIds,
      Collection<Long> orgIds);

  Long countByScriptIdAndAuthObjectIdAndAuthObjectType(Long scriptId, Long authObjectId,
      AuthObjectType authObjectType);

  @Modifying
  @Query(value = "DELETE FROM script_auth WHERE script_id in ?1", nativeQuery = true)
  void deleteByScriptIdIn(Collection<Long> scriptIds);

  @Modifying
  @Query(value = "DELETE FROM script_auth WHERE script_id = ?1 AND creator_flag = ?2", nativeQuery = true)
  void deleteByScriptIdAndCreatorFlag(Long scriptId, Boolean creatorFlag);

  @Modifying
  @Query(value = "DELETE FROM script_auth WHERE script_id = ?1 AND auth_object_id in ?2 AND creator_flag = ?3", nativeQuery = true)
  void deleteByScriptIdAndAuthObjectIdInAndCreatorFlag(Long scriptId, Collection<Long> creatorIds,
      boolean creatorFlag);

}
