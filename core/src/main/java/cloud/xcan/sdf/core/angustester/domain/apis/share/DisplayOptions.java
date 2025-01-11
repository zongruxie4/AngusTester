package cloud.xcan.sdf.core.angustester.domain.apis.share;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class DisplayOptions {

  private boolean includeServiceInfo = true;

  private boolean allowDebug = false;

  private SchemaStyle schemaStyle = SchemaStyle.TABLE;

}
