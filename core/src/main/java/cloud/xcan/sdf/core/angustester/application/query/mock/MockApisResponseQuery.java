package cloud.xcan.sdf.core.angustester.application.query.mock;

import cloud.xcan.sdf.core.angustester.domain.mock.apis.response.MockApisResponse;
import java.util.List;

public interface MockApisResponseQuery {

  List<MockApisResponse> findAllByApisId(Long apisId);

  void checkAddResponseNameExists(List<MockApisResponse> apisResponses);

}
