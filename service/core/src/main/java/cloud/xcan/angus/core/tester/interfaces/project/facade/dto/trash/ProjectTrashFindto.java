package cloud.xcan.angus.core.tester.interfaces.project.facade.dto.trash;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;


@Setter
@Getter
public class ProjectTrashFindto extends PageQuery {

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Project name for fuzzy search in trash")
  private String targetName;

  @Schema(description = "Project deletion date for temporal filtering")
  private LocalDateTime deletedDate;

}



