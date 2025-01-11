package cloud.xcan.sdf.core.angustester.application.query.mock;

import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLog;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLogInfo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

/**
 * @author xiaolong.liu
 */
public interface MockApisLogQuery {

  MockApisLog detail(Long id);

  Page<MockApisLogInfo> list(Long mockServiceId,
      GenericSpecification<MockApisLogInfo> spec, PageRequest pageable);

}

