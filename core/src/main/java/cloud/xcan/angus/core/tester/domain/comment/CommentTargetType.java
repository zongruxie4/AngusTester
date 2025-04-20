package cloud.xcan.angus.core.tester.domain.comment;

import cloud.xcan.angus.spec.locale.EnumValueMessage;

public enum CommentTargetType implements EnumValueMessage<String> {
  TASK, SCRIPT, FUNC_CASE;

  @Override
  public String getValue() {
    return this.name();
  }

}
