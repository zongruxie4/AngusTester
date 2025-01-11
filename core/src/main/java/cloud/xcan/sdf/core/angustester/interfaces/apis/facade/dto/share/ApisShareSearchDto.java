package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto.share;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.core.angustester.domain.apis.share.ApisShareScope;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import javax.validation.Valid;
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
public class ApisShareSearchDto extends PageQuery {

  @ApiModelProperty(value = "Share id")
  private Long id;

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @Length(max = DEFAULT_NAME_LENGTH)
  @ApiModelProperty(value = "Share name")
  private String name;

  @ApiModelProperty(value = "Share expired date")
  private LocalDateTime expiredDate;

  @ApiModelProperty(value = "Share scope")
  private ApisShareScope shareScope;

  @ApiModelProperty(value = "Share services id")
  private Long servicesId;

  @ApiModelProperty(value = "Share user id")
  private Long createdBy;

  @ApiModelProperty(value = "Share date")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}
