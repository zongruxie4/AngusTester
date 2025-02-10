package cloud.xcan.sdf.core.angustester.domain.mock.data;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockTextRequest {

  private String outKey;

  private String text;

  private int iterations;
}
