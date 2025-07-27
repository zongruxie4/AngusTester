package cloud.xcan.angus.core.tester.interfaces.services.facade.dto;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import cloud.xcan.angus.api.commonlink.services.ServicesPermission;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;


@Setter
@Getter
@Accessors(chain = true)
public class ServicesFindDto extends PageQuery {

  @Schema(description = "Service identifier for precise query filtering")
  private Long id;

  @NotNull
  @Schema(description = "Project identifier for service filtering", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @Length(max = MAX_NAME_LENGTH)
  @Schema(description = "Service name for fuzzy search")
  private String name;

  @Schema(description = "Flag indicating whether this is an admin query for all projects")
  private Boolean admin;

  @Schema(description = "Required permission level for service access filtering")
  private ServicesPermission hasPermission;

  @Schema(description = "Flag to filter services with associated APIs")
  private Boolean queryHasApis;

  @Schema(description = "Flag to filter services with associated mock services")
  private Boolean queryHasMockService;

  @Schema(description = "Creator identifier for creation-based filtering")
  private Long createdBy;

  @DateTimeFormat(pattern = DATE_FMT)
  @Schema(description = "Service creation date for temporal filtering")
  private LocalDateTime createdDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }
}




