package cloud.xcan.sdf.api.commonlink;

import static cloud.xcan.sdf.api.commonlink.TesterApisMessage.EVENT_SUBJECT;
import static cloud.xcan.sdf.api.commonlink.TesterConstant.ANGUSTESTER_PRODUCT_CODE;
import static cloud.xcan.sdf.core.utils.EventUtils.assembleNoticeEvent;
import static cloud.xcan.sdf.spec.locale.MessageHolder.message;

import cloud.xcan.sdf.api.enums.EventType;
import cloud.xcan.sdf.api.enums.NoticeType;
import cloud.xcan.sdf.api.enums.ReceiveObjectType;
import cloud.xcan.sdf.core.event.source.EventContent;
import java.util.List;
import java.util.stream.Collectors;

public class EventUtils {

  public static EventContent assembleAngusTesterUserNoticeEvent(String code, String message,
      String targetType, String targetId, String targetName,
      List<NoticeType> noticeTypes, List<Long> receiveObjectIds) {
    EventContent eventContent = assembleNoticeEvent(ANGUSTESTER_PRODUCT_CODE,
        EventType.NOTICE, code, message, targetType, targetId, targetName,
        noticeTypes, ReceiveObjectType.USER, receiveObjectIds.stream().distinct()
            .collect(Collectors.toList()));
    eventContent.setSubject(message(EVENT_SUBJECT));
    return eventContent;
  }

}
