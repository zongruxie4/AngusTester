package cloud.xcan.sdf.core.angustester.application.query.apis;

import cloud.xcan.sdf.core.angustester.domain.apis.follow.ApisFollowP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ApisFollowQuery {

  Page<ApisFollowP> search(Long projectId, String name, PageRequest pageable);

  Long count(Long projectId);
}




