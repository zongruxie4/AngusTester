package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.favourite;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import cloud.xcan.sdf.api.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ApisFavouriteSearchDto extends PageQuery {

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "Favourite apis name")
  private String apisName;

}



