package cloud.xcan.angus.core.tester.interfaces.apis.facade.vo.share;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid
@Getter
@Setter
@Accessors(chain = true)
public class ApisShareAddVo {

  @Schema(description = "Share id")
  private Long id;

  @Schema(description = "Access url")
  private String url;

}
