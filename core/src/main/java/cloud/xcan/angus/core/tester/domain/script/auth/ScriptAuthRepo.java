package cloud.xcan.angus.core.tester.domain.script.auth;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
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
  @Query(value = "DELETE FROM script_auth WHERE script_id = ?1 AND creator = ?2", nativeQuery = true)
  void deleteByScriptIdAndCreator(Long scriptId, Boolean creator);

  @Modifying
  @Query(value = "DELETE FROM script_auth WHERE script_id = ?1 AND auth_object_id in ?2 AND creator = ?3", nativeQuery = true)
  void deleteByScriptIdAndAuthObjectIdInAndCreator(Long scriptId, Collection<Long> creatorIds,
      boolean creator);

}
