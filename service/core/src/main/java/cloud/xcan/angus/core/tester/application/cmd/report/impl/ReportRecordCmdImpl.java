package cloud.xcan.angus.core.tester.application.cmd.report.impl;

import org.springframework.stereotype.Service;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.cmd.CommCmd;
import cloud.xcan.angus.core.jpa.repository.BaseRepository;
import cloud.xcan.angus.core.tester.application.cmd.report.ReportRecordCmd;
import cloud.xcan.angus.core.tester.application.query.report.ReportAuthQuery;
import cloud.xcan.angus.core.tester.domain.report.auth.ReportPermission;
import cloud.xcan.angus.core.tester.domain.report.record.ReportRecord;
import cloud.xcan.angus.core.tester.domain.report.record.ReportRecordRepo;
import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportRecordCmdImpl extends CommCmd<ReportRecord, Long> implements ReportRecordCmd {

  @Resource
  private ReportRecordRepo reportRecordRepo;

  @Resource
  private ReportAuthQuery reportAuthQuery;

  @Override
  public void add0(ReportRecord record) {
    insert0(record);
  }

  @Transactional(rollbackFor = Exception.class)
  @Override
  public void delete(Collection<Long> ids) {
    new BizTemplate<Void>() {
      @Override
      protected void checkParams() {
        Set<Long> reportIds = reportRecordRepo.findReportIdByIdIn(ids);
        ProtocolAssert.assertNotEmpty(reportIds, "Resource not found");
        reportAuthQuery.batchCheckPermission(reportIds, ReportPermission.DELETE);
      }

      @Override
      protected Void process() {
        reportRecordRepo.deleteByIdIn(ids);
        return null;
      }
    }.execute();
  }

  @Override
  protected BaseRepository<ReportRecord, Long> getRepository() {
    return reportRecordRepo;
  }

}
