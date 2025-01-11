package cloud.xcan.sdf.core.angustester.application.cmd.mock.impl;

import static cloud.xcan.sdf.core.pojo.principal.PrincipalContext.isOpenApi2p;

import cloud.xcan.sdf.core.angustester.application.cmd.mock.MockApisCmd;
import cloud.xcan.sdf.core.angustester.application.cmd.mock.MockApisLogCmd;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLog;
import cloud.xcan.sdf.core.angustester.domain.mock.apis.log.MockApisLogRepo;
import cloud.xcan.sdf.core.biz.Biz;
import cloud.xcan.sdf.core.biz.ProtocolAssert;
import cloud.xcan.sdf.core.biz.cmd.CommCmd;
import cloud.xcan.sdf.core.jpa.repository.BaseRepository;
import cloud.xcan.sdf.spec.experimental.IdKey;
import javax.annotation.Resource;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author xiaolong.liu
 */
@Biz
public class MockApisLogCmdImpl extends CommCmd<MockApisLog, Long> implements MockApisLogCmd {

  @Resource
  private MockApisLogRepo mockApisLogRepo;

  @Resource
  private MockApisCmd mockApisCmd;

  @Transactional(rollbackFor = Exception.class)
  @Override
  public IdKey<Long, Object> add0(MockApisLog log) {
    // Check the must be an open2p apis
    ProtocolAssert.assertTrue(isOpenApi2p(), "Must by the /openapi2p apis call.");

    // Check the update apis exists and permission etc.
    // NOOP -> Frequent calls to reduce database operations and improve optimization performance
    return insert(log);
  }

  @Override
  protected BaseRepository<MockApisLog, Long> getRepository() {
    return this.mockApisLogRepo;
  }
}
