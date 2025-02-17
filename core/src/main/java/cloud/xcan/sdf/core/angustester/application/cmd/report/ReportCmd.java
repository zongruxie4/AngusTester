package cloud.xcan.sdf.core.angustester.application.cmd.report;

import cloud.xcan.sdf.core.angustester.domain.ExampleDataType;
import cloud.xcan.sdf.core.angustester.domain.activity.ActivityResource;
import cloud.xcan.sdf.core.angustester.domain.report.Report;
import cloud.xcan.sdf.spec.experimental.IdKey;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ReportCmd {

  IdKey<Long, Object> add(Report report);

  IdKey<Long, Object> add0(Report report, ActivityResource resourceDb);

  void update(Report report);

  IdKey<Long, Object> replace(Report report);

  void generateNow(Long id);

  void generateRecord(Report reportDb);

  List<IdKey<Long, Object>> importExample(Long projectId, Set<ExampleDataType> dataTypes);

  void delete(Collection<Long> ids);

}
