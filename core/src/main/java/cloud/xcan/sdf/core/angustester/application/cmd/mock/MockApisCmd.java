package cloud.xcan.sdf.core.angustester.application.cmd.mock;

import cloud.xcan.sdf.core.angustester.domain.apis.Apis;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApis;
import cloud.xcan.sdf.core.angustester.domain.mock.service.MockService;
import cloud.xcan.sdf.model.remoting.dto.MockApisRequestCountDto.Counter;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface MockApisCmd {

  List<IdKey<Long, Object>> add(List<MockApis> mockApis, boolean saveActivity);

  List<IdKey<Long, Object>> submitModify(List<MockApis> mockApis);

  void add0(List<MockApis> mockApis);

  void update(List<MockApis> mockApis);

  void submitUpdate(List<MockApis> mockApis, List<MockApis> apisDb);

  List<IdKey<Long, Object>> replace(List<MockApis> mockApis);

  IdKey<Long, Object> copyApisAdd(Long apisId, Long mockServiceId);

  IdKey<Long, Object> assocApisAdd(Long apisId, Long mockServiceId);

  void assocApisUpdate(Long mockApisId, Long apisId);

  void assocDelete(HashSet<Long> ids);

  void submitReplace(List<MockApis> updatedApis, List<MockApis> updatedApisDb);

  IdKey<Long, Object> clone(Long id);

  void move(HashSet<Long> ids, Long targetServiceId);

  void instanceSync(Long mockApisId);

  void delete(Collection<Long> ids);

  void submitModify(Collection<Long> ids);

  void counterUpdate(Map<Long, Counter> hasValueApisCounter);

  void addMockApisResponses(MockApis mockApis, Apis apisDb, Long mockServiceId);

  void addImportedMockApisAndResponses(MockService mockServiceDb,
      List<cloud.xcan.angus.model.element.mock.apis.MockApis> angusMockApis);

  void syncAddedApisToServiceInstance(MockService service, List<MockApis> apis);

  void syncAddedApisToServiceInstance0(MockService service, List<MockApis> apis);

  void syncDeletedApisToServiceInstance(MockService service, List<MockApis> apis);

}

