package cloud.xcan.angus.core.tester.application.cmd.report;

import cloud.xcan.angus.core.tester.domain.report.record.ReportRecord;
import java.util.Collection;

public interface ReportRecordCmd {

  void add0(ReportRecord record);

  void delete(Collection<Long> ids);

}
