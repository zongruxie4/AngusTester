package cloud.xcan.angus.core.tester.application.query.exec;

import cloud.xcan.angus.core.tester.domain.exec.ExecInfo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExecSearch  {

  Page<ExecInfo> search(Set<SearchCriteria> criteria, Pageable pageable,
      Class<ExecInfo> clz, String... matches);

}
