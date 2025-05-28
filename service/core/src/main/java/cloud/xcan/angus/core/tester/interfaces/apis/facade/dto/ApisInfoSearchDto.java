package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_CODE_LENGTH_X5;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X4;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_URL_LENGTH_X2;

import cloud.xcan.angus.api.commonlink.apis.ApiPermission;
import cloud.xcan.angus.model.apis.ApiStatus;
import cloud.xcan.angus.remote.PageQuery;
import cloud.xcan.angus.spec.http.HttpMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.models.extension.ApisProtocol;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class ApisInfoSearchDto extends PageQuery {

  @Schema(description = "Apis id")
  private Long id;

  @NotNull
  @Schema(description = "Project id", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Schema(description = "Services id")
  private Long serviceId;

  private ApisProtocol protocol;

  private HttpMethod method;

  @Length(max = MAX_NAME_LENGTH_X4)
  private String summary;

  @Length(max = MAX_CODE_LENGTH_X5)
  private String operationId;

  private ApiStatus status;

  @Schema(description = "Required when app administrators query all apis")
  private Boolean admin;

  @Schema(description = "Required when the user query has the one permission apis")
  private ApiPermission hasPermission;

  @Length(max = MAX_URL_LENGTH_X2)
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



