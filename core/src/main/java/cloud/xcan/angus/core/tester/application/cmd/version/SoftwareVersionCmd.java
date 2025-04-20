package cloud.xcan.angus.core.tester.application.cmd.version;

import cloud.xcan.angus.core.tester.domain.version.SoftwareVersion;
import cloud.xcan.angus.core.tester.domain.version.SoftwareVersionStatus;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;

public interface SoftwareVersionCmd {

  IdKey<Long, Object> add(SoftwareVersion version);

  void update(SoftwareVersion version);

  IdKey<Long, Object> replace(SoftwareVersion version);

  void status(Long id, SoftwareVersionStatus status);

  void merge(Long formId, Long toId);

  void delete(Collection<Long> ids);

}
