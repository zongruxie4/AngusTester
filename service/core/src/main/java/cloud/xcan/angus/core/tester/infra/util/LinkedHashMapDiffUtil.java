package cloud.xcan.angus.core.tester.infra.util;

import cloud.xcan.angus.core.tester.domain.project.evaluation.EvaluationPurpose;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

/**
 * LinkedHashMap比较工具类（遍历枚举方式）
 */
public class LinkedHashMapDiffUtil {

  /**
   * 比较两个LinkedHashMap<EvaluationPurpose, Integer>的差异 完全相同时返回null，不相同时返回差异信息字符串
   *
   * @param map1 第一个Map
   * @param map2 第二个Map
   * @return 差异信息字符串，完全相同返回null
   */
  public static String compareEvaluationMaps(
      LinkedHashMap<EvaluationPurpose, Integer> map1,
      LinkedHashMap<EvaluationPurpose, Integer> map2) {

    // 处理null情况
    if (map1 == null && map2 == null) {
      return null;
    }
    if (map1 == null || map2 == null) {
      return formatNullDiff(map1, map2);
    }

    // 如果两个Map都为空，则相同
    if (map1.isEmpty() && map2.isEmpty()) {
      return null;
    }

    StringBuilder diffBuilder = new StringBuilder();
    boolean hasDifference = false;

    // 遍历枚举的所有值
    for (EvaluationPurpose enumValue : EvaluationPurpose.values()) {
      Integer value1 = map1.get(enumValue);
      Integer value2 = map2.get(enumValue);

      boolean existsInMap1 = map1.containsKey(enumValue);
      boolean existsInMap2 = map2.containsKey(enumValue);

      // 情况1: 两个Map都不包含此枚举键
      if (!existsInMap1 && !existsInMap2) {
        continue; // 跳过，因为两个Map都没有这个键
      }

      // 情况2: 只有一个Map包含此枚举键
      if (existsInMap1 && !existsInMap2) {
        if (hasDifference) {
          diffBuilder.append("; ");
        }
        diffBuilder.append(formatKeyMissing(enumValue, "map2", value1));
        hasDifference = true;
        continue;
      }

      if (!existsInMap1 && existsInMap2) {
        if (hasDifference) {
          diffBuilder.append("; ");
        }
        diffBuilder.append(formatKeyMissing(enumValue, "map1", value2));
        hasDifference = true;
        continue;
      }

      // 情况3: 两个Map都包含此枚举键，比较值
      if (!areValuesEqual(value1, value2)) {
        if (hasDifference) {
          diffBuilder.append("; ");
        }
        diffBuilder.append(formatValueDiff(enumValue, value1, value2));
        hasDifference = true;
      }
    }

    // 如果找到了差异，返回差异字符串
    if (hasDifference) {
      return diffBuilder.toString();
    }
    return null;
  }

  /**
   * 格式化null差异
   */
  private static String formatNullDiff(
      LinkedHashMap<EvaluationPurpose, Integer> map1,
      LinkedHashMap<EvaluationPurpose, Integer> map2) {

    if (map1 == null && map2 != null) {
      return "map1 is null, map2 is not null";
    } else if (map1 != null && map2 == null) {
      return "map1 is null, map2 is not null";
    }
    return null;
  }

  /**
   * 格式化键缺失的差异
   */
  private static String formatKeyMissing(EvaluationPurpose key, String missingMap,
      Integer otherValue) {
    return String.format("%s is missing in %s (Value: %s)",
        key.getMessage(), missingMap, formatInteger(otherValue));
  }

  /**
   * 格式化值差异
   */
  private static String formatValueDiff(EvaluationPurpose key, Integer value1, Integer value2) {
    return String.format("%s: %s -> %s",
        key.getMessage(), formatInteger(value1), formatInteger(value2));
  }

  /**
   * 格式化整数值
   */
  private static String formatInteger(Integer value) {
    return value == null ? "null" : value.toString();
  }

  /**
   * 比较两个值是否相等（处理null值）
   */
  private static boolean areValuesEqual(Integer value1, Integer value2) {
    if (value1 == null && value2 == null) {
      return true;
    }
    if (value1 == null || value2 == null) {
      return false;
    }
    return value1.equals(value2);
  }

  public static void main(String[] args) {
    LinkedHashMap<EvaluationPurpose, Integer> map1 = new LinkedHashMap<>();
    map1.put(EvaluationPurpose.FUNCTIONAL_SCORE, 5);
    map1.put(EvaluationPurpose.PERFORMANCE_SCORE, 10);
    map1.put(EvaluationPurpose.STABILITY_SCORE, 0);

    LinkedHashMap<EvaluationPurpose, Integer> map2 = new LinkedHashMap<>();
    map2.put(EvaluationPurpose.FUNCTIONAL_SCORE, 5);
    map2.put(EvaluationPurpose.PERFORMANCE_SCORE, 8);
    map2.put(EvaluationPurpose.STABILITY_SCORE, 1);

    String diff = compareEvaluationMaps(map1, map2);
    System.out.println(diff);
  }
}
