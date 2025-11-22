package cloud.xcan.angus.core.tester.application.query.report;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.tester.domain.report.ReportTemplate;
import cloud.xcan.angus.core.tester.domain.report.record.ReportRecord;
import cloud.xcan.angus.core.tester.domain.report.record.ReportRecordInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ReportRecordQuery {

  ReportRecord detail(Long id);

  Page<ReportRecordInfo> find(GenericSpecification<ReportRecordInfo> spec,
      PageRequest pageable);

  ReportRecord findContent(Long reportId, Long recordId, ReportTemplate template);

  ReportRecord checkAndFind(Long id);
}
