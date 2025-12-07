package cloud.xcan.angus.core.tester.domain.services;

public enum ServiceApisScope {
  ALL, SELECTED_APIS, MATCH_APIS;

  public boolean isAll() {
    return this == ServiceApisScope.ALL;
  }

  public boolean isMatch() {
    return this == ServiceApisScope.MATCH_APIS;
  }

  public boolean isSelected() {
    return this == ServiceApisScope.SELECTED_APIS;
  }

}
