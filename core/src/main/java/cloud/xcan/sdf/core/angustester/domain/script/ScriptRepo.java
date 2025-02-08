package cloud.xcan.sdf.core.angustester.domain.script;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.model.script.ScriptSource;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScriptRepo extends BaseRepository<Script, Long> {

  List<Script> findBySourceIdAndSource(Long sourceId, ScriptSource source);

  Script findBySourceIdAndSourceAndType(Long sourceTargetId, ScriptSource source,
      ScriptType scriptType);

  @Query(value = "SELECT s.* FROM script s WHERE s.id = ?1", nativeQuery = true)
  Optional<Script> find0ById(long scenarioId);

  @Query(value = "SELECT id FROM script WHERE id IN ?1 AND auth_flag = ?2 ", nativeQuery = true)
  List<Long> findIds0ByIdInAndAuthFlag(Collection<Long> ids, boolean authFlag);

  @Query(value = "SELECT id FROM script WHERE source_id IN ?1 AND source = ?2", nativeQuery = true)
  Set<Long> findIdsBySourceIdInAndSourceAndType(Collection<Long> sourceTargetIds, String source);

  @Query(value = "SELECT id FROM script WHERE source_id IN ?1 AND source = ?2 AND type IN ?3", nativeQuery = true)
  Set<Long> findIdsBySourceIdInAndSourceAndType(Collection<Long> sourceTargetIds, String source,
      Collection<String> testTypes);

  boolean existsBySourceIdAndSourceAndType(Long sourceId, ScriptSource source, ScriptType type);

  boolean existsBySourceIdAndSourceAndTypeAndIdNot(Long sourceId, ScriptSource source,
      ScriptType type, Long id);

  @Modifying
  @Query("UPDATE Script s SET s.authFlag=?2 WHERE s.id=?1")
  void updateAuthFlagById(Long id, Boolean authFlag);

  @Modifying
  @Query(value = "DELETE FROM script WHERE id in ?1", nativeQuery = true)
  void deleteByIdIn(Collection<Long> ids);

}
