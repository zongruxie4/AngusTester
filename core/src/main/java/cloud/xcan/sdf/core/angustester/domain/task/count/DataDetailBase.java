package cloud.xcan.sdf.core.angustester.domain.task.count;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"name"})
public interface DataDetailBase {

  String getName();

  void setName(String name);

  String[] toValues();

}
