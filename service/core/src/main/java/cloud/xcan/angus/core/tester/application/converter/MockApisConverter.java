package cloud.xcan.angus.core.tester.application.converter;

import static cloud.xcan.angus.core.tester.application.converter.MockServiceConverter.getMockResponseBodyString;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MESSAGE_MATCH_STATUS_AND_MEDIATYPE_RESPONSE_NAME;
import static cloud.xcan.angus.core.tester.domain.TesterCoreMessage.MESSAGE_MATCH_STATUS_RESPONSE_NAME;
import static cloud.xcan.angus.spec.locale.MessageHolder.message;
import static cloud.xcan.angus.spec.utils.ObjectUtils.isNotEmpty;
import static io.swagger.v3.oas.models.extension.ExtensionKey.VALUE_KEY;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.math.NumberUtils.isDigits;

import cloud.xcan.angus.core.tester.domain.apis.Apis;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApis;
import cloud.xcan.angus.core.tester.domain.mock.apis.MockApisSource;
import cloud.xcan.angus.core.tester.domain.mock.apis.response.MockApisResponse;
import cloud.xcan.angus.core.tester.domain.mock.service.MockService;
import cloud.xcan.angus.extension.angustester.api.ApiImportSource;
import cloud.xcan.angus.idgen.UidGenerator;
import cloud.xcan.angus.idgen.uid.impl.CachedUidGenerator;
import cloud.xcan.angus.model.element.mock.apis.MatchRequest;
import cloud.xcan.angus.model.element.mock.apis.MockResponse;
import cloud.xcan.angus.model.element.mock.apis.MockResponseContent;
import cloud.xcan.angus.spec.http.HttpHeader;
import cloud.xcan.angus.spec.http.HttpMethod;
import cloud.xcan.angus.spec.http.HttpResponseHeader;
import cloud.xcan.angus.spec.utils.ObjectUtils;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;

/**
 * @author XiaoLong Liu
 */public class MockApisConverter {

  public static MockApis toAssocOrCopeMockApis(Apis apisDb, Long mockServiceId,
      MockApisSource apisSource) {
    return new MockApis()
        .setProjectId(apisDb.getProjectId())
        .setSummary(apisDb.getSummary())
        .setDescription(apisDb.getDescription())
        .setSource(apisSource)
        .setImportSource(null)
        .setMethod(apisDb.getMethod())
        .setEndpoint(apisDb.getEndpoint())
        .setMockServiceId(mockServiceId)
        .setAssocServiceId(apisSource.isAssociatedApis() ? apisDb.getServiceId() : null)
        .setAssocApisId(apisSource.isAssociatedApis() ? apisDb.getId() : null)
        .setRequestNum(0L)
        .setPushbackNum(0L)
        .setSimulateErrorNum(0L)
        .setExceptionNum(0L)
        .setSuccessNum(0L);
  }

  public static MockApis toCloneApis(MockApis mockApisDb) {
    return new MockApis()
        .setProjectId(mockApisDb.getProjectId())
        .setSummary(mockApisDb.getSummary())
        .setDescription(mockApisDb.getDescription())
        .setSource(MockApisSource.CREATED)
        .setImportSource(null)
        .setMethod(mockApisDb.getMethod())
        .setEndpoint(mockApisDb.getEndpoint())
        .setMockServiceId(mockApisDb.getMockServiceId())
        .setAssocServiceId(mockApisDb.getAssocServiceId())
        .setAssocApisId(null /*mockApisDb.getAssocApisId()*/) // Apis only allows association once
        .setRequestNum(0L)
        .setPushbackNum(0L)
        .setSimulateErrorNum(0L)
        .setExceptionNum(0L)
        .setSuccessNum(0L);
  }

  public static MockApis toAssocMockApi(UidGenerator uidGenerator, MockService service, Apis apis) {
    return new MockApis().setId(uidGenerator.getUID())
        .setProjectId(service.getProjectId())
        .setSummary(apis.getSummary())
        .setDescription(apis.getDescription())
        .setSource(MockApisSource.ASSOC_APIS)
        .setImportSource(ApiImportSource.OPENAPI)
        .setMethod(apis.getMethod())
        .setEndpoint(apis.getEndpoint())
        .setMockServiceId(service.getId())
        .setAssocServiceId(service.getAssocServiceId())
        .setAssocApisId(apis.getId());
  }

  public static MockApis toImportedMockApis(UidGenerator uidGenerator, MockService mockServiceDb,
      cloud.xcan.angus.model.element.mock.apis.MockApis mockApis) {
    return new MockApis()
        .setId(uidGenerator.getUID())
        .setProjectId(mockServiceDb.getProjectId())
        .setSummary(mockApis.getName())
        .setDescription(mockApis.getDescription())
        .setSource(MockApisSource.ANGUS_IMPORT)
        .setImportSource(ApiImportSource.ANGUS)
        .setMethod(HttpMethod.valueOf(mockApis.getMethod().name()))
        .setEndpoint(mockApis.getEndpoint())
        .setMockServiceId(mockServiceDb.getId())
        .setAssocServiceId(null)
        .setAssocApisId(null)
        .setRequestNum(0L)
        .setPushbackNum(0L)
        .setSimulateErrorNum(0L)
        .setExceptionNum(0L)
        .setSuccessNum(0L);
  }

  public static MockApisResponse toAssocMockApisResponse(UidGenerator uidGenerator,
      Long serviceId, MockApis mockApi, String name, int priority, Entry<String, ApiResponse> entry,
      List<HttpHeader> headers, String body) {
    return new MockApisResponse().setId(uidGenerator.getUID())
        .setProjectId(mockApi.getProjectId())
        .setMockServiceId(serviceId)
        .setMockApisId(mockApi.getId())
        .setName(name)
        .setMatch(new MatchRequest()
            .setPriority(priority))
        .setContent(new MockResponseContent()
            .setStatus(isDigits(entry.getKey()) ? Integer.parseInt(entry.getKey()) : 200)
            .setHeaders(headers).setContent(body))
        .setEnablePushback(false);
  }

  public static MockApisResponse toImportedMockApisResponse(UidGenerator uidGenerator,
      MockApis mockApi, MockResponse mockResponse) {
    return new MockApisResponse().setId(uidGenerator.getUID())
        .setProjectId(mockApi.getProjectId())
        .setMockServiceId(mockApi.getMockServiceId())
        .setMockApisId(mockApi.getId())
        .setName(mockResponse.getName())
        .setMatch(mockResponse.getMatch())
        .setContent(mockResponse.getContent())
        .setEnablePushback(nonNull(mockResponse.getPushback()))
        .setPushback(mockResponse.getPushback());
  }

  public static void assembleMockApisResponse(CachedUidGenerator uidGenerator, MockApis mockApi,
      Apis apis, Long mockServiceId, List<MockApisResponse> responses) {
    int priority = apis.getResponses().size();
    for (Entry<String, ApiResponse> entry : apis.getResponses().entrySet()) {
      if (isEmpty(entry.getKey())) {
        continue;
      }
      List<HttpHeader> headers = new ArrayList<>();
      if (isNotEmpty(entry.getValue().getHeaders())) {
        for (Entry<String, Header> headerEntry : entry.getValue().getHeaders().entrySet()) {
          // @DoInFuture("Write response examples to extensions during import and definition")
          String value = ObjectUtils.isEmpty(headerEntry.getValue().getExtensions())
              ? "" : headerEntry.getValue().getExtensions()
              .getOrDefault(VALUE_KEY, "").toString();
          headers.add(new HttpHeader(headerEntry.getKey(), value));
        }
      }

      if (isNotEmpty(entry.getValue().getContent())) {
        for (Entry<String, MediaType> mediaTypeEntry : entry.getValue().getContent().entrySet()) {
          if (isEmpty(mediaTypeEntry.getKey())) {
            continue;
          }
          if (headers.stream().noneMatch(
              x -> HttpResponseHeader.Content_Type.value.equalsIgnoreCase(x.getName()))) {
            headers.add(new HttpHeader(HttpResponseHeader.Content_Type.value,
                mediaTypeEntry.getKey()));
          }
          String name = message(MESSAGE_MATCH_STATUS_AND_MEDIATYPE_RESPONSE_NAME,
              new Object[]{entry.getKey(), mediaTypeEntry.getKey()});
          // @DoInFuture("Write response examples to extensions during import and definition")
          String body = getMockResponseBodyString(mediaTypeEntry, apis);
          responses.add(toAssocMockApisResponse(uidGenerator, mockServiceId, mockApi, name,
              priority--, entry, headers, body));
        }
      } else {
        String name = message(MESSAGE_MATCH_STATUS_RESPONSE_NAME, new Object[]{entry.getKey()});
        responses.add(toAssocMockApisResponse(uidGenerator, mockServiceId, mockApi, name,
            priority--, entry, headers, ""));
      }
    }
  }


  public static @NotNull List<cloud.xcan.angus.model.element.mock.apis.MockApis> toAngusMockApis(
      List<MockApis> mockApisDb, Map<Long, List<MockApisResponse>> mockApisResponsesMap) {
    List<cloud.xcan.angus.model.element.mock.apis.MockApis> angusMockApis = new ArrayList<>();
    for (MockApis mockApis : mockApisDb) {
      cloud.xcan.angus.model.element.mock.apis.MockApis angusMockApi =
          new cloud.xcan.angus.model.element.mock.apis.MockApis();
      angusMockApi.setName(mockApis.getName())
          .setDescription(mockApis.getDescription())
          .setMethod(PathItem.HttpMethod.valueOf(mockApis.getMethod().name()))
          .setEndpoint(mockApis.getEndpoint());
      if (isNotEmpty(mockApisResponsesMap) && mockApisResponsesMap.containsKey(mockApis.getId())) {
        List<MockResponse> responses = new ArrayList<>();
        for (MockApisResponse response : mockApisResponsesMap.get(mockApis.getId())) {
          responses.add(MockResponse.newBuilder()
              .name(response.getName())
              .match(response.getMatch())
              .content(response.getContent())
              .pushback(response.getPushback())
              .build());
        }
        angusMockApi.setResponses(responses);
      }
      angusMockApis.add(angusMockApi);
    }
    return angusMockApis;
  }

}
