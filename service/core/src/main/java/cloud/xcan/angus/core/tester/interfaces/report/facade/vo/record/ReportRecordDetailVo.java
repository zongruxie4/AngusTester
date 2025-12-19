package cloud.xcan.angus.core.tester.interfaces.report.facade.vo.record;

import cloud.xcan.angus.core.tester.domain.report.ReportCategory;
import cloud.xcan.angus.core.tester.domain.report.ReportStatus;
import cloud.xcan.angus.core.tester.domain.report.ReportTemplate;
import cloud.xcan.angus.remote.NameJoinField;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Setter
@Getter
@Accessors(chain = true)
public class ReportRecordDetailVo {

  private Long id;

  private Long reportId;

  @NameJoinField(id = "reportId", repository = "reportInfoRepo")
  private String reportName;

  private ReportCategory category;

  private ReportTemplate template;

  private Object content;

  private ReportStatus status;

  private String failureMessage;

  private Long createdBy;

  @NameJoinField(id = "createdBy", repository = "commonUserBaseRepo")
  private String creator;

  private LocalDateTime createdDate;

}
