package cloud.xcan.angus.model.remoting;

import static cloud.xcan.angus.spec.experimental.BizConstant.DEFAULT_PARAM_VALUE_LENGTH;

import cloud.xcan.angus.api.enums.PrintLevel;
import cloud.xcan.angus.mockservice.api.MockServiceConstant;
import cloud.xcan.angus.spec.annotations.Beta;
import cloud.xcan.angus.spec.annotations.DoInFuture;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class MockServiceSetting {

  //  @Min(1)
  //  @Schema(requiredMode = RequiredMode.REQUIRED)
  //  private long serviceId;

  //  @Min(1)
  //  @Schema(requiredMode = RequiredMode.REQUIRED)
  //  private long deviceId;

  @JsonIgnore
  @Beta
  @Length(max = DEFAULT_PARAM_VALUE_LENGTH)
  @Schema(example = "-Xms128m -Xmx256m")
  private String jvmArgs;

  ////////////////////Mock Service Configuration/////////////////////////
  //  @NotEmpty
  //  @IPv4
  //  @Schema(requiredMode = RequiredMode.REQUIRED)
  //  private String serverIp = MockServiceConstant.DEFAULT_SERVER_IP;

  //  @Port
  //  @Schema(requiredMode = RequiredMode.REQUIRED)
  //  private int serverPort = MockServiceConstant.DEFAULT_SERVER_PORT;

  @DoInFuture("Support ssl: io.netty.handler.ssl.NotSslRecordException: not an SSL/TLS record")
  @Schema(description = "Enable SSL options on netty http server, default false", requiredMode = RequiredMode.REQUIRED)
  private boolean useSsl = MockServiceConstant.DEFAULT_USE_SSL;

  @Min(1)
  @Max(MockServiceConstant.MAX_WORK_THREAD)
  @Schema(description = "The number of threads processing requests, max 10000, default 256", requiredMode = RequiredMode.REQUIRED)
  private int workThreadNum = MockServiceConstant.DEFAULT_WORK_THREAD;

  @Schema(description = "Enable netty log. It is recommended to open it only in debug mode, default false", requiredMode = RequiredMode.REQUIRED)
  private boolean enableNettyLog = MockServiceConstant.DEFAULT_ENABLE_NETTY_LOG;

  @NotNull
  @Schema(description = """
      Configure to log file the request information level, including four options: NONE, BASIC, HEADERS and FULL.
      - NONE: No logging.
      - BASIC: Log only the request method and URL and the response status code and execution time, default value.
      - HEADERS: Log the basic information along with request and response headers.
      - FULL: Log the headers, body, and metadata for both requests and responses.""", requiredMode = RequiredMode.REQUIRED)
  private PrintLevel logFileLevel = MockServiceConstant.DEFAULT_REQUEST_LOG_LEVEL;

  @Schema(description = "Whether to send mock request logs to the server-side, default true.", requiredMode = RequiredMode.REQUIRED)
  private boolean sendRequestLog = MockServiceConstant.DEFAULT_SEND_REQUEST_LOG;

  @Schema(description = "Maximum allowed request size, default 1000 * 1024 * 1024 (1000MB)", requiredMode = RequiredMode.REQUIRED)
  private int maxContentLength = MockServiceConstant.DEFAULT_MAX_CONTENT_LENGTH;

  @Max(MockServiceConstant.MAX_WORK_THREAD)
  @Schema(description = "The number of threads processing pushback requests, default 8", requiredMode = RequiredMode.REQUIRED)
  private int workPushbackThreadNum = MockServiceConstant.DEFAULT_PUSHBACK_THREAD_NUM;

  @Schema(description = "Maximum pushback connect timeout, unit millisecond, default 5000", requiredMode = RequiredMode.REQUIRED)
  private int maxPushbackConnectTimeout = MockServiceConstant.DEFAULT_PUSHBACK_CONNECTION_TIMEOUT;

  @Schema(description = "Maximum pushback request timeout, unit millisecond, default -1 not to timeout", requiredMode = RequiredMode.REQUIRED)
  private int maxPushbackRequestTimeout = MockServiceConstant.DEFAULT_PUSHBACK_REQUEST_TIMEOUT;

}
