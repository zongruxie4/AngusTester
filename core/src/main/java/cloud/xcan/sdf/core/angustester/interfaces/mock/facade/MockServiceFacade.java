package cloud.xcan.sdf.core.angustester.interfaces.mock.facade;

import cloud.xcan.angus.agent.message.mockservice.StartVo;
import cloud.xcan.angus.agent.message.mockservice.StopVo;
import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.MockServiceAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.MockServiceExportDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.MockServiceFileImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.MockServiceFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.MockServiceImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.MockServiceReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.MockServiceSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.MockServiceServicesAssocDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.dto.service.MockServiceUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.MockServiceDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.MockServiceListVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface MockServiceFacade {

  IdKey<Long, Object> add(MockServiceAddDto dto);

  void update(MockServiceUpdateDto dto);

  IdKey<Long, Object> replace(MockServiceReplaceDto dto);

  void instanceSync(Long id);

  IdKey<Long, Object> fileImport(MockServiceFileImportDto dto);

  IdKey<Long, Object> servicesAssoc(MockServiceServicesAssocDto dto);

  void servicesAssocUpdate(Long mockServiceId, Long serviceId);

  List<StartVo> start(HashSet<Long> ids);

  List<StopVo> stop(HashSet<Long> ids);

  void imports(MockServiceImportDto dto);

  IdKey<Long, Object> importExample(Long projectId);

  void importApisExample(Long id);

  void assocDelete(Long id);

  void delete(HashSet<Long> ids, Boolean force);

  Set<Long> assocApisIdsList(Long mockServiceId);

  MockServiceDetailVo detail(Long id);

  PageResult<MockServiceListVo> list(MockServiceFindDto dto);

  PageResult<MockServiceListVo> search(MockServiceSearchDto dto);

  ResponseEntity<Resource> export(MockServiceExportDto dto, HttpServletResponse response);

}
