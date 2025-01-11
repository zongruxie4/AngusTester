package cloud.xcan.sdf.model.remoting;

import static cloud.xcan.sdf.spec.experimental.BizConstant.DEFAULT_PARAM_VALUE_LENGTH;

import cloud.xcan.angus.mockservice.api.MockServiceConstant;
import cloud.xcan.sdf.api.enums.PrintLevel;
import cloud.xcan.sdf.spec.annotations.Beta;
import cloud.xcan.sdf.spec.annotations.DoInFuture;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Setter
@Getter
@Accessors(chain = true)
public class MockServiceSetting {

  //  @Min(1)
  //  @ApiModelProperty(required = true)
  //  private long serviceId;

  //  @Min(1)
  //  @ApiModelProperty(required = true)
  //  private long deviceId;

  @JsonIgnore
  @Beta
  @Length(max = DEFAULT_PARAM_VALUE_LENGTH)
  @ApiModelProperty(example = "-Xms128m -Xmx256m")
  private String jvmArgs;

  ////////////////////Mock Service Configuration/////////////////////////
  //  @NotEmpty
  //  @IPv4
  //  @ApiModelProperty(required = true)
  //  private String serverIp = MockServiceConstant.DEFAULT_SERVER_IP;

  //  @Port
  //  @ApiModelProperty(required = true)
  //  private int serverPort = MockServiceConstant.DEFAULT_SERVER_PORT;

  @DoInFuture("Support ssl: io.netty.handler.ssl.NotSslRecordException: not an SSL/TLS record")
  @ApiModelProperty(value = "Enable SSL options on netty http server, default false", required = true)
  private boolean useSsl = MockServiceConstant.DEFAULT_USE_SSL;

  @Min(1)
  @Max(MockServiceConstant.MAX_WORK_THREAD)
  @ApiModelProperty(value = "The number of threads processing requests, max 10000, default 256", required = true)
  private int workThreadNum = MockServiceConstant.DEFAULT_WORK_THREAD;

  @ApiModelProperty(value = "Enable netty log. It is recommended to open it only in debug mode, default false", required = true)
  private boolean enableNettyLog = MockServiceConstant.DEFAULT_ENABLE_NETTY_LOG;

  @NotNull
  @ApiModelProperty(value = "Configure to log file the request information level, including four options: NONE, BASIC, HEADERS and FULL", required = true, notes
      = "- NONE: No logging.\n"
      + "- BASIC: Log only the request method and URL and the response status code and execution time, default value.\n"
      + "- HEADERS: Log the basic information along with request and response headers.\n"
      + "- FULL: Log the headers, body, and metadata for both requests and responses.")
  private PrintLevel logFileLevel = MockServiceConstant.DEFAULT_REQUEST_LOG_LEVEL;

  @ApiModelProperty(value = "Whether to send mock request logs to the server-side, default true.", required = true)
  private boolean sendRequestLog = MockServiceConstant.DEFAULT_SEND_REQUEST_LOG;

  @ApiModelProperty(value = "Maximum allowed request size, default 1000 * 1024 * 1024 (1000MB)", required = true)
  private int maxContentLength = MockServiceConstant.DEFAULT_MAX_CONTENT_LENGTH;

  @Max(MockServiceConstant.MAX_WORK_THREAD)
  @ApiModelProperty(value = "The number of threads processing pushback requests, default 8", required = true)
  private int workPushbackThreadNum = MockServiceConstant.DEFAULT_PUSHBACK_THREAD_NUM;

  @ApiModelProperty(value = "Maximum pushback connect timeout, unit millisecond, default 5000", required = true)
  private int maxPushbackConnectTimeout = MockServiceConstant.DEFAULT_PUSHBACK_CONNECTION_TIMEOUT;

  @ApiModelProperty(value = "Maximum pushback request timeout, unit millisecond, default -1 not to timeout", required = true)
  private int maxPushbackRequestTimeout = MockServiceConstant.DEFAULT_PUSHBACK_REQUEST_TIMEOUT;

}
