package cloud.xcan.sdf.core.angustester.interfaces.apis.facade.dto;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_CODE_LENGTH_X5;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_URL_LENGTH_X2;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.commonlink.apis.ApiPermission;
import cloud.xcan.sdf.model.apis.ApiStatus;
import cloud.xcan.sdf.spec.http.HttpMethod;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import java.time.LocalDateTime;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@Valid
@ApiModel
@Setter
@Getter
public class ApisInfoFindDto extends PageQuery {

  @ApiModelProperty(value = "Apis id")
  private Long id;

  @NotNull
  @ApiModelProperty(value = "Project id", required = true)
  private Long projectId;

  @ApiModelProperty(value = "Services id")
  private Long serviceId;

  private ApisProtocol protocol;

  private HttpMethod method;

  @Length(max = DEFAULT_NAME_LENGTH_X4)
  private String summary;

  @Length(max = DEFAULT_CODE_LENGTH_X5)
  private String operationId;

  private ApiStatus status;

  @ApiModelProperty(value = "Required when app administrators query all apis")
  private Boolean adminFlag;

  @ApiModelProperty(value = "Required when the user query has the one permission apis")
  private ApiPermission hasPermission;

  @Length(max = DEFAULT_URL_LENGTH_X2)
  private String endpoint;

  private Long ownerId;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long createdBy;

  private Long favouriteBy;

  private Long followBy;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}



