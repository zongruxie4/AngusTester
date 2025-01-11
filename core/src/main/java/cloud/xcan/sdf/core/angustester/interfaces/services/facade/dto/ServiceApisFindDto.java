package cloud.xcan.sdf.core.angustester.interfaces.services.facade.dto;

import static cloud.xcan.sdf.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_CODE_LENGTH_X5;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_NAME_LENGTH_X4;
import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_URL_LENGTH_X2;

import cloud.xcan.sdf.api.PageQuery;
import cloud.xcan.sdf.api.commonlink.services.ServicesPermission;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@ApiModel
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class ServiceApisFindDto extends PageQuery {

  @ApiModelProperty(value = "Apis id")
  private Long id;

  // Fix: The path value {serviceId} binds the query parameter exception: Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'
  //  @ID
  //  @ApiModelProperty(value = "Services id")
  //  private Long serviceId;

  private ApisProtocol protocol;

  @Length(max = DEFAULT_NAME_LENGTH_X4)
  private String summary;

  @Length(max = DEFAULT_CODE_LENGTH_X5)
  private String operationId;

  @ApiModelProperty(value = "Required when app administrators query all projects")
  private Boolean adminFlag;

  @ApiModelProperty(value = "Required when the user query has the one permission project")
  private ServicesPermission hasPermission;

  @Length(max = DEFAULT_URL_LENGTH_X2)
  private String endpoint;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long createdBy;

  private Long ownerId;
}



