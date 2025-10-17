import { EnumMessage, HttpMethod } from '@xcan-angus/infra';
import { ApiImportSource, ApiSource, ApisProtocol, ApiStatus } from '@/enums/enums';
import { OpenAPIV3 } from '@/types/openapi-types';

export type ApisListInfo = {
  id: string;
  source: EnumMessage<ApiSource>;
  importSource: EnumMessage<ApiImportSource>;
  serviceId: string;
  serviceName: string;
  protocol: EnumMessage<ApisProtocol>;
  method: HttpMethod;
  endpoint: string;
  summary: string;
  operationId: string;
  // servers: Server[];
  deprecated: boolean;
  status: EnumMessage<ApiStatus>;
  favourite: boolean;
  follow: boolean;
  tenantId: string;
  createdBy: string;
  createdByName: string;
  ownerId: string;
  ownerName: string;
  avatar: string;
  createdDate: string;
  lastModifiedDate: string;
  auth: boolean;
  serviceAuth: boolean;
  /**
   * Whether to enable functional testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis
   */
  testFunc?: boolean;
  testFuncPassed?: boolean;
  testFuncFailureMessage: string;
  /**
   * Whether to enable performance testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis.
   */
  testPerf?: boolean;
  testPerfPassed?: boolean;
  testPerfFailureMessage: string;
  /**
   * Whether to enable stability testing, default enabled.
   * <p>
   * After enabled, the test results will be included in the efficiency analysis
   */
  testStability?: boolean;
  testStabilityPassed?: boolean;
  testStabilityFailureMessage: string;
  tags: string[];
  mockServiceId: string;
  mockApisId: string;
}
export type ApisUnarchivedListInfo = {
  id: string;
  projectId: string;
  protocol: EnumMessage<ApisProtocol>;
  method: string;
  endpoint: string;
  summary: string;
  createdDate: string;
  lastModifiedDate: string;
}

// Detail VO mapped from backend class `ApisUnarchivedDetailVo`
export interface ApisUnarchivedDetail {
  id?: string;
  projectId?: string;

  protocol?: EnumMessage<ApisProtocol>;
  method?: string;
  endpoint?: string;

  // OpenAPI Document fields
  summary?: string;
  description?: string;
  operationId?: string;
  parameters?: (OpenAPIV3.ReferenceObject | OpenAPIV3.ParameterObject)[];
  requestBody?: OpenAPIV3.ReferenceObject | OpenAPIV3.RequestBodyObject;
  responses?: OpenAPIV3.ResponsesObject;
  security?: OpenAPIV3.SecurityRequirementObject[];
  currentServer?: OpenAPIV3.ServerObject;
  extensions?: Record<string, any>;

  // Security/authentication
  authentication?: OpenAPIV3.SecuritySchemeObject;
  assertions?: any[];
  auth?: boolean;
  secured?: boolean;

  tenantId?: string;
  createdBy?: string;
  createdDate?: string;
  lastModifiedDate?: string;
}
