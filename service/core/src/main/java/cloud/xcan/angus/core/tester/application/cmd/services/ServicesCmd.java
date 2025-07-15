package cloud.xcan.angus.core.tester.application.cmd.services;

import cloud.xcan.angus.api.commonlink.apis.StrategyWhenDuplicated;
import cloud.xcan.angus.core.tester.domain.services.Services;
import cloud.xcan.angus.core.tester.domain.services.ServicesExportScope;
import cloud.xcan.angus.core.tester.domain.services.schema.SchemaFormat;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.spec.experimental.IdKey;
import cloud.xcan.angus.spec.locale.SupportedLanguage;
import java.io.File;
import java.util.List;
import java.util.Set;
import org.springframework.web.multipart.MultipartFile;

public interface ServicesCmd {

  IdKey<Long, Object> add(Services services, boolean initSchema);

  void rename(Long id, String name);

  void statusUpdate(Long id, ApiStatus status);

  void clone(Long id);

  IdKey<Long, Object> imports(Long projectId, Long serviceId, String projectName,
      ApiImportSource importSource, StrategyWhenDuplicated strategyWhenDuplicated,
      Boolean deleteWhenNotExisted, MultipartFile file, String content);

  List<IdKey<Long, Object>> importExample(Long projectId);

  File exportProject(ServicesExportScope exportScope, Set<Long> serviceIds, Set<Long> apisId,
      SchemaFormat format, boolean onlyApisComponents);

  void delete(Long id);

  void delete0(List<Long> ids);

}
