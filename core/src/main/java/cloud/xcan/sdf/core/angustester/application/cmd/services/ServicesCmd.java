package cloud.xcan.sdf.core.angustester.application.cmd.services;

import cloud.xcan.sdf.extension.angustester.api.ApiImportSource;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.angustester.domain.services.ServicesExportScope;
import cloud.xcan.sdf.core.angustester.domain.services.schema.SchemaFormat;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.io.File;
import java.util.List;
import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

public interface ServicesCmd {

  IdKey<Long, Object> add(Services services);

  void rename(Long id, String name);

  void delete(Long id);

  void delete0(List<Long> ids);

  void clone(Long id);

  void statusUpdate(Long id, ApiStatus status);

  IdKey<Long, Object> sampleImport(Long projectId);

  IdKey<Long, Object> imports(Long projectId, Long serviceId, String projectName,
      ApiImportSource importSource, StrategyWhenDuplicated strategyWhenDuplicated,
      Boolean deleteWhenNotExisted, MultipartFile file, String content);

  File exportProject(ServicesExportScope exportScope, Set<Long> serviceIds, Set<Long> apisId,
      SchemaFormat format, boolean onlyApisComponents);

}
