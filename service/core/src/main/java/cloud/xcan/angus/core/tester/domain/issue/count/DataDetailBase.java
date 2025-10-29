package cloud.xcan.angus.core.tester.domain.issue.count;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"name"})
public interface DataDetailBase {

  String getName();

  void setName(String name);

  String[] toValues();

}
