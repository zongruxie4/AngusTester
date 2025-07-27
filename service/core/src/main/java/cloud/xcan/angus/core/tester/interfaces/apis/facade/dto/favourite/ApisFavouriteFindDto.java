package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.favourite;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;


@Setter
@Getter
@Accessors(chain = true)
public class ApisFavouriteFindDto extends PageQuery {

  @NotNull
  @Schema(description = "Project identifier for favorite API query scope definition", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Favorite API name for fuzzy search and filtering")
  private String apisName;

}



