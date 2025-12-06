package cloud.xcan.angus.core.tester.infra.util;

import cloud.xcan.angus.api.enums.Percentile;
import cloud.xcan.angus.core.tester.domain.config.indicator.PerfData;
import cloud.xcan.angus.spec.unit.TimeValue;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * POJO对象差异比较工具类
 */
public class ObjectDiffUtil {

  /**
   * 比较两个同类型POJO对象的字段值差异
   *
   * @param obj1 第一个对象
   * @param obj2 第二个对象
   * @param <T>  对象类型
   * @return 完全相同返回null，否则返回差异信息字符串
   * @throws IllegalArgumentException 如果参数为null或类型不同
   */
  public static <T> String compareObjects(T obj1, T obj2) {
    // 参数校验
    if (obj1 == null && obj2 == null) {
      return null;
    }
    if (obj1 == null || obj2 == null) {
      return formatDiffResult("Object", obj1, obj2);
    }

    // 检查类型是否相同
    if (!obj1.getClass().equals(obj2.getClass())) {
      throw new IllegalArgumentException("The types of the two objects must be the same.");
    }

    List<String> differences = new ArrayList<>();
    Class<?> clazz = obj1.getClass();

    // 获取所有声明的字段（包括私有字段）
    Field[] fields = clazz.getDeclaredFields();

    for (Field field : fields) {
      try {
        // 设置可访问以便访问私有字段
        field.setAccessible(true);

        Object value1 = field.get(obj1);
        Object value2 = field.get(obj2);

        // 使用Objects.equals进行安全的null值比较
        if (!Objects.equals(value1, value2)) {
          differences.add(formatDiffResult(
              field.getName(),
              value1,
              value2
          ));
        }
      } catch (IllegalAccessException e) {
        // 忽略无法访问的字段
      }
    }

    // 如果没有差异，返回null
    if (differences.isEmpty()) {
      return null;
    }

    // 拼接所有差异信息
    return String.join("; ", differences);
  }

  /**
   * 格式化差异信息
   */
  private static String formatDiffResult(String fieldName, Object value1, Object value2) {
    return String.format("%s: %s -> %s",
        fieldName,
        formatValue(value1),
        formatValue(value2)
    );
  }

  /**
   * 格式化值显示，处理null值和特殊类型
   */
  private static String formatValue(Object value) {
    if (value == null) {
      return "null";
    }

    // 对于字符串，添加引号以便区分
    if (value instanceof String) {
      return "\"" + value + "\"";
    }

    // 对于数组，使用Arrays.toString
    if (value.getClass().isArray()) {
      if (value instanceof Object[]) {
        return java.util.Arrays.toString((Object[]) value);
      } else if (value instanceof int[]) {
        return java.util.Arrays.toString((int[]) value);
      } else if (value instanceof long[]) {
        return java.util.Arrays.toString((long[]) value);
      } else if (value instanceof double[]) {
        return java.util.Arrays.toString((double[]) value);
      } else if (value instanceof float[]) {
        return java.util.Arrays.toString((float[]) value);
      } else if (value instanceof boolean[]) {
        return java.util.Arrays.toString((boolean[]) value);
      } else if (value instanceof byte[]) {
        return java.util.Arrays.toString((byte[]) value);
      } else if (value instanceof char[]) {
        return java.util.Arrays.toString((char[]) value);
      } else if (value instanceof short[]) {
        return java.util.Arrays.toString((short[]) value);
      }
    }
    return value.toString();
  }

  public static void main(String[] args) {
    PerfData perfData1 = new PerfData();
    perfData1.setThreads(5);
    perfData1.setArt(1L);
    perfData1.setDuration(TimeValue.ofSeconds(12));
    perfData1.setPercentile(Percentile.P50);
    PerfData perfData2 = new PerfData();
    perfData2.setThreads(5);
    perfData2.setArt(2L);
    perfData2.setDuration(TimeValue.ofSeconds(11));
    perfData2.setPercentile(Percentile.P90);
    System.out.println(compareObjects(perfData1, perfData2));
  }
}
