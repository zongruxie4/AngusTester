package cloud.xcan.angus.core.tester.application.query.services.impl;

import static cloud.xcan.angus.api.commonlink.TesterConstant.MAX_SYNC_OPENAPI_NUM;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_SYNC_NAME_EXISTED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_SYNC_NUM_OVER_LIMIT_CODE;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_SYNC_NUM_OVER_LIMIT_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_SYNC_TEST_CONN_FAILED_T;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.SERVICE_SYNC_TEST_URL_INVALID;
import static cloud.xcan.angus.remote.message.ProtocolException.M.PARAM_ERROR_KEY;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isEmpty;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;

import cloud.xcan.angus.api.pojo.auth.SimpleHttpAuth;
import cloud.xcan.angus.core.biz.Biz;
import cloud.xcan.angus.core.biz.BizTemplate;
import cloud.xcan.angus.core.biz.ProtocolAssert;
import cloud.xcan.angus.core.biz.exception.QuotaException;
import cloud.xcan.angus.core.tester.application.query.services.ServicesSyncQuery;
import cloud.xcan.angus.core.tester.domain.services.sync.ServicesSync;
import cloud.xcan.angus.core.tester.domain.services.sync.ServicesSyncRepo;
import cloud.xcan.angus.core.utils.ValidatorUtils;
import cloud.xcan.angus.spec.http.HttpSender.Request;
import cloud.xcan.angus.spec.http.HttpUrlConnectionSender;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import jakarta.annotation.Resource;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.exception.ExceptionUtils;

@Biz
public class ServicesSyncQueryImpl implements ServicesSyncQuery {

  @Resource
  private ServicesSyncRepo projectSyncRepo;

  @Override
  public List<ServicesSync> find(Long serviceId) {
    return new BizTemplate<List<ServicesSync>>() {

      @Override
      protected List<ServicesSync> process() {
        return projectSyncRepo.findByServiceId(serviceId);
      }
    }.execute();
  }

  @Override
  public ServicesSync find(Long serviceId, String name) {
    List<ServicesSync> sync = find(serviceId, Collections.singletonList(name));
    return sync.isEmpty() ? null : sync.get(0);
  }

  @Override
  public List<ServicesSync> find(Long serviceId, Collection<String> names) {
    return /* Fix:: !names.isEmpty()*/ isEmpty(names)
        ? projectSyncRepo.findByServiceId(serviceId)
        : projectSyncRepo.findByServiceIdAndNameIn(serviceId, names);
  }

  @Override
  public void checkSyncAddNumQuota(Long serviceId, int incr) {
    int exitedNum = projectSyncRepo.countByServiceId(serviceId);
    if (exitedNum + incr > MAX_SYNC_OPENAPI_NUM) {
      throw QuotaException.of(SERVICE_SYNC_NUM_OVER_LIMIT_CODE, SERVICE_SYNC_NUM_OVER_LIMIT_T,
          new Object[]{MAX_SYNC_OPENAPI_NUM});
    }
  }

  @Override
  public void checkSyncNumQuota(Long serviceId, int num) {
    // List<String> namesDb = projectSyncRepo.findNameByProjectId(serviceId);
    if (num > MAX_SYNC_OPENAPI_NUM) {
      throw QuotaException.of(SERVICE_SYNC_NUM_OVER_LIMIT_CODE, SERVICE_SYNC_NUM_OVER_LIMIT_T,
          new Object[]{MAX_SYNC_OPENAPI_NUM});
    }
  }

  @Override
  public void checkRepeatedNameInParams(List<String> names) {
    List<String> repeatedNames = names.stream().filter(ObjectUtils.duplicateByKey(x -> x))
        .collect(Collectors.toList());
    ProtocolAssert.assertTrue(isEmpty(repeatedNames), SERVICE_SYNC_NAME_EXISTED_T, PARAM_ERROR_KEY,
        new Object[]{isNotEmpty(repeatedNames) ? repeatedNames.get(0) : ""});
  }

  @Override
  public String checkAndGetOpenApiContent(String syncUrl, List<SimpleHttpAuth> auths) {
    // Check the connectivity
    String content = null;
    try {
      // Check the url format
      ProtocolAssert.assertTrue(ValidatorUtils.isUrl(syncUrl.split("//?")[0]),
          SERVICE_SYNC_TEST_URL_INVALID);
      content = Request.build(syncUrl, new HttpUrlConnectionSender())
          .withAuths(auths).send().body();
    } catch (URISyntaxException e) {
      ProtocolAssert.assertTrue(false, SERVICE_SYNC_TEST_URL_INVALID);
    } catch (Exception e) {
      ProtocolAssert.assertTrue(false, SERVICE_SYNC_TEST_CONN_FAILED_T,
          new Object[]{ExceptionUtils.getMessage(e)});
    } catch (Throwable throwable) {
      ProtocolAssert.assertTrue(false, SERVICE_SYNC_TEST_CONN_FAILED_T,
          new Object[]{ExceptionUtils.getMessage(throwable)});
    }
    return content;
  }
}
