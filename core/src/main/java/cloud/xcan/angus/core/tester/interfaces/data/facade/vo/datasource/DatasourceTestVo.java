package cloud.xcan.angus.core.tester.interfaces.data.facade.vo.datasource;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


@Getter
@Setter
@Accessors(chain = true)
public class DatasourceTestVo {

  private Boolean connSuccess;

  private String connFailureMessage;

}
