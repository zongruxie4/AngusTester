package cloud.xcan.angus.core.tester.interfaces.services.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_CODE_LENGTH_X5;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X4;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X2;

import cloud.xcan.angus.api.commonlink.services.ServicesPermission;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;


@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
public class ServicesApisSearchDto extends PageQuery {

  @Schema(description = "Apis id")
  private Long id;

  // Fix: The path value {servicesId} binds the query parameter exception: Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'
  //  @ID
  //  @Schema(description = "Services id")
  //  private Long serviceId;

  private ApisProtocol protocol;

  @Length(max = MAX_NAME_LENGTH_X4)
  private String summary;

  @Length(max = MAX_CODE_LENGTH_X5)
  private String operationId;

  @Schema(description = "Required when app administrators query all projects")
  private Boolean admin;

  @Schema(description = "Required when the user query has the one permission project")
  private ServicesPermission hasPermission;

  @Length(max = MAX_URL_LENGTH_X2)
  private String endpoint;

  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

  private Long createdBy;

  private Long ownerId;
}



