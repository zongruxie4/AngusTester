package cloud.xcan.sdf.core.angustester.domain.setting;

import io.swagger.annotations.ApiModelProperty;
import java.util.LinkedHashMap;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ContentSetting {

  @Valid
  @NotNull
  @ApiModelProperty(required = true)
  private ContentFilterSetting filter;

  private LinkedHashMap<String, Object> catalogContent;

}
