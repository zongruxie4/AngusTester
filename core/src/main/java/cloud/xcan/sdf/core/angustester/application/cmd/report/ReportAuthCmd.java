package cloud.xcan.sdf.core.angustester.application.cmd.report;

import cloud.xcan.sdf.core.angustester.domain.report.auth.ReportAuth;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.Set;

public interface ReportAuthCmd {

  IdKey<Long, Object> add(ReportAuth auth);

  void replace(ReportAuth auth);

  void enabled(Long reportId, Boolean enabledFlag);

  void delete(Long id);

  void addCreatorAuth(Long id, Set<Long> authCreatorIds);

  void deleteByReportId(Collection<Long> reportId);
}
