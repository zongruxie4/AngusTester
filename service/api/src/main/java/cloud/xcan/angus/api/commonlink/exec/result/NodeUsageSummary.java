package cloud.xcan.angus.api.commonlink.exec.result;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class NodeUsageSummary {

  private Double meanCpu;
  private Double meanMemory;
  private Double meanFilesystem;
  private Double meanNetwork;

  private Double maxCpu;
  private Double maxMemory;
  private Double maxFilesystem;
  private Double maxNetwork;

}
