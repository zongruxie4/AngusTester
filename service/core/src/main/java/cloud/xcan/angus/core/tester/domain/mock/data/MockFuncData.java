package cloud.xcan.angus.core.tester.domain.mock.data;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
public class MockFuncData {

  private String outKey;

  private List<Object> values;

}
