package cloud.xcan.angus.core.tester.interfaces.report.facade.internal.assembler;

import cloud.xcan.angus.core.jpa.criteria.GenericSpecification;
import cloud.xcan.angus.core.jpa.criteria.SearchCriteriaBuilder;
import cloud.xcan.angus.core.tester.domain.report.record.ReportRecord;
import cloud.xcan.angus.core.tester.domain.report.record.ReportRecordInfo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.dto.record.ReportRecordFindDto;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.record.ReportRecordDetailVo;
import cloud.xcan.angus.core.tester.interfaces.report.facade.vo.record.ReportRecordListVo;
import cloud.xcan.angus.remote.search.SearchCriteria;
import java.util.Set;

public class ReportRecordAssembler {

  public static ReportRecordDetailVo toDetailVo(ReportRecord record) {
    return new ReportRecordDetailVo().setId(record.getId())
        .setReportId(record.getReportId())
        .setCategory(record.getCategory())
        .setTemplate(record.getTemplate())
        .setContent(record.getContent())
        .setCreatedBy(record.getCreatedBy())
        .setCreatedDate(record.getCreatedDate());
  }

  public static ReportRecordListVo toListVo(ReportRecordInfo record) {
    return new ReportRecordListVo().setId(record.getId())
        .setReportId(record.getReportId())
        .setCategory(record.getCategory())
        .setTemplate(record.getTemplate())
        .setCreatedBy(record.getCreatedBy())
        .setCreatedDate(record.getCreatedDate());
  }

  public static GenericSpecification<ReportRecordInfo> getSpecification(ReportRecordFindDto dto) {
    // Build the final filters
    Set<SearchCriteria> filters = new SearchCriteriaBuilder<>(dto)
        .rangeSearchFields("id", "createdDate")
        .orderByFields("id", "createdDate", "category", "template")
        .build();
    return new GenericSpecification<>(filters);
  }
}
