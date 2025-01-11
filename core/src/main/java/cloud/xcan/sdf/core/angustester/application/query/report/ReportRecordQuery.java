package cloud.xcan.sdf.core.angustester.application.query.report;

import cloud.xcan.sdf.core.angustester.domain.report.ReportTemplate;
import cloud.xcan.sdf.core.angustester.domain.report.record.ReportRecord;
import cloud.xcan.sdf.core.angustester.domain.report.record.ReportRecordInfo;
import cloud.xcan.sdf.core.jpa.criteria.GenericSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface ReportRecordQuery {

  ReportRecord detail(Long id);

  Page<ReportRecordInfo> find(GenericSpecification<ReportRecordInfo> spec,
      PageRequest pageable);

  ReportRecord findContent(Long reportId, Long recordId, ReportTemplate template);

  ReportRecord checkAndFind(Long id);
}
