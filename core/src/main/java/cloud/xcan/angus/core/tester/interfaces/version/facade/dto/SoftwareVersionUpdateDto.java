package cloud.xcan.angus.core.tester.interfaces.version.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_DESC_LENGTH;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;


@Setter
@Getter
@Accessors(chain = true)
public class SoftwareVersionUpdateDto {

  @NotNull
  @Schema(description = "Version id", requiredMode = RequiredMode.REQUIRED)
  private Long id;

  @Schema(description = "Version name")
  private String name;

  @Schema(description = "Version start date")
  private LocalDateTime startDate;

  @Schema(description = "Version release date")
  private LocalDateTime releaseDate;

  @Length(max = MAX_DESC_LENGTH)
  @Schema(description = "Version description")
  private String description;

}
