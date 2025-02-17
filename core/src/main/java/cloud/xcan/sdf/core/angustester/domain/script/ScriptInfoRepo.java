package cloud.xcan.sdf.core.angustester.domain.script;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.jpa.repository.NameJoinRepository;
import cloud.xcan.sdf.model.script.ScriptSource;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScriptInfoRepo extends BaseRepository<ScriptInfo, Long>,
    NameJoinRepository<ScriptInfo, Long> {

  List<ScriptInfo> findBySourceIdAndSource(Long sourceId, ScriptSource source);

  @Query(value = "SELECT * FROM script WHERE project_id = ?1 AND plugin = ?2 AND type = ?3 LIMIT 1", nativeQuery = true)
  ScriptInfo findTop1ByProjectIdAndPluginAndTypeIn(Long projectId, String plugin, String type);

}
