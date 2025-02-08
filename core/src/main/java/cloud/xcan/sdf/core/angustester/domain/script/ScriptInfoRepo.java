package cloud.xcan.sdf.core.angustester.domain.script;

import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.core.jpa.repository.NameJoinRepository;
import cloud.xcan.sdf.model.script.ScriptSource;
import java.util.List;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface ScriptInfoRepo extends BaseRepository<ScriptInfo, Long>,
    NameJoinRepository<ScriptInfo, Long> {

  List<ScriptInfo> findBySourceIdAndSource(Long sourceId, ScriptSource source);

}
