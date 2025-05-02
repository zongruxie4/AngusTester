package cloud.xcan.angus.core.tester.interfaces.exec.facade.vo.sample;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ExecSampleUploadResultProgressVo {

  private boolean finish;

  private Long nodeId;

  private long uploadResultBytes;

  private long uploadResultTotalBytes;

  private String uploadResultProgress;

}
