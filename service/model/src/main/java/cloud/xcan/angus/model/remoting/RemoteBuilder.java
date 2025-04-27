package cloud.xcan.angus.model.remoting;

import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT_10;
import static cloud.xcan.angus.spec.SpecConstant.DateFormat.DATE_FMT_4;

import cloud.xcan.angus.spec.experimental.Assert;
import cloud.xcan.angus.spec.jackson.EnumModule;
import cloud.xcan.angus.spec.jackson.serializer.TimeValueDeSerializer;
import cloud.xcan.angus.spec.jackson.serializer.TimeValueSerializer;
import cloud.xcan.angus.spec.unit.TimeValue;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import feign.Feign;
import feign.Request;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class RemoteBuilder {

  private RemoteBuilder() {
  }

  public static ObjectMapper buildDefaultObjectMapper() {
    ObjectMapper mapper = new ObjectMapper()
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)
        .configure(SerializationFeature.INDENT_OUTPUT, true)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        .configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    // SDF config
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addSerializer(Long.TYPE, new ToStringSerializer(Long.TYPE));
    simpleModule.addSerializer(Long.class, new ToStringSerializer(Long.class));

    simpleModule.addSerializer(LocalDateTime.class,
        new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_FMT)));
    simpleModule.addSerializer(LocalDate.class,
        new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FMT_4)));
    simpleModule.addSerializer(LocalTime.class,
        new LocalTimeSerializer(DateTimeFormatter.ofPattern(DATE_FMT_10)));

    simpleModule.addDeserializer(LocalDateTime.class,
        new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_FMT)));
    simpleModule.addDeserializer(LocalDate.class,
        new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FMT_4)));
    simpleModule.addDeserializer(LocalTime.class,
        new LocalTimeDeserializer(DateTimeFormatter.ofPattern(DATE_FMT_10)));

    simpleModule.addSerializer(TimeValue.class, new TimeValueSerializer());
    simpleModule.addDeserializer(TimeValue.class, new TimeValueDeSerializer());
    mapper.registerModule(simpleModule);
    mapper.registerModule(new JavaTimeModule());
    mapper.registerModule(new EnumModule());

    return mapper;
  }

  public static MockApisLogOpen2pRemote buildMockApisLogOpen2pRemote(String serverUrl) {
    return buildMockApisLogOpen2pRemote(serverUrl, buildDefaultObjectMapper());
  }

  public static MockApisLogOpen2pRemote buildMockApisLogOpen2pRemote(
      String serverUrl, ObjectMapper mapper) {
    Assert.assertNotNull(serverUrl, "Remote serverUrl is required");
    return Feign.builder()
        .encoder(new JacksonEncoder(mapper))
        .decoder(new JacksonDecoder(mapper))
        .options(new Request.Options(10, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, false))
        .target(MockApisLogOpen2pRemote.class, serverUrl);
  }

  public static MockApisOpen2pRemote buildMockApisOpen2pRemote(String serverUrl) {
    return buildMockApisOpen2pRemote(serverUrl, buildDefaultObjectMapper());
  }

  public static MockApisOpen2pRemote buildMockApisOpen2pRemote(
      String serverUrl, ObjectMapper mapper) {
    Assert.assertNotNull(serverUrl, "Remote serverUrl is required");
    return Feign.builder()
        .encoder(new JacksonEncoder(mapper))
        .decoder(new JacksonDecoder(mapper))
        .options(new Request.Options(10, TimeUnit.SECONDS, 60, TimeUnit.SECONDS, false))
        .target(MockApisOpen2pRemote.class, serverUrl);
  }

}
