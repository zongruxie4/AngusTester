package cloud.xcan.angus.core.tester.interfaces.scenario.facade.dto.auth;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;

import cloud.xcan.angus.api.enums.AuthObjectType;
import cloud.xcan.angus.remote.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;


@Setter
@Getter

public class ScenarioAuthFindDto extends PageQuery {

  @Schema(description = "Authorization record identifier for precise query filtering")
  private Long id;

  //@NotNull -> Transferring values in filters
  @Schema(description = "Scenario identifier for authorization query filtering", requiredMode = RequiredMode.REQUIRED)
  private Long scenarioId;

  @NotNull
  @Schema(description = "Authorization object type for entity category filtering", example = "USER", requiredMode = RequiredMode.REQUIRED)
  private AuthObjectType authObjectType;

  @Schema(description = "Authorization object identifier for specific entity filtering")
  private Long authObjectId;

  @Schema(description = "Authorization creation date for temporal filtering")
  @DateTimeFormat(pattern = DATE_FMT)
  private LocalDateTime createdDate;

}
