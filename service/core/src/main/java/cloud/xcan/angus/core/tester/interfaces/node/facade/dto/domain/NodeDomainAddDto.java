package cloud.xcan.angus.core.tester.interfaces.node.facade.dto.domain;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_NAME_LENGTH_X2;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Valid
@Setter
@Getter
@Accessors(chain = true)
public class NodeDomainAddDto {

  @Length(max = MAX_NAME_LENGTH_X2)
  @Schema(example = "example.com", requiredMode = RequiredMode.REQUIRED)
  private String name;

}




