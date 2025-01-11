package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.trash;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.commonlink.ApisTargetType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@ApiModel
@Setter
@Getter
@Accessors(chain = true)
public class ApisTrashSearchDto extends PageQuery {

  private Long id;

  @NotNull
  @ApiModelProperty(required = true)
  private Long projectId;

  @Length(max = DEFAULT_NAME_LENGTH)
  private String targetName;

  @NotNull
  @ApiModelProperty(required = true)
  private ApisTargetType targetType;

  private LocalDateTime deletedDate;

}



