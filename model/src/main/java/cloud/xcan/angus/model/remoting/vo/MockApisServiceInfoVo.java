package cloud.xcan.angus.model.remoting.vo;

import cloud.xcan.angus.api.pojo.CorsData;
import cloud.xcan.angus.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.angus.model.remoting.MockServiceSetting;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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

  private boolean auth;

  private List<SimpleHttpAuth> apisSecurity;

  private CorsData apisCors;

  private MockServiceSetting setting;

}
