package cloud.xcan.angus.model.remoting.vo;


import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author XiaoLong Liu
 */
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
