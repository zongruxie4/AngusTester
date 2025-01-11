package cloud.xcan.sdf.core.angustester.domain.kanban;

import cloud.xcan.angus.model.script.configuration.ScriptType;
import cloud.xcan.sdf.api.pojo.IdAndCreatedDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Getter
@Accessors(chain = true)
public class ScriptTimeSeries extends IdAndCreatedDate {

  private ScriptType type;

  public ScriptTimeSeries() {
  }

  public ScriptTimeSeries(ScriptType type, Long id, LocalDateTime createdDate) {
    super(id, createdDate);
    this.type = type;
  }

}
