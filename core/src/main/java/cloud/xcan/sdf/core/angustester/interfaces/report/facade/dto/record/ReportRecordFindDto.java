package cloud.xcan.sdf.core.angustester.interfaces.report.facade.dto.record;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.core.angustester.domain.report.ReportCategory;
import cloud.xcan.sdf.core.angustester.domain.report.ReportTemplate;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ReportRecordFindDto extends PageQuery {

  private Long id;

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  private Long reportId;

  private ReportCategory category;

  private ReportTemplate template;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}
