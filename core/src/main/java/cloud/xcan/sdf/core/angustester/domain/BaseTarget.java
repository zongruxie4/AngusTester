package cloud.xcan.sdf.core.angustester.domain;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;

public interface BaseTarget {

  Long getTargetId();

  CombinedTargetType getTargetType();

  String getTargetName();

  void setTargetId(Long targetId);

  void setTargetType(CombinedTargetType targetType);

  void setTargetName(String targetName);
}
