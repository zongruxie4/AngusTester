package cloud.xcan.angus.core.tester.domain;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;

public interface BaseTarget {

  Long getTargetId();

  CombinedTargetType getTargetType();

  String getTargetName();

  void setTargetId(Long targetId);

  void setTargetType(CombinedTargetType targetType);

  void setTargetName(String targetName);
}
