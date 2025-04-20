package cloud.xcan.angus.core.tester.interfaces.project.facade.dto.trash;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.remote.PageQuery;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;


@Setter
@Getter

public class ProjectTrashSearchDto extends PageQuery {

  @Length(max = MAX_NAME_LENGTH)
  private String targetName;

  private LocalDateTime deletedDate;

}



