package cloud.xcan.sdf.core.angustester.application.converter;


import static cloud.xcan.sdf.api.commonlink.CommonConstant.DEFAULT_STABILITY_DURATION;
import static cloud.xcan.sdf.spec.utils.ObjectUtils.convert;
import static java.util.Objects.isNull;

import cloud.xcan.sdf.api.commonlink.CombinedTargetType;
import cloud.xcan.sdf.api.commonlink.setting.indicator.Stability;
import cloud.xcan.sdf.api.enums.Percentile;
import cloud.xcan.sdf.core.angustester.domain.indicator.IndicatorStability;
import cloud.xcan.sdf.core.utils.GsonUtils;
import cloud.xcan.sdf.spec.unit.TimeValue;
import java.time.LocalDateTime;

/**
 * @author xiaolong.liu
 */
public class IndicatorStabilityConverter {

  public static IndicatorStability toIndicatorStability(Stability default0, Long targetId,
      CombinedTargetType targetType) {
    return new IndicatorStability()
        .setTargetType(targetType)
        .setTargetId(targetId)
        .setThreads(default0.getThreads())
        .setDuration(default0.getDuration())
        .setArt(default0.getArt())
        .setPercentile(default0.getPercentile())
        .setTps(default0.getTps())
        .setErrorRate(default0.getErrorRate())
        .setCpu(default0.getCpu())
        .setMemory(default0.getMemory())
        .setDisk(default0.getDisk())
        .setNetwork(default0.getNetwork());
  }

  public static IndicatorStability objectArrToStability(Object[] objects) {
    return new IndicatorStability()
        .setId(convert(objects[0], Long.class))
        .setTargetId(convert(objects[1], Long.class))
        .setThreads(convert(objects[2], Integer.class))
        .setDuration(isNull(objects[3]) ? DEFAULT_STABILITY_DURATION
            : GsonUtils.fromJson(objects[3].toString(), TimeValue.class))
        .setArt(convert(objects[4], Long.class))
        .setPercentile(convert(objects[5], Percentile.class))
        .setTps(convert(objects[6], Integer.class))
        .setErrorRate(convert(objects[7], Double.class))
        .setCpu(convert(objects[8], Double.class))
        .setMemory(convert(objects[9], Double.class))
        .setDisk(convert(objects[10], Double.class))
        .setNetwork(convert(objects[11], Double.class))
        .setCreatedBy(convert(objects[12], Long.class))
        .setCreatedDate(convert(objects[13], LocalDateTime.class))
        .setTargetName(convert(objects[14], String.class))
        .setTargetType(convert(objects[15], CombinedTargetType.class));
  }

}
