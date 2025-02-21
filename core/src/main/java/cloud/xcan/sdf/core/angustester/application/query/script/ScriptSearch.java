package cloud.xcan.sdf.core.angustester.application.query.script;

import cloud.xcan.sdf.api.search.SearchCriteria;
import cloud.xcan.sdf.core.angustester.domain.script.ScriptInfo;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ScriptSearch {

  Page<ScriptInfo> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<ScriptInfo> clz, String... matches);
}
