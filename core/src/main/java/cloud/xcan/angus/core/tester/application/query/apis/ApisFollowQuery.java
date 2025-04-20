package cloud.xcan.angus.core.tester.application.query.apis;

import cloud.xcan.angus.core.tester.domain.apis.follow.ApisFollowP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ApisFollowQuery {

  Page<ApisFollowP> search(Long projectId, String name, PageRequest pageable);

  Long count(Long projectId);
}




