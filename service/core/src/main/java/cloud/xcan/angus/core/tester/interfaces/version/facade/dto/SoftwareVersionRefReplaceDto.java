package cloud.xcan.angus.core.tester.interfaces.version.facade.dto;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_KEY_LENGTH;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Accessors(chain = true)
public class SoftwareVersionRefReplaceDto {

  @Length(max = MAX_KEY_LENGTH)
  @Schema(description = "Version of software for the task, allow clear version by empty value")
  private String softwareVersion;

}
