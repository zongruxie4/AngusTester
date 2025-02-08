package cloud.xcan.sdf.core.angustester.interfaces.apis.facade;

import cloud.xcan.sdf.api.PageResult;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisArchiveDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisExportDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisInfoFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisInfoSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisMoveDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisReplaceDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.ApisUpdateDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.mock.ApisAssocMockApisAddDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.schema.ApisSchemaOpenApiDto;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.ApisDetailVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.ApisInfoListVo;
import cloud.xcan.sdf.core.angustester.interfaces.apis.facade.vo.mock.ApisAssocMockApiVo;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServiceApisFindDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServiceApisScopeDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto.ServicesApisSearchDto;
import cloud.xcan.sdf.core.angustester.interfaces.services.facade.vo.ServicesApisInfoListVo;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.spec.experimental.IdKey;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface ApisFacade {

  List<IdKey<Long, Object>> archive(List<ApisArchiveDto> dtos);

  void update(List<ApisUpdateDto> dto);

  List<IdKey<Long, Object>> replace(List<ApisReplaceDto> dto);

  void rename(Long id, String name);

  void move(ApisMoveDto dto);

  IdKey<Long, Object> clone(Long id);

  void delete(Collection<Long> ids);

  void statusUpdate(Long id, ApiStatus status);

  void serverReplace(Long id, Server server);

  void serverReplaceAll(Long id, List<Server> servers);

  void serverDelete(Long id, Set<String> urls);

  void addParameters(Long serviceId, ServiceApisScopeDto dto, List<Parameter> parameters);

  void updateParameters(Long serviceId, ServiceApisScopeDto dto, List<Parameter> parameters);

  void deleteParameters(Long serviceId, ServiceApisScopeDto dto, List<String> names);

  void enableParameters(Long serviceId, ServiceApisScopeDto dto, List<String> names, Boolean enabled);

  void updateAuth(Long serviceId, ServiceApisScopeDto dto, SecurityScheme authentication);

  void updateServer(Long serviceId, ServiceApisScopeDto dto, Server server);

  void addVariableReference(Long serviceId, ServiceApisScopeDto dto, List<String> variableNames);

  void deleteVariableReference(Long serviceId, ServiceApisScopeDto dto, List<String> variableNames);

  void addDatasetReference(Long serviceId, ServiceApisScopeDto dto, List<String> datasetNames);

  void deleteDatasetReference(Long serviceId, ServiceApisScopeDto dto, List<String> datasetNames);

  List<Server> serverList(Long id);

  ApisAssocMockApiVo assocMockApis(Long id);

  IdKey<Long, Object> assocMockApisAdd(Long id, ApisAssocMockApisAddDto dto);

  ApisDetailVo detail(Long id, Boolean resolveRefFlag);

  String openapiDetail(Long id, ApisSchemaOpenApiDto dto);

  void check(Long id);

  List<ApisDetailVo> listDetail(HashSet<Long> ids, Boolean resolveRefFlag);

  PageResult<ServicesApisInfoListVo> listApis(Long servicesId, ServiceApisFindDto dto);

  PageResult<ServicesApisInfoListVo> searchApis(Long servicesId, ServicesApisSearchDto dto);

  PageResult<ApisInfoListVo> list(ApisInfoFindDto dto);

  PageResult<ApisInfoListVo> search(ApisInfoSearchDto dto);

  ResponseEntity<Resource> export(Long id, ApisExportDto dto, HttpServletResponse response);

}
