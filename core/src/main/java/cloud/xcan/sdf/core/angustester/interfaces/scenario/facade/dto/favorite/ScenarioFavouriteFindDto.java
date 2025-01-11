package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.favorite;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import cloud.xcan.sdf.api.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Valid
@Setter
@Getter
@ApiModel
public class ScenarioFavouriteFindDto extends PageQuery {

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @Length(max = DEFAULT_NAME_LENGTH)
  private String scenarioName;

}



