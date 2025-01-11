package cloud.xcan.sdf.model.remoting.vo;

import io.swagger.annotations.ApiModel;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author xiaolong.liu
 */
@ApiModel
@Getter
@Setter
@Accessors(chain = true)
public class MockApisInfoVo {

  private Long id;

  private Long projectId;

  private String name;

  private String method;

  private String endpoint;

  private Long assocProjectId;

  private Long assocApisId;

  private List<MockApiResponseInfoVo> responses;

}
