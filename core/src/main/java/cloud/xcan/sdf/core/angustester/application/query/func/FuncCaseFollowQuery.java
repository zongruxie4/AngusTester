package cloud.xcan.sdf.core.angustester.application.query.func;

import cloud.xcan.sdf.core.angustester.domain.func.follow.FuncCaseFollowP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface FuncCaseFollowQuery {

  Page<FuncCaseFollowP> search(Long projectId, String name, PageRequest pageable);

  Long count(Long projectId);

}




