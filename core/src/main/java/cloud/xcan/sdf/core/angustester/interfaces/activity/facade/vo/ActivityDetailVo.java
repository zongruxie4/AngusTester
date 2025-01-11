package cloud.xcan.sdf.core.angustester.interfaces.activity.facade.vo;


import cloud.xcan.sdf.api.NameJoinField;
import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ActivityDetailVo {

  private Long id;

  private Long projectId;

  @NameJoinField(id = "projectId", repository = "projectRepo")
  private String projectName;

  private Long userId;

  private String fullname;

  private String avatar;

  private Long targetId;

  private Long parentTargetId;

  private CombinedTargetType targetType;

  private String targetName;

  private LocalDateTime optDate;

  private String description;

  private String detail;

}



