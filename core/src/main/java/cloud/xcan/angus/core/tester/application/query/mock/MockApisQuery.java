package cloud.xcan.angus.core.tester.application.query.mock;

import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.apis.ApisBaseInfo;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.core.tester.domain.mock.service.MockServiceInfo;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MockApisQuery {

  MockApis detail(Long id);

  List<MockApis> info0(Long serviceId, String method, String endpoint);

  Page<MockApis> find(GenericSpecification<MockApis> spec, Pageable pageable);

  MockApis checkAndFind(Long id);

  List<MockApis> checkAndFind(Collection<Long> reqServiceIds);

  void checkAddNameExists(Long serviceId, List<MockApis> apis);

  void checkUpdateNameExists(Long serviceId, List<MockApis> apis);

  void checkAddOperationExists(Long serviceId, List<MockApis> apis);

  void checkUpdateOperationExists(Long serviceId, List<MockApis> apis);

  void checkServiceApisQuota(MockService mockServiceDb, int incr);

  void checkAssocApisExists(Apis apisDb);

  void checkAssocApisExists(ApisBaseInfo apisDb);

  void checkAssocApissExists(List<MockApis> mockApis);

  void setSafeCloneName(MockApis apis);

  void setMockServiceInfo(MockApis mockApisDb, MockServiceInfo mockServiceDb);

}




