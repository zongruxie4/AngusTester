package cloud.xcan.angus.core.tester.domain.setting;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.LinkedHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ContentSetting {

  @Valid
  @NotNull
  @Schema(requiredMode = RequiredMode.REQUIRED)
  private ContentFilterSetting filter;

  private LinkedHashMap<String, Object> catalogContent;

}
