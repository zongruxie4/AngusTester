<script setup lang="ts">
// External dependencies
import Oas from 'oas';
import qs from 'qs';
import { inject, ref, watch } from 'vue';
import { notification, FormatHighlight } from '@xcan-angus/vue-ui';
import XML from 'xml';
import { Button, RadioGroup } from 'ant-design-vue';
import oasToSnippet from '@readme/oas-to-snippet';
import { getSupportedLanguages } from '@readme/oas-to-snippet/languages';
import { toClipboard } from '@xcan-angus/infra';
import SwaggerUI from '@xcan-angus/swagger-ui';
import JSONToSchema from 'json-to-schema';
import { useI18n } from 'vue-i18n';

// Internal dependencies
import ApiUtils from '@/utils/apis';
import { apis } from '@/api/tester';
import { CONTENT_TYPE, RADIO_TYPE, HTTP_HEADERS } from '@/utils/constant';
import { deepDelAttrFromObj } from '@/views/apis/services/protocol/http/utils';

// Language icons
import cIcon from '@/views/apis/services/components/image/c.png';
import cSharpIcon from '@/views/apis/services/components/image/csharp.png';
import cPlus from '@/views/apis/services/components/image/c++.png';
import goIcon from '@/views/apis/services/components/image/go.png';
import javaIcon from '@/views/apis/services/components/image/java.png';
import jsIcon from '@/views/apis/services/components/image/js.png';
import nodeIcon from '@/views/apis/services/components/image/node.png';
import phpIcon from '@/views/apis/services/components/image/php.png';
import pythonIcon from '@/views/apis/services/components/image/python.png';
import rubyIcon from '@/views/apis/services/components/image/ruby.png';
import shellIcon from '@/views/apis/services/components/image/shell.png';

// Constants and configuration
const { API_EXTENSION_KEY } = ApiUtils;
const supportedLanguages = getSupportedLanguages();
const supportedLanguageList = ['shell', 'php', 'javascript', 'node', 'java', 'cplusplus', 'csharp', 'c', 'python', 'ruby', 'go'];

// Props and reactive data
interface Props {
  id: string;
  from?: 'list'|undefined
}

const props = withDefaults(defineProps<Props>(), {
  id: ''
});

const { t } = useI18n();
const getParameter = inject('getParameter', () => ({} as Record<string, any>));

// Reactive state
const apiInfo = ref<{[key:string]: any}>({});
const selectedLanguage = ref<string[]>([]);
const languageFunctions = ref<{value: string, label: string}[]>([]);
const codeContent = ref<string>('');
const codeOptions = ref<any[]>([]);

// API extension keys
const { valueKey, serverSourceKey, fileNameKey, oAuth2Key, oAuth2Token, newTokenKey } = API_EXTENSION_KEY;

/**
 * Initialize code options for all supported languages
 */
const initializeCodeOptions = () => {
  Object.keys(supportedLanguages).forEach(languageKey => {
    Object.keys(supportedLanguages[languageKey].httpsnippet.targets).forEach(target => {
      codeOptions.value.push({
        l: languageKey,
        label: `${languageKey}_${target}`,
        target: target,
        highlight: supportedLanguages[languageKey].highlight,
        ...supportedLanguages[languageKey].httpsnippet.targets[target]
      });
    });
  });
};

/**
 * Get display label for programming language
 */
const getLanguageDisplayLabel = (languageKey: string): string => {
  const languageMap: Record<string, string> = {
    cplusplus: 'c++',
    csharp: 'c#'
  };
  return languageMap[languageKey] || languageKey;
};

/**
 * Get icon source for programming language
 */
const getLanguageIcon = (languageKey: string): string => {
  const iconMap: Record<string, string> = {
    c: cIcon,
    cplusplus: cPlus,
    csharp: cSharpIcon,
    go: goIcon,
    java: javaIcon,
    javascript: jsIcon,
    node: nodeIcon,
    php: phpIcon,
    python: pythonIcon,
    ruby: rubyIcon,
    shell: shellIcon
  };
  return iconMap[languageKey] || '';
};

/**
 * Process request body content based on content type
 */
const processRequestBody = (parameter: any, contentType: string) => {
  if (!parameter?.requestBody?.content) {
    delete parameter.requestBody;
    return;
  }

  if (parameter?.requestBody?.$ref) {
    delete parameter.requestBody;
    return;
  }

  // Filter content by content type
  if (contentType) {
    Object.keys(parameter.requestBody?.content || {}).forEach(key => {
      if (key !== contentType) {
        delete parameter.requestBody.content[key];
      }
    });
  } else {
    delete parameter.requestBody;
  }

  if (parameter.requestBody) {
    parameter.requestBody = deepDelAttrFromObj(parameter.requestBody, ['$ref']);
  }
};

/**
 * Extract parameters by type (query, path, header)
 */
const extractParameters = (parameters: any[]) => {
  const queryParams = {};
  const pathParams = {};
  const headerParams = {};

  (parameters || []).forEach(param => {
    if (param.in === 'query' && (param.name || param[valueKey])) {
      queryParams[param.name] = param[valueKey];
    } else if (param.in === 'path') {
      pathParams[param.name] = param[valueKey];
    } else if (param.in === 'header') {
      headerParams[param.name] = param[valueKey];
    }
  });

  return { queryParams, pathParams, headerParams };
};

/**
 * Process form data for URL encoded content
 */
const processFormData = (parameter: any, contentType: string) => {
  const formParams = {};

  if (contentType === CONTENT_TYPE.FORM_URLENCODED) {
    Object.entries(parameter.requestBody?.content[CONTENT_TYPE.FORM_URLENCODED]?.schema?.properties || {}).forEach(([key, value]) => {
      formParams[key] = value?.[valueKey];
    });
  }

  return formParams;
};

/**
 * Process multipart form data with file handling
 */
const processMultipartFormData = (parameter: any) => {
  const body = {};
  const formContent = parameter.requestBody.content[CONTENT_TYPE.MULTIPART_FORM_DATA]?.schema?.properties || {};

  Object.keys(formContent).forEach((key) => {
    if (formContent[key].format === 'binary') {
      if (formContent[key].type === 'string') {
        try {
          let fileValueObj = formContent[key][valueKey];
          if (typeof formContent[key][valueKey] === 'string') {
            fileValueObj = JSON.parse(formContent[key][valueKey]);
          }
          body[key] = fileValueObj[fileNameKey];
        } catch (error) {
          console.warn('Failed to parse file value:', error);
        }
      } else {
        let fileValueArr = formContent[key][valueKey];
        try {
          if (typeof fileValueArr === 'string') {
            fileValueArr = JSON.parse(fileValueArr);
          }
          (fileValueArr || []).forEach(file => {
            body[key] = file[fileNameKey];
          });
        } catch (error) {
          console.warn('Failed to parse file array:', error);
        }
      }
    } else {
      body[key] = formContent[key];
    }
  });

  parameter.requestBody.content[CONTENT_TYPE.MULTIPART_FORM_DATA].schema = JSONToSchema(body);
  return body;
};

/**
 * Process request body based on content type
 */
const processRequestBodyContent = (parameter: any, contentType: string) => {
  let body;

  if (contentType === CONTENT_TYPE.MULTIPART_FORM_DATA) {
    body = processMultipartFormData(parameter);
  } else if (contentType === RADIO_TYPE.OCTET_STREAM || parameter.requestBody?.content?.[contentType]?.schema?.format === 'binary') {
    body = parameter.requestBody?.content[contentType]?.schema?.[valueKey];
  } else {
    if (valueKey in (parameter.requestBody?.content?.[contentType] || {})) {
      body = parameter.requestBody?.content?.[contentType]?.[valueKey] || undefined;
    } else if (parameter.requestBody?.content?.[contentType]) {
      const isXml = contentType === CONTENT_TYPE.XML;

      parameter.requestBody.content[contentType][valueKey] = SwaggerUI.extension.sampleFromSchemaGeneric(
        parameter.requestBody.content[contentType].schema || {},
        { useValue: true },
        undefined,
        isXml
      );

      body = parameter.requestBody?.content?.[contentType]?.[valueKey] || undefined;

      if (isXml && typeof body === 'object' && (!body || typeof body.notagname !== 'string')) {
        body = XML(body);
      }
    } else {
      body = undefined;
    }

    // Try to parse JSON body
    try {
      body = JSON.parse(body);
    } catch (error) {
      // Body is not JSON, keep as is
    }

    // Update schema based on body type
    if (body && typeof body === 'object') {
      parameter.requestBody.content[contentType].schema = JSONToSchema(body);
    } else if (body && typeof body === 'string') {
      parameter.requestBody.content[contentType].schema = {
        type: 'string'
      };
    }
  }

  return body;
};

/**
 * Process authentication configuration
 */
const processAuthentication = (parameter: any) => {
  const auth: Record<string, any> = {};

  if (!parameter.authentication?.type) {
    return auth;
  }

  parameter.authentication = {
    ...parameter.authentication,
    ...(parameter.authentication?.extensions || {})
  };

  if (parameter.authentication.type === 'http') {
    if (parameter.authentication.scheme === 'bearer') {
      auth.Authorization = parameter.authentication[valueKey];
    } else {
      auth.Authorization = {
        user: parameter.authentication.name,
        pass: parameter.authentication[valueKey]
      };
    }
  } else if (parameter.authentication.type === 'oauth2') {
    if (parameter.authentication[newTokenKey]) {
      if (parameter.authentication[oAuth2Key] === 'password') {
        auth.access_token = parameter.authentication.flows?.password?.[oAuth2Token] || '';
      }
      if (parameter.authentication[oAuth2Key] === 'clientCredentials') {
        auth.access_token = parameter.authentication.flows?.clientCredentials?.[oAuth2Token] || '';
      }
    } else {
      auth.access_token = parameter.authentication[oAuth2Token] || '';
    }
  }

  return auth;
};

/**
 * Generate code snippet for the selected language
 */
const generateCodeSnippet = async () => {
  const parameter = await (apiInfo.value.id ? getSelectedApiParameter() : getParameter());

  // Process content type parameter
  const contentTypeParameter = (parameter.parameters || []).find(i => i.in === 'header' && i.name === HTTP_HEADERS.CONTENT_TYPE);
  const contentType = contentTypeParameter?.[valueKey];

  // Process request body
  processRequestBody(parameter, contentType);

  // Extract parameters
  const { queryParams, pathParams, headerParams } = extractParameters(parameter.parameters);

  // Process form data
  const formParams = processFormData(parameter, contentType);

  // Process request body content
  const body = processRequestBodyContent(parameter, contentType);

  // Create API definition
  const apiDefinition = new Oas(parameter as any);

  // Prepare form data for code generation
  const formData = {
    query: queryParams,
    path: pathParams,
    header: headerParams,
    body,
    formData: formParams,
    server: {
      selected: 0,
      ...parameter.currentServer
    }
  };

  // Process authentication
  const auth = processAuthentication(parameter);

  // Build query string and update parameter
  const queryString = qs.stringify(queryParams);
  (parameter as any).path = (parameter.endpoint || '') + (queryString ? '?' + queryString : '');
  (parameter as any).url = parameter.currentServer;

  // Generate code snippet
  const { code } = await oasToSnippet(apiDefinition, parameter, formData, auth, selectedLanguage.value);
  codeContent.value = code as string;
};

/**
 * Select programming language and update available functions
 */
const selectProgrammingLanguage = (languageFunctions: any, languageKey: string) => {
  selectedLanguage.value = [languageKey, languageFunctions.httpsnippet.default];
  languageFunctions.value = Object.keys(languageFunctions.httpsnippet.targets).map(func => ({
    value: func,
    label: func
  }));
};

/**
 * Copy generated code to clipboard
 */
const copyCodeToClipboard = () => {
  toClipboard(codeContent.value)
    .then(() => {
      notification.success(t('actions.tips.copySuccess'));
    });
};

/**
 * Get API parameter data for selected API
 */
const getSelectedApiParameter = async () => {
  let {
    parameters,
    requestBody,
    endpoint,
    currentServer,
    method,
    authentication,
    protocol,
    availableServers = [],
    summary,
    ownerId,
    ownerName
  } = apiInfo.value;

  currentServer = currentServer ||
    availableServers.find(i => i[serverSourceKey] === 'CURRENT_REQUEST') ||
    availableServers[0] ||
    { url: '' };

  return {
    parameters,
    currentServer,
    requestBody,
    endpoint,
    method: method.value,
    authentication,
    protocol,
    summary,
    ownerId,
    availableServers,
    ownerName
  };
};

/**
 * Replace schema references with actual schema content
 */
const replaceSchemaReferences = (apiData: any = {}) => {
  const { resolvedRefModels = {} } = apiData;

  const replaceObjectReferences = (obj: any = {}) => {
    Object.keys(obj).forEach(key => {
      if (key === '$ref') {
        const schema = resolvedRefModels[obj[key]];
        if (schema) {
          Object.assign(obj, JSON.parse(schema));
        }
        delete obj.$ref;
      } else if (Object.prototype.toString.call(obj[key]) === '[object Object]') {
        obj[key] = replaceObjectReferences(obj[key]);
      }
    });
    return obj;
  };

  return replaceObjectReferences(apiData);
};

/**
 * Load API parameters from server
 */
const loadApiParameters = async () => {
  const [error, response] = await apis.getApiDetail(props.id, true);
  if (error) {
    return;
  }
  apiInfo.value = replaceSchemaReferences(response.data || {});
};

/**
 * Refresh code snippet
 */
const refreshCodeSnippet = () => {
  generateCodeSnippet();
};

// Watchers
watch(() => selectedLanguage.value, (newValue) => {
  if (newValue.length) {
    generateCodeSnippet();
  }
}, {
  deep: true
});

watch(() => props.id, async () => {
  initializeCodeOptions();
  if (props.from === 'list') {
    await loadApiParameters();
  } else {
    apiInfo.value = {};
  }
  selectProgrammingLanguage(supportedLanguages[supportedLanguageList[0]], supportedLanguageList[0]);
}, {
  immediate: true
});
</script>

<template>
  <div class="flex flex-col mt-2">
    <!-- Language selection tabs -->
    <div class="overflow-auto py-2 space-x-2 whitespace-nowrap">
      <div
        v-for="languageKey in supportedLanguageList"
        :key="languageKey"
        class="inline-block min-w-15 min-h-10 text-center cursor-pointer text-3 pt-1 bg-gray-light border align-top rounded"
        :class="{'border-blue-1': selectedLanguage[0] === languageKey}"
        @click="selectProgrammingLanguage(supportedLanguages[languageKey], languageKey)">
        <img class="w-7 inline-block" :src="getLanguageIcon(languageKey)" />
        <div class="bg-gray-bg-active mt-1">{{ getLanguageDisplayLabel(languageKey) }}</div>
      </div>
    </div>

    <!-- Language function selection and action buttons -->
    <div class="flex space-x-5 items-center">
      <RadioGroup
        v-model:value="selectedLanguage[1]"
        class="inline-block"
        :options="languageFunctions" />
      <div>
        <Button
          type="link"
          size="small"
          class="text-left inline-block"
          @click="copyCodeToClipboard">
          {{ t('actions.copy') }}
        </Button>
        <Button
          type="link"
          size="small"
          class="text-left inline-block"
          @click="refreshCodeSnippet">
          {{ t('actions.refresh') }}
        </Button>
      </div>
    </div>

    <!-- Code display area -->
    <div class="py-2 whitespace-pre-wrap break-all flex-1 overflow-auto">
      <div v-show="codeContent" class="code-wrapper">
        <FormatHighlight
          format="preview"
          :dataSource="codeContent"
          :dataType="selectedLanguage[0]">
        </FormatHighlight>
      </div>
    </div>
  </div>
</template>

<style scoped>
.code-wrapper {
  @apply p-2 whitespace-pre-wrap;
  background-color: #f6f6f6;
}

:deep(.code-wrapper) pre code.hljs {
  @apply whitespace-pre-wrap;
}
</style>
