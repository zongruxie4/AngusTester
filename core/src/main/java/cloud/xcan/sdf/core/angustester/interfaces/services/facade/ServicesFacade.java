package cloud.xcan.sdf.core.angustester.interfaces.services.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.core.angustester.interfaces.mock.facade.vo.service.MockServiceDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesExportDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesImportDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.ServiceVo;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.ServicesDetailVo;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface ServicesFacade {

  IdKey<Long, Object> add(ServicesAddDto dto);

  void rename(Long id, String name);

  void delete(Long id);

  void clone(Long id);

  List<IdKey<Long, Object>> exampleImport(Long projectId);

  IdKey<Long, Object> imports(ServicesImportDto dto);

  ResponseEntity<Resource> export(ServicesExportDto dto, HttpServletResponse response);

  void statusUpdate(Long id, ApiStatus status);

  ServicesDetailVo detail(Long id, Boolean joinSchemaFlag);

  MockServiceDetailVo associationMockService(Long id);

  PageResult<ServiceVo> list(ServicesFindDto dto);

  PageResult<ServiceVo> search(ServicesSearchDto dto);

}
