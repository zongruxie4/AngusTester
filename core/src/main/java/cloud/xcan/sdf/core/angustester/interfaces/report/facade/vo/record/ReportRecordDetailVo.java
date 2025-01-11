package cloud.xcan.sdf.core.angustester.interfaces.report.facade.vo.record;

import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.core.angustester.domain.report.ReportCategory;
import cloud.xcan.sdf.core.angustester.domain.report.ReportStatus;
import cloud.xcan.sdf.core.angustester.domain.report.ReportTemplate;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
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
  private String createdByName;

  private LocalDateTime createdDate;

}
