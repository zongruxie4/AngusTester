package cloud.xcan.angus.core.tester.application.cmd.report;

import cloud.xcan.angus.core.tester.domain.report.auth.ReportAuth;
import cloud.xcan.angus.spec.experimental.IdKey;
import java.util.Collection;
import java.util.Set;

public interface ReportAuthCmd {

  IdKey<Long, Object> add(ReportAuth auth);

  void replace(ReportAuth auth);

  void enabled(Long reportId, Boolean enabled);

  void delete(Long id);

  void addCreatorAuth(Long id, Set<Long> authCreatorIds);

  void deleteByReportId(Collection<Long> reportId);
}
