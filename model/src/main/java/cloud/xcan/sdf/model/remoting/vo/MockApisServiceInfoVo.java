package cloud.xcan.sdf.model.remoting.vo;

import cloud.xcan.sdf.api.pojo.CorsData;
import cloud.xcan.sdf.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.sdf.model.remoting.MockServiceSetting;
import io.swagger.annotations.ApiModel;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@ApiModel
@Getter
@Setter
@Accessors(chain = true)
public class MockApisServiceInfoVo {

  private Long id;

  private Long projectId;

  private String code;

  private String name;

  private Long nodeId;

  private String serviceDomainUrl;

  private String serviceHostUrl;

  private boolean authFlag;

  private List<SimpleHttpAuth> apisSecurity;

  private CorsData apisCors;

  private MockServiceSetting setting;

}
