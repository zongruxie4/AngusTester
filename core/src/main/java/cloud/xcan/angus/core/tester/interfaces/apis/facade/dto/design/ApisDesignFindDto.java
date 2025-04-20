package cloud.xcan.angus.core.tester.interfaces.apis.facade.dto.design;

import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid

@Setter
@Getter
@Accessors(chain = true)
public class ApisDesignFindDto extends PageQuery {

  private Long id;

  @NotNull
  @Schema(description = "Project ID", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  private String name;

  private Boolean release;

  private Long tenantId;

  private Long createdBy;

  private LocalDateTime createdDate;

  protected Long lastModifiedBy;

  private LocalDateTime lastModifiedDate;

  @Override
  public String getDefaultOrderBy() {
    return "createdDate";
  }

}
