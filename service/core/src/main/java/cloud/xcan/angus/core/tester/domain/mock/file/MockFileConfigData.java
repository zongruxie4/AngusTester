package cloud.xcan.angus.core.tester.domain.mock.file;

import static cloud.xcan.angus.spec.experimental.BizConstant.MAX_PARAM_SIZE;

import cloud.xcan.angus.spec.experimental.ValueObjectSupport;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class MockFileConfigData extends ValueObjectSupport<MockFileConfigData> {

  @Size(max = MAX_PARAM_SIZE)
  private List<MockExpression> mockExpression;

  // @Length(max = MAX_MOCK_TEXT_LENGTH)
  private String scriptText;

  @Override
  public MockFileConfigData copy() {
    return new MockFileConfigData(this.mockExpression, this.scriptText);
  }
}
