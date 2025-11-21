package cloud.xcan.angus.core.tester.interfaces.config.facade.dto.domain;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class NodeDomainAddDto {

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(description = "Domain name for cloud service node identification and routing", example = "example.com", requiredMode = RequiredMode.REQUIRED)
  private String name;

}




