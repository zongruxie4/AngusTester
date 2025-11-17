package cloud.xcan.angus.core.tester.domain;

/**
 * Test Object / Level (Required)
 */
public enum TestLayer {

  /**
   * User interface testing
   */
  UI,

  /**
   * API interface testing
   */
  API,

  /**
   * Unit testing
   */
  UNIT,

  /**
   * Integration testing between modules
   */
  INTEGRATION,

  /**
   * End-to-end testing
   */
  E2E
}
