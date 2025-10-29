package cloud.xcan.angus.core.tester.interfaces.test.facade.dto.favourite;

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
public class FuncCaseFavouriteFindDto extends PageQuery {

  @Schema(description = "Project identifier for favorite case scope filtering")
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Favorite test case name for fuzzy search and filtering")
  private String caseName;

}



