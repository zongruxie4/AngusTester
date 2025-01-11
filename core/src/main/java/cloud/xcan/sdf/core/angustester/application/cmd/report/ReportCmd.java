package cloud.xcan.sdf.core.angustester.application.cmd.report;

import cloud.xcan.sdf.core.angustester.domain.report.Report;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;

public interface ReportCmd {

  IdKey<Long, Object> add(Report report);

  void update(Report report);

  IdKey<Long, Object> replace(Report report);

  void generateNow(Long id);

  void generateRecord(Report reportDb);

  void delete(Collection<Long> ids);

}
