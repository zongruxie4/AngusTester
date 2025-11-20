package cloud.xcan.angus.api.commonlink;

import static cloud.xcan.angus.api.commonlink.TesterApisMessage.EVENT_SUBJECT;
import static cloud.xcan.angus.core.utils.EventUtils.assembleNoticeEvent;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;

import cloud.xcan.angus.api.enums.EventType;
import cloud.xcan.angus.api.enums.NoticeType;
import cloud.xcan.angus.api.enums.ReceiveObjectType;
import cloud.xcan.angus.core.event.source.EventContent;
import java.util.List;

public class EventUtils {

  public static EventContent assembleAngusTesterUserNoticeEvent(String code, String message,
      String targetType, String targetId, String targetName,
      List<NoticeType> noticeTypes, List<Long> receiveObjectIds) {
    EventContent eventContent = assembleNoticeEvent(TesterConstant.ANGUSTESTER_PRODUCT_CODE,
        EventType.NOTICE.getValue(), code, message, targetType, targetId, targetName,
        noticeTypes, ReceiveObjectType.USER, receiveObjectIds.stream().distinct()
            .toList());
    eventContent.setSubject(message(EVENT_SUBJECT));
    return eventContent;
  }

}
