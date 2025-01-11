package cloud.xcan.sdf.core.angustester.domain.script.tag;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScriptTagRepo extends BaseRepository<ScriptTag, Long> {

  List<ScriptTag> findByScriptId(Long id);

  List<ScriptTag> findByScriptIdIn(Collection<Long> ids);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM script_tag WHERE script_id = ?1", nativeQuery = true)
  void deleteByScriptId(Long id);

  @Transactional
  @Modifying
  @Query(value = "DELETE FROM script_tag WHERE script_id IN ?1", nativeQuery = true)
  void deleteByScriptIdIn(Collection<Long> ids);

}
