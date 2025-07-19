package cloud.xcan.angus.core.tester.application.query.mock;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLog;
import cloud.xcan.angus.core.tester.domain.mock.apis.log.MockApisLogInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


public interface MockApisLogQuery {

  MockApisLog detail(Long id);

  Page<MockApisLogInfo> list(Long mockServiceId, GenericSpecification<MockApisLogInfo> spec,
      PageRequest pageable, boolean fullTextSearch, String[] matchSearchFields);

}

