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
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * <p>
 * Implementation of ServicesSyncQuery for services synchronization management and query
 * operations.
 * </p>
 * <p>
 * Provides methods for service synchronization configuration, quota validation, and OpenAPI content
 * retrieval.
 * </p>
 */
@Biz
public class ServicesSyncQueryImpl implements ServicesSyncQuery {

  @Resource
  private ServicesSyncRepo projectSyncRepo;

  /**
   * <p>
   * Find all synchronization configurations for a service.
   * </p>
   *
   * @param serviceId Service ID
   * @return List of service synchronization configurations
   */
  @Override
  public List<ServicesSync> find(Long serviceId) {
    return new BizTemplate<List<ServicesSync>>() {

      @Override
      protected List<ServicesSync> process() {
        return projectSyncRepo.findByServiceId(serviceId);
      }
    }.execute();
  }

  /**
   * <p>
   * Find a specific synchronization configuration by name.
   * </p>
   *
   * @param serviceId Service ID
   * @param name      Synchronization configuration name
   * @return Service synchronization configuration or null if not found
   */
  @Override
  public ServicesSync find(Long serviceId, String name) {
    List<ServicesSync> sync = find(serviceId, Collections.singletonList(name));
    return sync.isEmpty() ? null : sync.get(0);
  }

  /**
   * <p>
   * Find synchronization configurations by names.
   * </p>
   * <p>
   * If no names are provided, returns all synchronization configurations for the service.
   * Otherwise, returns only configurations matching the specified names.
   * </p>
   *
   * @param serviceId Service ID
   * @param names     Collection of synchronization configuration names
   * @return List of matching service synchronization configurations
   */
  @Override
  public List<ServicesSync> find(Long serviceId, Collection<String> names) {
    return /* Fix:: !names.isEmpty()*/ isEmpty(names)
        ? projectSyncRepo.findByServiceId(serviceId)
        : projectSyncRepo.findByServiceIdAndNameIn(serviceId, names);
  }

  /**
   * <p>
   * Check quota for adding synchronization configurations.
   * </p>
   * <p>
   * Validates that adding the specified number of configurations does not exceed the maximum
   * limit.
   * </p>
   *
   * @param serviceId Service ID
   * @param incr      Number of configurations to be added
   */
  @Override
  public void checkSyncAddNumQuota(Long serviceId, int incr) {
    int exitedNum = projectSyncRepo.countByServiceId(serviceId);
    if (exitedNum + incr > MAX_SYNC_OPENAPI_NUM) {
      throw QuotaException.of(SERVICE_SYNC_NUM_OVER_LIMIT_CODE, SERVICE_SYNC_NUM_OVER_LIMIT_T,
          new Object[]{MAX_SYNC_OPENAPI_NUM});
    }
  }

  /**
   * <p>
   * Check quota for synchronization configuration count.
   * </p>
   * <p>
   * Validates that the specified number does not exceed the maximum allowed limit.
   * </p>
   *
   * @param serviceId Service ID
   * @param num       Number of configurations to validate
   */
  @Override
  public void checkSyncNumQuota(Long serviceId, int num) {
    // List<String> namesDb = projectSyncRepo.findNameByProjectId(serviceId);
    if (num > MAX_SYNC_OPENAPI_NUM) {
      throw QuotaException.of(SERVICE_SYNC_NUM_OVER_LIMIT_CODE, SERVICE_SYNC_NUM_OVER_LIMIT_T,
          new Object[]{MAX_SYNC_OPENAPI_NUM});
    }
  }

  /**
   * <p>
   * Check for repeated names in the provided list.
   * </p>
   * <p>
   * Validates that there are no duplicate names in the synchronization configuration names list.
   * Throws an exception if duplicates are found.
   * </p>
   *
   * @param names List of names to check for duplicates
   */
  @Override
  public void checkRepeatedNameInParams(List<String> names) {
    List<String> repeatedNames = names.stream().filter(ObjectUtils.duplicateByKey(x -> x))
        .toList();
    ProtocolAssert.assertTrue(isEmpty(repeatedNames), SERVICE_SYNC_NAME_EXISTED_T, PARAM_ERROR_KEY,
        new Object[]{isNotEmpty(repeatedNames) ? repeatedNames.get(0) : ""});
  }

  /**
   * <p>
   * Check connectivity and retrieve OpenAPI content from a URL.
   * </p>
   * <p>
   * Validates URL format, tests connectivity, and retrieves OpenAPI content with optional
   * authentication. Handles various connection and parsing errors gracefully.
   * </p>
   *
   * @param syncUrl URL to retrieve OpenAPI content from
   * @param auths   List of HTTP authentication configurations
   * @return OpenAPI content as string
   */
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
