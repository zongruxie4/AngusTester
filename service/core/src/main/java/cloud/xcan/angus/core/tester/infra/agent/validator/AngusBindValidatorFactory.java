package cloud.xcan.angus.core.tester.infra.agent.validator;


import cloud.xcan.angus.remoting.common.spi.BindValidator;
import cloud.xcan.angus.remoting.common.spi.BindValidatorFactory;
import cloud.xcan.angus.remoting.common.spi.Spi;

@Spi(order = 0)
public class AngusBindValidatorFactory implements BindValidatorFactory {

  private final BindValidator validator = new AngusBindValidator();

  @Override
  public BindValidator get() {
    return validator;
  }
}
