package cloud.xcan.angus.core.tester.application.query.script;

import cloud.xcan.angus.core.tester.domain.script.ScriptInfo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ScriptSearch {

  Page<ScriptInfo> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<ScriptInfo> clz, String... matches);
}
