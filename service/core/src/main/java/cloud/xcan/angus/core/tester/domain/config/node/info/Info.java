package cloud.xcan.angus.core.tester.domain.config.node.info;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class Info {

  private String platform;
  private int cpuNum;
  private int cpuPhysicalNum;
  private long memTotal;
  private long swapTotal;
  private long fsTotal;
  // Speed: 100,000,000 bits = 100 Mbps = 12.5MB/s
  private String networkSpeed;
  private long maxOpenFiles;
  private long openedFiles;
  // Unsupported in oshi
  @Deprecated private long stackSize;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Info)) {
      return false;
    }
    Info info = (Info) o;
    return cpuNum == info.cpuNum &&
        cpuPhysicalNum == info.cpuPhysicalNum &&
        memTotal == info.memTotal &&
        swapTotal == info.swapTotal &&
        fsTotal == info.fsTotal &&
        maxOpenFiles == info.maxOpenFiles &&
        openedFiles == info.openedFiles &&
        stackSize == info.stackSize &&
        Objects.equals(platform, info.platform) &&
        Objects.equals(networkSpeed, info.networkSpeed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(platform, cpuNum, cpuPhysicalNum, memTotal, swapTotal, fsTotal,
        networkSpeed, maxOpenFiles, openedFiles, stackSize);
  }
}
