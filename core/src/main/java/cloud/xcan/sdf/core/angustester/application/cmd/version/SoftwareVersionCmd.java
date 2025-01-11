package cloud.xcan.sdf.core.angustester.application.cmd.version;

import cloud.xcan.sdf.core.angustester.domain.version.SoftwareVersion;
import cloud.xcan.sdf.core.angustester.domain.version.SoftwareVersionStatus;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;

public interface SoftwareVersionCmd {

  IdKey<Long, Object> add(SoftwareVersion version);

  void update(SoftwareVersion version);

  IdKey<Long, Object> replace(SoftwareVersion version);

  void status(Long id, SoftwareVersionStatus status);

  void merge(Long formId, Long toId);

  void delete(Collection<Long> ids);

}
