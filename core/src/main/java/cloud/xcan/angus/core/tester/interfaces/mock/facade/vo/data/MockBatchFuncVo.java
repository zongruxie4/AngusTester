package cloud.xcan.angus.core.tester.interfaces.mock.facade.vo.data;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Valid

@Setter
@Getter
@Accessors(chain = true)
public class MockBatchFuncVo {

  @Schema(example = "outKey")
  private String name;

  @Schema(example = "jack", description = "If the mock function is an array function @Array(...), then the value array data")
  private List<Object> values;

}




