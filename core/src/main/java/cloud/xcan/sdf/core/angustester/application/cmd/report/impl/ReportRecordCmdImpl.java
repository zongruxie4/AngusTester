package cloud.xcan.sdf.core.angustester.application.cmd.report.impl;

import cloud.xcan.sdf.core.angustester.application.cmd.report.ReportRecordCmd;
import cloud.xcan.sdf.core.angustester.application.query.report.ReportAuthQuery;
import cloud.xcan.sdf.core.angustester.domain.report.auth.ReportPermission;
import cloud.xcan.sdf.core.angustester.domain.report.record.ReportRecord;
import cloud.xcan.sdf.core.angustester.domain.report.record.ReportRecordRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.BizTemplate;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

@Biz
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
