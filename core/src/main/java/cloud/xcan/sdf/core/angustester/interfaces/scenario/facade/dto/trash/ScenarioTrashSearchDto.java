package cloud.xcan.sdf.core.angustester.interfaces.scenario.facade.dto.trash;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import cloud.xcan.sdf.api.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;


@Setter
@Getter
@ApiModel
public class ScenarioTrashSearchDto extends PageQuery {

  @NotNull
  @ApiModelProperty(required = true)
  private Long projectId;

  @Length(max = DEFAULT_NAME_LENGTH)
  private String targetName;

  private LocalDateTime deletedDate;

}



