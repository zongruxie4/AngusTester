package cloud.xcan.sdf.core.angustester.application.cmd.apis;

import cloud.xcan.sdf.core.angustester.domain.apis.design.ApisDesign;
import cloud.xcan.sdf.core.angustester.domain.services.schema.SchemaFormat;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import org.springframework.web.multipart.MultipartFile;

public interface ApisDesignCmd {

  IdKey<Long, Object> add(ApisDesign design);

  IdKey<Long, Object> add0(ApisDesign design);

  void updateName(Long id, String name);

  void replaceContent(Long id, String openapi);

  void release(Long id);

  IdKey<Long, Object> clone(Long id);

  void servicesGenerate(Long id);

  IdKey<Long, Object> imports(Long projectId, String name, String content, MultipartFile file);

  void delete(HashSet<Long> ids);

  File export(Long id, SchemaFormat format);

  void deleteByServiceIdIn(Collection<Long> serviceIds);
}
