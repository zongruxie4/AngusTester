package cloud.xcan.angus.core.tester.interfaces.func.facade.dto.favourite;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;


@Setter
@Getter
@Accessors(chain = true)
public class FuncCaseFavouriteSearchDto extends PageQuery {

  @Schema(description = "Project id")
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Favourite case name")
  private String caseName;

}



