package cloud.xcan.sdf.core.angustester.application.converter;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.getUserFullname;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.enums.CreatedAt;
import cloud.xcan.sdf.core.angustester.domain.report.Report;
import cloud.xcan.sdf.core.angustester.domain.report.ReportCategory;
import cloud.xcan.sdf.core.angustester.domain.report.ReportStatus;
import cloud.xcan.sdf.core.angustester.domain.report.ReportTemplate;
import cloud.xcan.sdf.core.angustester.domain.report.record.ReportRecord;
import cloud.xcan.sdf.core.angustester.domain.report.record.content.ReportContent;
import cloud.xcan.sdf.core.angustester.domain.report.setting.BasicInfoSetting;
import cloud.xcan.sdf.core.angustester.domain.setting.ContentFilterSetting;
import cloud.xcan.sdf.core.angustester.domain.setting.ContentSetting;
import cloud.xcan.sdf.core.angustester.domain.setting.TimeSetting;
import java.time.LocalDateTime;
import org.apache.commons.lang3.RandomStringUtils;

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

  public static ReportRecord toGeneratedRecord(Report report, ReportContent reportContent) {
    return new ReportRecord()
        .setProjectId(report.getProjectId()).setReportId(report.getId())
        .setCategory(report.getCategory()).setTemplate(report.getTemplate())
        .setContent(reportContent)
        .setCreatedBy(report.getCreatedBy()).setCreatedDate(LocalDateTime.now());
  }

  public static Report toExampleDomain(Long projectId, String name, ReportCategory category,
      ReportTemplate template, CombinedTargetType targetType, Long targetId) {
    return new Report().setProjectId(projectId)
        .setAuthFlag(false)
        .setName(name)
        .setVersion("V1")
        .setDescription(null)
        .setCategory(category)
        .setTemplate(template)
        .setStatus(ReportStatus.PENDING)
        .setShareToken(RandomStringUtils.randomAlphanumeric(32))
        .setCreateTimeSetting(new TimeSetting()
            .setCreatedAt(CreatedAt.NOW))
        .setBasicInfoSetting(new BasicInfoSetting()
            .setReportContacts(getUserFullname())
            .setReportCopyright("© 2024 xcan.cloud All rights reserved.")
            .setWatermark("AngusTester"))
        .setContentSetting(new ContentSetting()
            .setFilter(new ContentFilterSetting()
                .setTargetType(targetType)
                .setTargetId(targetId))
        );
  }
}
