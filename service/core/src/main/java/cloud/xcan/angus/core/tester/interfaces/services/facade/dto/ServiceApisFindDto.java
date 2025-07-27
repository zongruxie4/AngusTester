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
public class ServiceApisFindDto extends PageQuery {

  @Schema(description = "API identifier for precise query filtering")
  private Long id;

  // Fix: The path value {serviceId} binds the query parameter exception: Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'
  //  @ID
  //  @Schema(description = "Services id")
  //  private Long serviceId;

  @Schema(description = "API protocol type for filtering")
  private ApisProtocol protocol;

  @Length(max = MAX_NAME_LENGTH_X4)
  @Schema(description = "API summary for fuzzy search")
  private String summary;

  @Length(max = MAX_CODE_LENGTH_X5)
  @Schema(description = "API operation identifier for precise search")
  private String operationId;

  @Schema(description = "Flag indicating whether this is an admin query for all projects")
  private Boolean admin;

  @Schema(description = "Required permission level for API access filtering")
  private ServicesPermission hasPermission;

  @Length(max = MAX_URL_LENGTH_X2)
  @Schema(description = "API endpoint for URL-based filtering")
  private String endpoint;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "API creation date for temporal filtering")
  private LocalDateTime createdDate;

  @Schema(description = "Creator identifier for creation-based filtering")
  private Long createdBy;

  @Schema(description = "Owner identifier for ownership-based filtering")
  private Long ownerId;
}



