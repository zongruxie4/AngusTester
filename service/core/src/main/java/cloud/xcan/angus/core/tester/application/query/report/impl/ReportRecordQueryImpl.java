package cloud.xcan.angus.core.tester.application.query.report.impl;

import static cloud.xcan.angus.spec.principal.PrincipalContext.getUserId;
import static java.util.Objects.nonNull;

import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.application.query.project.ProjectMemberQuery;
import cloud.xcan.angus.core.tester.application.query.report.ReportAuthQuery;
import cloud.xcan.angus.core.tester.application.query.report.ReportRecordQuery;
import cloud.xcan.angus.core.tester.domain.report.ReportTemplate;
import cloud.xcan.angus.core.tester.domain.report.record.ReportRecord;
import cloud.xcan.angus.core.tester.domain.report.record.ReportRecordInfo;
import cloud.xcan.angus.core.tester.domain.report.record.ReportRecordInfoRepo;
import cloud.xcan.angus.core.tester.domain.report.record.ReportRecordRepo;
import cloud.xcan.angus.remote.message.http.ResourceNotFound;
import cloud.xcan.angus.spec.annotations.NonNullable;
import jakarta.annotation.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Biz
public class ReportRecordQueryImpl implements ReportRecordQuery {

  @Resource
  private ReportRecordRepo reportRecordRepo;

  @Resource
  private ReportRecordInfoRepo reportRecordInfoRepo;

  @Resource
  private ReportAuthQuery reportAuthQuery;

  @Resource
  private ProjectMemberQuery projectMemberQuery;

  @Override
  public ReportRecord detail(Long id) {
    return new BizTemplate<ReportRecord>() {
      ReportRecord recordDb;

      @Override
      protected void checkParams() {
        recordDb = checkAndFind(id);
        reportAuthQuery.checkViewReportAuth(getUserId(), recordDb.getReportId());
      }

      @Override
      protected ReportRecord process() {
        return recordDb;
      }
    }.execute();
  }

  @Override
  public Page<ReportRecordInfo> find(GenericSpecification<ReportRecordInfo> spec,
      PageRequest pageable) {
    return new BizTemplate<Page<ReportRecordInfo>>() {
      @Override
      protected void checkParams() {
        // Check the project member permission
        projectMemberQuery.checkMember(spec.getCriteria());
      }

      @Override
      protected Page<ReportRecordInfo> process() {
        return reportRecordInfoRepo.findAll(spec, pageable);
      }
    }.execute();
  }

  @Override
  public ReportRecord findContent(@NonNullable Long reportId, Long recordId,
      ReportTemplate template) {
    return new BizTemplate<ReportRecord>() {
      @Override
      protected void checkParams() {
        reportAuthQuery.checkViewReportAuth(getUserId(), reportId);
      }

      @Override
      protected ReportRecord process() {
        ReportRecord recordDb = nonNull(recordId)
            ? reportRecordRepo.findByReportIdAndIdAndTemplate(reportId, recordId, template)
            : reportRecordRepo.findLastByReportIdAndTemplate(reportId, template.getValue());
        ProtocolAssert.assertResourceNotFound(recordDb, reportId, "ReportRecord");
        return recordDb;
      }
    }.execute();
  }

  @Override
  public ReportRecord checkAndFind(Long id) {
    return reportRecordRepo.findById(id)
        .orElseThrow(() -> ResourceNotFound.of(id, "ReportRecord"));
  }
}
