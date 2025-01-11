package cloud.xcan.sdf.core.angustester.application.converter;

import cloud.xcan.sdf.core.angustester.domain.report.Report;
import cloud.xcan.sdf.core.angustester.domain.report.record.ReportRecord;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.ReportContent;
import cloud.xcan.sdf.core.angustester.domain.setting.TimeSetting;
import java.time.LocalDateTime;

public class ReportConverter {

  public static void setReplaceInfo(Report report, Report reportDb) {
    reportDb.setName(report.getName())
        .setVersion(report.getVersion())
        .setDescription(report.getDescription())
        //.setCategory(report.getCategory())
        //.setTemplate(report.getTemplate())
        //.setStatus(nonNull(dto.getId()) ? null : ReportStatus.PENDING)
        .setCreateTimeSetting(report.getCreateTimeSetting())
        .setBasicInfoSetting(report.getBasicInfoSetting())
        .setContentSetting(report.getContentSetting());

    TimeSetting timeSetting = report.getCreateTimeSetting();
    reportDb.setCreatedAt(timeSetting.getCreatedAt())
        .setNextGenerationDate(timeSetting.getNextDate());
  }

  public static ReportRecord toGeneratedReportRecord(Report report, ReportContent reportContent) {
    return new ReportRecord()
        .setProjectId(report.getProjectId()).setReportId(report.getId())
        .setCategory(report.getCategory()).setTemplate(report.getTemplate())
        .setContent(reportContent)
        .setCreatedBy(report.getCreatedBy()).setCreatedDate(LocalDateTime.now());
  }
}
