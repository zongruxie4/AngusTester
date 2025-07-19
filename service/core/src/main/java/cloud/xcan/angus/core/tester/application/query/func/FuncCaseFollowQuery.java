package cloud.xcan.angus.core.tester.application.query.func;

import cloud.xcan.angus.core.tester.domain.func.follow.FuncCaseFollowP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface FuncCaseFollowQuery {

  Page<FuncCaseFollowP> list(Long projectId, String name, PageRequest pageable);

  Long count(Long projectId);

}




