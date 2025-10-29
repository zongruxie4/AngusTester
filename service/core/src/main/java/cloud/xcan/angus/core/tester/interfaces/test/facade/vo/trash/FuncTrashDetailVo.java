package cloud.xcan.angus.core.tester.interfaces.test.facade.vo.trash;


import cloud.xcan.angus.core.tester.domain.test.FuncTargetType;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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
