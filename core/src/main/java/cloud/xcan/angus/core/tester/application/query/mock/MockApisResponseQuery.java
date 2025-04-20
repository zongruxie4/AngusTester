package cloud.xcan.angus.core.tester.application.query.mock;

import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponse;
import java.util.List;

public interface MockApisResponseQuery {

  List<MockApisResponse> findAllByApisId(Long apisId);

  void checkAddResponseNameExists(List<MockApisResponse> apisResponses);

}
