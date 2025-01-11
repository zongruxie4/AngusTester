package cloud.xcan.sdf.core.angustester.domain.comment;

import cloud.xcan.sdf.spec.locale.EnumValueMessage;

public enum CommentTargetType implements EnumValueMessage<String> {
  TASK, SCRIPT, FUNC_CASE;

  @Override
  public String getValue() {
    return this.name();
  }

}
