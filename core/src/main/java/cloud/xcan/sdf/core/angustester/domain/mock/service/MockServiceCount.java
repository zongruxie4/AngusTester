package cloud.xcan.sdf.core.angustester.domain.mock.service;

import cloud.xcan.sdf.core.angustester.domain.mock.apis.MockApisCount;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MockServiceCount implements MockApisCount {

  private Long apisNum;

  private Long requestNum;

  private Long pushbackNum;

  private Long simulateErrorNum;

  private Long successNum;

  private Long exceptionNum;

}
