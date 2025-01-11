package cloud.xcan.sdf.core.angustester.domain.mock.file;

import static cloud.xcan.sdf.spec.experimental.BizConstant.MAX_PARAM_SIZE;

import cloud.xcan.sdf.spec.experimental.ValueObjectSupport;
import java.util.List;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
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
