package cloud.xcan.sdf.core.angustester.application.converter;


import static cloud.xcan.sdf.api.commonlink.CommonConstant.DEFAULT_PERF_DURATION;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.convert;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.isNull;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.commonlink.setting.indicator.Perf;
import cloud.xcan.sdf.api.enums.Percentile;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorPerf;
import cloud.xcan.sdf.core.angustester.domain.services.Services;
import cloud.xcan.sdf.core.utils.GsonUtils;
import cloud.xcan.sdf.spec.unit.TimeValue;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author xiaolong.liu
 */
public class IndicatorPerfConverter {

  public static IndicatorPerf toIndicatorPerf(Perf default0, Long targetId,
      CombinedTargetType targetType) {
    return new IndicatorPerf()
        .setTargetType(targetType)
        .setTargetId(targetId)
        .setThreads(default0.getThreads())
        .setDuration(default0.getDuration())
        .setRampUpThreads(default0.getRampUpThreads())
        .setRampUpInterval(default0.getRampUpInterval())
        .setArt(default0.getArt())
        .setPercentile(default0.getPercentile())
        .setTps(default0.getTps())
        .setErrorRate(default0.getErrorRate());
  }

  public static IndicatorPerf objectArrToPerf(Object[] objects) {
    return new IndicatorPerf()
        .setId(convert(objects[0], Long.class))
        .setTargetId(convert(objects[1], Long.class))
        .setThreads(convert(objects[2], Integer.class))
        .setDuration(isNull(objects[3]) ? DEFAULT_PERF_DURATION
            : GsonUtils.fromJson(objects[3].toString(), TimeValue.class))
        .setRampUpThreads(convert(objects[4], Integer.class))
        .setRampUpInterval(isNull(objects[5]) ? null/*DEFAULT_PERF_RAMPUP_INTERVAL*/
            : GsonUtils.fromJson(objects[5].toString(), TimeValue.class))
        .setArt(convert(objects[6], Long.class))
        .setPercentile(convert(objects[7], Percentile.class))
        .setTps(convert(objects[8], Integer.class))
        .setErrorRate(convert(objects[9], Double.class))
        .setCreatedBy(convert(objects[10], Long.class))
        .setCreatedDate(convert(objects[11], LocalDateTime.class))
        .setTargetName(convert(objects[12], String.class))
        .setTargetType(convert(objects[13], CombinedTargetType.class));
  }

  public static Long getTargetParentId(Services serviceOrApisParent) {
    if (Objects.nonNull(serviceOrApisParent)) {
      return serviceOrApisParent.getId();
    }
    return null;
  }

  public static String getTargetParentName(Services serviceOrApisParent) {
    if (Objects.nonNull(serviceOrApisParent)) {
      return serviceOrApisParent.getName();
    }
    return null;
  }

}
