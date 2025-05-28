package cloud.xcan.angus.core.tester.interfaces.activity.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@Accessors(chain = true)
public class ActivitySearchDto extends PageQuery {

  @Schema(description = "Activity id")
  private Long id;

  @Schema(description = "Project id")
  private Long projectId;

  @Schema(description = "Target id")
  private Long targetId;

  @Schema(description = "Target parent id")
  private Long parentTargetId;

  @Schema(description = "Target type")
  private CombinedTargetType targetType;

  @Schema(description = "Operation user id")
  private Long userId;

  @Schema(description = "Main target id")
  private Long mainTargetId;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "For fulltext search")
  private String detail;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime optDate;

  @Override
  public String getDefaultOrderBy() {
    return "optDate";
  }
}
