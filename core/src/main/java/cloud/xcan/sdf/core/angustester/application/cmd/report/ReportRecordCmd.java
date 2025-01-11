package cloud.xcan.sdf.core.angustester.application.cmd.report;

import cloud.xcan.sdf.core.angustester.domain.report.record.ReportRecord;
import java.util.Collection;

public interface ReportRecordCmd {

  void add0(ReportRecord record);

  void delete(Collection<Long> ids);

}
