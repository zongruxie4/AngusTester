export type PermissionKey = 'TEST' | 'VIEW' | 'MODIFY' | 'DELETE' | 'EXPORT' | 'GRANT' | 'COLON';

export type ScriptInfo = {
  auth: boolean;
  content: string;
  createdBy: string;
  createdByName: string;
  createdDate: string;
  description: string;
  id: string;
  lastModifiedBy: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
  name: string;
  plugin: string;
  projectId: string;
  serviceId: string;
  serviceName: string;
  source: {
    value: 'API' | 'IMPORTED' | 'SCENARIO' | 'SERVICE_SECURITY' | 'SERVICE_SMOKE' | 'USER_DEFINED';
    message: string;
  };
  sourceId: string;
  sourceName: string;
  tags: string[];
  type: {
    value: 'MOCK_APIS' | 'MOCK_DATA' | 'TEST_CUSTOMIZATION' | 'TEST_FUNCTIONALITY' | 'TEST_PERFORMANCE' | 'TEST_STABILITY';
    message: string;
  };
}

export type ResourceInfo = {
  totalScriptNum: string;
  perfScriptNum: string;
  functionalScriptNum: string;
  stabilityScriptNum: string;
  customizationScriptNum: string;
  mockDataScriptNum: string;
  mockApisScriptNum: string;
  createdSourceNum: string;
  importedSourceNum: string;
  apisSourceNum: string;
  caseSourceNum: string;
  scenarioSourceNum: string;
}

export type FormState = {
  name: string | undefined;
  type: 'MOCK_APIS' | 'MOCK_DATA' | 'TEST_CUSTOMIZATION' | 'TEST_FUNCTIONALITY' | 'TEST_PERFORMANCE' | 'TEST_STABILITY' | undefined,
  typeName: string | undefined;
  description: string | undefined;
}
