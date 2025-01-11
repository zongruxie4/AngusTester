package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share;


import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;
import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_PUBLIC_TOKEN_LENGTH;

import cloud.xcan.sdf.api.PageQuery;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@Valid
@Getter
@Setter
@Accessors(chain = true)
public class ApisShareApisSearchPubDto extends PageQuery {

  @Length(max = DEFAULT_NAME_LENGTH)
  private String name;

  @NotNull
  @ApiModelProperty(value = "Share id", required = true)
  private Long sid;

  @NotEmpty
  @Length(max = MAX_PUBLIC_TOKEN_LENGTH)
  @ApiModelProperty(value = "Share public token", required = true)
  private String spt;


  //////////////////////////////////////////////////////////////

  //@NotNull
  //@ApiModelProperty(value = "Target type", required = true)
  //private FuncTargetType targetType;

  //@NotNull
  //@ApiModelProperty(value = "Target(Services/Service/Apis) id", required = true)
  //private Long targetId;

  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}
