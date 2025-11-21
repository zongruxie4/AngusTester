package cloud.xcan.angus.core.tester.interfaces.project.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH;
import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_KEY_LENGTH;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;


@Setter
@Getter
@Accessors(chain = true)
public class SoftwareVersionAddDto {

  @NotNull
  @Schema(description = "Project identifier for version organization and access control", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @NotEmpty
  @Length(max = MAX_KEY_LENGTH)
  @Schema(description = "Software version name for identification and release management", requiredMode = RequiredMode.REQUIRED)
  private String name;

  @Schema(description = "Version development start date for timeline planning")
  private LocalDateTime startDate;

  @Schema(description = "Version release date for production deployment planning")
  private LocalDateTime releaseDate;

  @Length(max = MAX_DESC_LENGTH)
  @Schema(description = "Version description for feature documentation and release notes")
  private String description;

}
