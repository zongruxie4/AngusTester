package cloud.xcan.angus.core.tester.domain.indicator;

import cloud.xcan.angus.api.commonlink.CombinedTargetType;

@Deprecated
public interface IndicatorTarget<T> {

  CombinedTargetType getTargetType();

  Long getTargetId();

  String getTargetName();

  Long getTargetParentId();

  String getTargetParentName();

  T setTargetType(CombinedTargetType targetType);

  T setTargetId(Long targetId);

  T setTargetName(String targetName);

  T setTargetParentId(Long targetParentId);

  T setTargetParentName(String targetParentName);

}
