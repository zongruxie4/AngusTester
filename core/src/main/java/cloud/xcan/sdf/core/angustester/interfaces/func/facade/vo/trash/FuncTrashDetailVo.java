package cloud.xcan.sdf.core.angustester.interfaces.func.facade.vo.trash;


import cloud.xcan.sdf.core.angustester.domain.func.FuncTargetType;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import javax.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class FuncTrashDetailVo {

  private Long id;

  private Long projectId;

  private Long targetId;

  private FuncTargetType targetType;

  private String targetName;

  private Long createdBy;

  private String createdByName;

  private String createdByAvatar;

  private Long deletedBy;

  private String deletedByName;

  private String deletedByAvatar;

  private LocalDateTime deletedDate;

}