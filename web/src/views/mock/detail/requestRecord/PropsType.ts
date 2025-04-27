
export interface DataRecordType {
  id: string,
  apiName: string,
  apiId: string,
  method: string,
  endpoint: string
}

export interface DataInfoType {
  id: string,
  apiId: string,
  method: string,
  protocol: string,
  queryParam: string,
  remote: string,
  requestBody: string,
  requestDate: string,
  requestHeaders: Record<string, any>,
  responseBody: string,
  responseDate: string,
  responseHeaders: Record<string, any>,
  responseSize: string,
  status: string,
  tenantId: string,
  endpoint: string,
  url: string
}

export type ApiLogSetting = {
  enabled: boolean;
  loggerService: string;
  eventService: string;
  clearBeforeDay: string;
  userRequest: {
      enabled: boolean;
      printLevel: {
          value: string;
          message: string;
      },
      maxPayloadLength: string;
      defaultIgnorePattern: string;
      customIgnorePattern: string;
      pushLoggerService: boolean;
      pushLoggerServiceIgnorePattern:string;
  },
  systemRequest: {
      enabled: boolean;
      printLevel: {
          value: string;
          message: string;
      },
      maxPayloadLength: string;
      defaultIgnorePattern: string;
      customIgnorePattern: string;
      pushLoggerService: boolean;
      pushLoggerServiceIgnorePattern:string;
  }
}
