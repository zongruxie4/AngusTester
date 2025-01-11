package cloud.xcan.sdf.core.angustester.domain.activity.summary;


import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DEFAULT_DATE_TIME_FORMAT;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ActivitySummary {

  private Long id;

  private Long projectId;

  private Long userId;

  private String fullname;

  private String avatar;

  private Long targetId;

  private Long parentTargetId;

  private CombinedTargetType targetType;

  private String targetName;

  @JsonFormat(pattern = DEFAULT_DATE_TIME_FORMAT)
  private LocalDateTime optDate;

  private String description;

  private String detail;

}



