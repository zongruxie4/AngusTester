package cloud.xcan.angus.core.tester.interfaces.services.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class ServicesAddDto {

  @NotNull
  @Schema(description = "Project identifier for service association", requiredMode = RequiredMode.REQUIRED)
  private Long projectId;

  @NotBlank
  @Schema(description = "Service name for identification and management", example = "UserService", requiredMode = RequiredMode.REQUIRED)
  @Length(max = MAX_NAME_LENGTH)
  private String name;

  @Schema(description = "Flag to enable authorization control for access management")
  public Boolean auth;
}




