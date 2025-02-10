package cloud.xcan.sdf.core.angustester.domain.mock.data;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockTextData {

  private String outKey;

  private List<String> values;
}
