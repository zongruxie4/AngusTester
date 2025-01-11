package cloud.xcan.sdf.core.angustester.interfaces.project.facade.dto.trash;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import cloud.xcan.sdf.api.PageQuery;
import io.swagger.annotations.ApiModel;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;


@Setter
@Getter
@ApiModel
public class ProjectTrashSearchDto extends PageQuery {

  @Length(max = DEFAULT_NAME_LENGTH)
  private String targetName;

  private LocalDateTime deletedDate;

}



