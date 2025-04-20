package cloud.xcan.angus.core.tester.application.cmd.mock;

import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.HashSet;
import java.util.List;

public interface MockApisResponseCmd {

  List<IdKey<Long, Object>> add(Long apisId, List<MockApisResponse> apisResponses);

  List<IdKey<Long, Object>> submitModify(MockApis apisDb, List<MockApisResponse> apisResponses);

  void add0(List<MockApisResponse> apisResponses);

  void replace(Long apisId, List<MockApisResponse> responses);

  void submitModify(Long apisId, MockApis apisDb, List<MockApisResponse> responses);

  void delete(Long apisId, HashSet<Long> responseIds);

  void submitModify(Long apisId, HashSet<Long> responseIds);
}
