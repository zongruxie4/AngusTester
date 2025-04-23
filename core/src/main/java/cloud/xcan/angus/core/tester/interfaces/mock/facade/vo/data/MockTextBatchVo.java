package cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.data;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */@Valid
@Setter
@Getter
@Accessors(chain = true)
public class MockTextBatchVo {

  @Schema(example = "outKey")
  private String outKey;

  @Schema(example = "jack", description = "If the mock function is an array function @Array(...), then the value array data")
  private List<String> values;

}
