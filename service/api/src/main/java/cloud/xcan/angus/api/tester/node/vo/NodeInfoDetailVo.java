package cloud.xcan.angus.api.tester.node.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class NodeInfoDetailVo {

  private Long id;

  private InfoVo info;

  private OsVo os;

  private Boolean agentInstalled;

  private Boolean agentOnline;

  protected LocalDateTime modifiedDate;

  @Setter
  @Getter
  @Accessors(chain = true)
  public static class InfoVo {

    private String platform;
    private int cpuNum;
    private int cpuPhysicalNum;
    private long memTotal;
    private long swapTotal;
    private long fsTotal;
    private String networkSpeed;
    private long maxOpenFiles;
    private long stackSize;

  }

  @Setter
  @Getter
  @Accessors(chain = true)
  public static class OsVo {

    private String name;
    private String version;
    private String arch;
    @JsonIgnore
    @Schema(hidden = true)
    private String machine;
    private String vendor;

  }
}
