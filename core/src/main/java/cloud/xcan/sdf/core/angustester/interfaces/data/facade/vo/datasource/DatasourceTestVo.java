package cloud.xcan.sdf.core.angustester.interfaces.data.facade.vo.datasource;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Getter
@Setter
@Accessors(chain = true)
public class DatasourceTestVo {

  private Boolean connSuccess;

  private String connFailureMessage;

}
