package cloud.xcan.angus.core.tester.application.cmd.report;

import cloud.xcan.angus.core.tester.domain.ExampleDataType;
import cloud.xcan.angus.core.tester.domain.activity.ActivityResource;
import cloud.xcan.angus.core.tester.domain.report.Report;
import cloud.xcan.angus.spec.experimental.IdKey;
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
