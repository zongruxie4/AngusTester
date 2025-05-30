<script setup lang="ts">
import Oas from 'oas';
import qs from 'qs';
import { inject, ref, watch } from 'vue';
import { notification, Select, FormatHighlight } from '@xcan-angus/vue-ui';
import XML from 'xml';
import { Button, RadioGroup } from 'ant-design-vue';
// import { supportedLanguages, oasToSnippet } from '@readme/oas-to-snippet';
import oasToSnippet from '@readme/oas-to-snippet';
import { getSupportedLanguages } from '@readme/oas-to-snippet/languages';
import { clipboard } from '@xcan-angus/tools';
import SwaggerUI from '@xcan-angus/swagger-ui';
import JSONToSchema from 'json-to-schema' ;

import { API_EXTENSION_KEY } from '@/views/apis/utils';
import { apis } from 'src/api/tester';
import cIcon from './image/c.png';
import cSharpIcon from './image/csharp.png';
import cPlus from './image/c++.png';
import goIcon from './image/go.png';
import javaIcon from './image/java.png';
import jsIcon from './image/js.png';
import nodeIcon from './image/node.png';
import phpIcon from './image/php.png';
import pythonIcon from './image/python.png';
import rubyIcon from './image/ruby.png';
import shellIcon from './image/shell.png';
import { deepDelAttrFromObj } from '@/views/apis/services/apiHttp/utils';

interface Props {
  id: string;
  from?: 'list'|undefined
}

const props = withDefaults(defineProps<Props>(), {
  id: ''
});

const supportedLanguages = getSupportedLanguages();

const getParameter = inject('getParameter', () => ({} as Record<string, any>));
const apiInfo = ref<{[key:string]: any}>({});
const languageStr = ['shell', 'php', 'javascript', 'node', 'java', 'cplusplus', 'csharp', 'c', 'python', 'ruby', 'go'];
const { valueKey, serverSourceKey, fileNameKey } = API_EXTENSION_KEY;
const codeOptions = ref<any[]>([]);
const getCodeOptions = () => {
  Object.keys(supportedLanguages).forEach(l => {
    Object.keys(supportedLanguages[l].httpsnippet.targets).forEach(target => {
      codeOptions.value.push({
        l: l,
        label: `${l}_${target}`,
        target: target,
        highlight: supportedLanguages[l].highlight,
        ...supportedLanguages[l].httpsnippet.targets[target]
      });
    });
  });
};

const getLanguageLabel = (key) => {
  if (key === 'cplusplus') {
    return 'c++';
  }
  if (key === 'csharp') {
    return 'c#';
  }
  // if (key === 'clojure') {
  //   return 'c';
  // }
  return key;
};

const getIconSrc = (key) => {
  if (key === 'c') {
    return cIcon;
  }
  if (key === 'cplusplus') {
    return cPlus;
  }

  if (key === 'csharp') {
    return cSharpIcon;
  }
  if (key === 'go') {
    return goIcon;
  }
  if (key === 'java') {
    return javaIcon;
  }
  if (key === 'javascript') {
    return jsIcon;
  }
  if (key === 'node') {
    return nodeIcon;
  }
  if (key === 'php') {
    return phpIcon;
  }
  if (key === 'python') {
    return pythonIcon;
  }
  if (key === 'ruby') {
    return rubyIcon;
  }
  if (key === 'shell') {
    return shellIcon;
  }
};

const language = ref<string[]>([]);
const langFuncs = ref<{value: string, label: string}[]>([]);

const codeContent = ref();

const getCodeContent = async () => {
  const parameter = await (apiInfo.value.id ? getSelectApiParameter() : getParameter());
  if (!parameter?.requestBody?.content) {
    delete parameter.requestBody;
  }
  if (parameter?.requestBody?.$ref) {
    delete parameter.requestBody;
  }
  const contentTypeParameter = (parameter.parameters || []).find(i => i.in === 'header' && i.name === 'Content-Type');
  if (contentTypeParameter) {
    const contentType = contentTypeParameter[valueKey];
    parameter.contentType = contentType;
    if (!contentType) {
      delete parameter.requestBody;
    } else {
      Object.keys(parameter.requestBody?.content || {}).forEach(key => {
        if (key !== contentType) {
          delete parameter.requestBody.content[key];
        }
      });
    }
  } else {
    delete parameter.requestBody;
  }
  if (parameter.requestBody) {
    parameter.requestBody = deepDelAttrFromObj(parameter.requestBody, ['$ref']);
  }

  const query = {};
  const path = {};
  const header = {};
  (parameter?.parameters || []).forEach(i => {
    if (i.in === 'query') {
      if (!i.name && !i[valueKey]) {
        return;
      }
      query[i.name] = i[valueKey];
    }
    if (i.in === 'path') {
      path[i.name] = i[valueKey];
    }
    if (i.in === 'header') {
      header[i.name] = i[valueKey];
    }
  });
  const server = parameter.currentServer;
  const contentType = (parameter.parameters || []).find(i => i.in === 'header' && i.name === 'Content-Type')?.[valueKey];
  const formParam = {};
  if (contentType === 'application/x-www-form-urlencoded') {
    Object.entries(parameter.requestBody?.content['application/x-www-form-urlencoded']?.schema?.properties || {}).forEach(arr => {
      formParam[arr[0]] = arr[1];
    });
  }
  let body;
  if (contentType === 'multipart/form-data') {
    // body = new FormData();
    body = {};
    const formContent = parameter.requestBody.content['multipart/form-data']?.schema?.properties || {};

    Object.keys(formContent).forEach((key) => {
      if (formContent[key].format === 'binary') {
        if (formContent[key].type === 'string') {
          try {
            let fileValueObj = formContent[key][valueKey];
            if (typeof formContent[key][valueKey] === 'string') {
              fileValueObj = JSON.parse(formContent[key][valueKey]);
            }
            body[key] = fileValueObj[fileNameKey];
            // body.append(key, fileValueObj.file, formContent[key][fileNameKey] || '');
          } catch {}
        } else {
          let fileValueArr = formContent[key][valueKey];
          try {
            if (typeof fileValueArr === 'string') {
              fileValueArr = JSON.parse(fileValueArr);
            }
            (fileValueArr || []).forEach(file => {
              body[key] = file[fileNameKey];
              // body.append(key, file.file, file[fileNameKey]);
            });
          } catch {}
        }
      } else {
        // body.append(key, formContent[key]);
        body[key] = formContent[key];
      }
    });
    parameter.requestBody.content['multipart/form-data'].schema = JSONToSchema(body);


  } else if (contentType === 'application/octet-stream' || parameter.requestBody?.content?.[contentType]?.schema?.format === 'binary') {
    body = parameter.requestBody?.content[contentType]?.schema?.[valueKey];
  } else {
    if (valueKey in (parameter.requestBody?.content?.[contentType] || {})) {
      body = parameter.requestBody?.content?.[contentType]?.[valueKey] || undefined;
    } else if (parameter.requestBody?.content?.[contentType]) {
      let isxml = false;
      if (contentType === 'application/xml') {
        isxml = true;
      }
      parameter.requestBody.content[contentType][valueKey] = SwaggerUI.extension.sampleFromSchemaGeneric(parameter.requestBody.content[contentType].schema || {}, { useValue: true }, undefined, isxml);
      body = parameter.requestBody?.content?.[contentType]?.[valueKey] || undefined;
      if (isxml && typeof body === 'object' && (!body || typeof body.notagname !== 'string')) {
        body = XML(body);
      }
    } else {
      body = undefined;
    }
    try {
      body = JSON.parse(body)
    } catch {}
    if (body && typeof body === 'object') {
      parameter.requestBody.content[contentType].schema = JSONToSchema(body);
    } else if (body && typeof body === 'string') {
      parameter.requestBody.content[contentType].schema = {
        type: 'string'
      };
    }
  }
  const apiDefinition = new Oas({ ...parameter, servers: [parameter.currentServer] });


  const formData = {
    query,
    path,
    header,
    body,
    formData: formParam,
    server: {
      selected: 0,
      ...server
    }
  };
  // const url = server.url + parameter.endpoint;

  const auth = parameter.authentication?.type
    ? {
        [parameter.authentication.type]: parameter.authentication.scheme
      }
    : {};
  const queryString = qs.stringify(query);
  parameter.path = (parameter.endpoint || '') + (queryString ? '?' + queryString : '');
  parameter.url = server;

  const { code } = await oasToSnippet(apiDefinition, parameter, formData, auth, language.value);
  codeContent.value = code;
};

const selectLanguage = (funcs, l) => {
  language.value = [l, funcs.httpsnippet.default];
  langFuncs.value = Object.keys(funcs.httpsnippet.targets).map(i => ({ value: i, label: i }));
};

const copyCode = () => {
  clipboard.toClipboard(codeContent.value)
    .then(() => {
      notification.success('复制成功');
    });
};

// 组装使用的数据
const getSelectApiParameter = async () => {
  let { parameters, requestBody, endpoint, currentServer, method, authentication, protocol, availableServers = [], summary, ownerId, ownerName } = apiInfo.value;
  currentServer = currentServer || availableServers.find(i => i[serverSourceKey] === 'CURRENT_REQUEST') || availableServers[0] || { url: '' };
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

const replaceSchemaRef = (api = {}) => {
  const { resolvedRefModels = {} } = api;
  function replaceObj (obj = {}) {
    Object.keys(obj).forEach(key => {
      if (key === '$ref') {
        const schema = resolvedRefModels[obj[key]];
        if (schema) {
          obj = {
            ...obj,
            ...JSON.parse(schema)
          };
        }
        delete obj.$ref;
      } else if (Object.prototype.toString.call(obj[key]) === '[object Object]') {
        obj[key] = replaceObj(obj[key]);
      }
    });
    return obj;
  }
  return replaceObj(api);
};

const loadApiParams = async () => {
  const [error, resp] = await apis.loadInfo(props.id, true);
  if (error) {
    return;
  }
  apiInfo.value = replaceSchemaRef(resp.data || {});
};

const refresh = () => {
  getCodeContent();
};

watch(() => language.value, newValue => {
  if (newValue.length) {
    getCodeContent();
  }
}, {
  deep: true
});

watch(() => props.id, async () => {
  getCodeOptions();
  if (props.from === 'list') {
    await loadApiParams();
  } else {
    apiInfo.value = {};
  }
  selectLanguage(supportedLanguages[languageStr[0]], languageStr[0]);
}, {
  immediate: true
});

// class="inline-block  min-w-20 min-h-15 text-center cursor-pointer text-3.5 pt-1 bg-gray-light border align-top rounded"
</script>
<template>
  <div class="flex flex-col mt-2">
    <div class="overflow-auto py-2 space-x-2 whitespace-nowrap">
      <div
        v-for="value in languageStr"
        :key="value"
        class="inline-block  min-w-15 min-h-10 text-center cursor-pointer text-3 pt-1 bg-gray-light border align-top rounded"
        :class="{'border-blue-1': language[0] === value}"
        @click="selectLanguage(supportedLanguages[value], value)">
        <img class="w-7 inline-block" :src="getIconSrc(value)" />
        <div class="bg-gray-bg-active mt-1">{{ getLanguageLabel(value) }}</div>
      </div>
    </div>

    <div class="flex space-x-5 items-center">
      <RadioGroup
        v-model:value="language[1]"
        class="inline-block"
        :options="langFuncs"/>
      <div>
        <Button
          type="link"
          size="small"
          class="text-left inline-block"
          @click="copyCode">
          复制
        </Button>
        <Button
          type="link"
          size="small"
          class="text-left  inline-block"
          @click="refresh">
          刷新
        </Button>
      </div>
    </div>
    <div class="py-2 whitespace-pre-wrap break-all flex-1 overflow-auto">
      <!-- <div>{{ codeContent }}</div> -->
      <div v-show="codeContent" class="code-wrapper">
        <FormatHighlight
          format="preview"
          :dataSource="codeContent"
          :dataType="language[0]">
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
