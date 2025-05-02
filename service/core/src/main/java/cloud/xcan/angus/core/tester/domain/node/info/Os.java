package cloud.xcan.angus.core.tester.domain.node.info;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class Os {

  private String name;

  private String version;

  // Unsupported in oshi
  @Deprecated private String arch;

  @JsonIgnore
  private transient String machine;

  private String vendor;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Os)) {
      return false;
    }
    Os os = (Os) o;
    return Objects.equals(name, os.name) &&
        Objects.equals(version, os.version) &&
        Objects.equals(arch, os.arch) &&
        Objects.equals(vendor, os.vendor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, version, arch, vendor);
  }
}
