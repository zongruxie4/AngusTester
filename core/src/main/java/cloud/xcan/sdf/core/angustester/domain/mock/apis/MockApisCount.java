package cloud.xcan.sdf.core.angustester.domain.mock.apis;

/**
 * Fix:: JPA Null return value from advice does not match primitive return type for: MockApisCount.
 * <p>
 * Modify primitive long to Long.
 */
public interface MockApisCount {

  Long getRequestNum();

  Long getPushbackNum();

  Long getSimulateErrorNum();

  Long getSuccessNum();

  Long getExceptionNum();

}
