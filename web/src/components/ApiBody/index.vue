<script setup lang="ts">
import { computed, defineAsyncComponent, reactive, ref, watch, inject, onMounted, nextTick } from 'vue';
import { Icon, notification, Select, AsyncComponent } from '@xcan-angus/vue-ui';
import { http, duration, TESTER, codeUtils } from '@xcan-angus/infra';
import XML from 'xml';
import pretty from 'pretty';
import jsBeautify from 'js-beautify';
import { Button, RadioGroup, Radio, Upload, Tooltip } from 'ant-design-vue';
import SwaggerUI from '@xcan-angus/swagger-ui';
import { debounce } from 'throttle-debounce';
import { deconstruct, API_EXTENSION_KEY, getDataTypeFromFormat, CONTENT_TYPE, getNewItem, getBodyDefaultItem, ParamsItem, getModelDataByRef } from '@xcan-angus/vue-ui/ApiUtils/index';
import { radioGroups, RequestBodyParam, StateItem, OptionItem, deepParseJson } from './interface';
import { useI18n } from 'vue-i18n';
const { t }  = useI18n();

const { gzip, ungzip } = codeUtils;
export interface Props {
  defaultValue:RequestBodyParam,
  binaryFile?: any;
  contentType: string|null;
  pluginsJSON: Record<string, any>;
  hideImportBtn: boolean;
  viewType: boolean;
  fieldNames?: { valueKey: string; enabledKey: string, fileNameKey: string };
  useFlatForm: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  defaultValue: undefined,
  binaryFile: undefined,
  contentType: null,
  pluginsJSON: () => ({}),
  hideImportBtn: false,
  viewType: false,
  useFlatForm: false,
  fieldNames: () => ({ valueKey: API_EXTENSION_KEY.valueKey, enabledKey: API_EXTENSION_KEY.enabledKey, fileNameKey: API_EXTENSION_KEY.fileNameKey, formContentTypeKey: API_EXTENSION_KEY.formContentTypeKey })
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', value: RequestBodyParam): void;
  (e: 'update:binaryFile', value: any):void;
  (e: 'update:contentType', value: string): void;
  (e: 'rendered', value: true);
}>();

const ApiForm = defineAsyncComponent(() => import('./api-form/index.vue'));
const ApiFormForFlat = defineAsyncComponent(() => import('./apiFormForFlat/index.vue'));
const MonacoEditor = defineAsyncComponent(() => import('../MonacoEditor/index.vue'));
const ModelModal = defineAsyncComponent(() => import('./modelModal.vue'));

// eslint-disable-next-line vue/no-setup-props-destructure
const valueKey = props.fieldNames.valueKey || API_EXTENSION_KEY.valueKey;
// eslint-disable-next-line vue/no-setup-props-destructure
const enabledKey = props.fieldNames.enabledKey || API_EXTENSION_KEY.enabledKey;
// eslint-disable-next-line vue/no-setup-props-destructure
const fileNameKey = props.fieldNames.fileNameKey || API_EXTENSION_KEY.fileNameKey;

const plugins = ref(props.pluginsJSON);

const apiBaseInfo = inject('apiBaseInfo', ref());

const getRefData = async (ref, serviceId) => {
  const [error, resp] = await getModelDataByRef(serviceId, ref);
  if (error) {
    return '';
  }
  return deconstruct(resp.data || {});
};

const transRefJsonToDataJson = async (schema = {}, serviceId) => {
  // const keys = Object.keys(schema);
  if (!serviceId) {
    return schema;
  }
  for (const key in schema) {
    if (key === '$ref') {
      const refData = await getRefData(schema.$ref, serviceId);
      schema = { ...schema, ...refData };
      delete schema.$ref;
    }
    if (Object.prototype.toString.call(schema[key]) === '[object Object]') {
      schema[key] = await transRefJsonToDataJson(schema[key], serviceId);
    }
  }
  return schema;
};

const clearRawContent = ref(0);
const _contentType = ref<string>();
const binaryCt = ref(); // 文件 base64 内容
const contentMediaType = ref(); // 文件媒体类型

// raw 选中效果
const checkeRaw = computed(() => {
  return !!state.rawSelectOptions.find(i => i.value === _contentType.value);
});

const state = reactive<StateItem>({
  encodeedList: [], // encodeed form params
  formDataList: [], // formData form params
  rawContent: '',
  radioOptions: [],
  rawSelectOptions: []
});

const allContentTypes = ref<string[]>(CONTENT_TYPE);

const packageParams = () => {
  if (!_contentType.value) {
    return {
      ...props.defaultValue,
      content: null
    };
  }

  const content = {
    ...props.defaultValue.content
  };
  if (!content[_contentType.value]) {
    content[_contentType.value] = {
      schema: {}
    };
  }
  if (_contentType.value === 'application/x-www-form-urlencoded') {
    content[_contentType.value as string].schema.properties = {};
    state.encodeedList.forEach(({ name, ...item }) => {
      if (!name) {
        return;
      }
      if (!content[_contentType.value as string].schema.properties) {
        content[_contentType.value as string].schema.properties = {};
      }
      content[_contentType.value as string].schema.properties[name as string] = {
        ...item
      };
    });
    if (!urlencodedUseRef.value) {
      delete content[_contentType.value as string].schema.$ref;
    }
  } else if (_contentType.value === 'multipart/form-data') {
    content[_contentType.value as string].schema.properties = {};
    state.formDataList.forEach(({ name, ...item }) => {
      if (!name) {
        return;
      }
      if (!content[_contentType.value as string].schema.properties) {
        content[_contentType.value as string].schema.properties = {};
      }
      content[_contentType.value as string].schema.properties[name as string] = {
        ...item
      };
    });
    if (!formDataUseRef.value) {
      delete content[_contentType.value as string].schema.$ref;
    }
  } else if (_contentType.value === 'application/octet-stream') {
    const _type_ = contentMediaType.value || 'application/octet-stream';
    if (!content[_type_]) {
      content[_type_] = {
        schema: {}
      };
    }
    if (!content[_type_].schema) {
      content[_type_].schema = {};
    }
    content[_type_].schema.type = 'string';
    content[_type_].schema.format = 'binary';
    content[_type_].schema['x-xc-contentEncoding'] = 'gzip-base64';
    content[_type_].schema[valueKey] = binaryCt.value;
    content[_type_].schema[fileNameKey] = filename.value;
    if (_type_ !== 'application/octet-stream') {
      delete content['application/octet-stream'];
    }
    Object.keys(content).forEach(key => {
      if (key !== _type_ && content[key]?.schema?.format === 'binary') {
        delete content[key];
      }
    });
  } else {
    if (!content[_contentType.value].schema) {
      content[_contentType.value].schema = {};
    }
    content[_contentType.value][valueKey] = state.rawContent;
  }
  const data = {
    ...props.defaultValue,
    content,
    $ref: $ref.value
  };
  return data;
};

const handleRadioChange = (e):void => {
  const value = e.target.value;
  _contentType.value = value;
  emit('update:contentType', _contentType.value);
};

const clickRawRadio = (RadioEvent) => {
  const checked = RadioEvent.target.checked;
  if (checked) {
    _contentType.value = state.rawSelectOptions[0]?.value;
  }
  emit('update:contentType', _contentType.value);
  getPropsRawContent();
};

const handleSelectChange = ():void => {
  // emitHandle();
  emit('update:contentType', _contentType.value);
  getPropsRawContent();
};

// 编辑内容 application/x-www-form-urlencoded
const urlencodedUseRef = ref();
const changeEncodeedList = (index:number, data:ParamsItem):void => {
  state.encodeedList[index] = data;
  emitHandle();
  if (urlencodedUseRef.value) {
    return;
  }
  // 没有name为空的元素则添加新的空元素
  const newItem = getNewItem(state.encodeedList);
  if (newItem && !props.viewType) {
    state.encodeedList.push(newItem);
  }
};
// 取消 urlencodedUseRef 引用
const cancelUrlEncodedRef = () => {
  urlencodedUseRef.value = false;
  emitHandle();
  const newItem = getNewItem(state.encodeedList);
  if (newItem && !props.viewType) {
    state.encodeedList.push(newItem);
  }
};

const delEncodeedList = (index:number):void => {
  state.encodeedList.splice(index, 1);
  emitHandle();
  // 没有name为空的元素则添加新的空元素
  const newItem = getNewItem(state.encodeedList);
  if (newItem && !props.viewType) {
    state.encodeedList.push(newItem);
  }
};

const formDataUseRef = ref();
const changeFormDataList = (index:number, data:ParamsItem) => {
  state.formDataList[index] = data;
  emitHandle();
  if (formDataUseRef.value) {
    return;
  }
  // 没有name为空的元素则添加新的空元素
  const newItem = getNewItem(state.formDataList);
  if (newItem && !props.viewType) {
    state.formDataList.push(newItem);
  }
};

const delFormDataList = (index:number) => {
  state.formDataList.splice(index, 1);
  // 没有name为空的元素则添加新的空元素
  const newItem = getNewItem(state.formDataList);
  if (newItem && !props.viewType) {
    state.formDataList.push(newItem);
  }
  emitHandle();
};
// 取消 formDataUseRef 引用
const cancelformDataRef = () => {
  formDataUseRef.value = false;
  emitHandle();
  const newItem = getNewItem(state.formDataList);
  if (newItem && !props.viewType) {
    state.formDataList.push(newItem);
  }
};

const emitHandle = ():void => {
  const params = packageParams();
  emit('change', params);
};

const isRaw = computed(() => {
  return ['application/json', 'text/html', 'application/javascript', 'application/xml', '*/*', 'text/plain'].includes(_contentType.value);
});

const showEncodeedForm = computed(() => {
  return _contentType.value === 'application/x-www-form-urlencoded';
});

const showFormDataForm = computed(() => {
  return _contentType.value === 'multipart/form-data';
});

const language = computed(() => {
  switch (_contentType.value) {
    case 'application/json':
      return 'json';
    case 'text/html':
    case 'application/xml':
      return 'html';
    case 'application/javascript':
      return 'typescript';
    case 'text/plain':
      return 'text';
    default:
      return 'text';
  }
});
// 上传流文件
const filename = ref(); // 文件名称
const binaryFile = ref(); // 上传文件的 blob 形式
const binaryBase64 = ref(); // 上传文件的 base64 形式

const getModelData = async (ref) => {
  const [error, { data }] = await getModelDataByRef(apiBaseInfo.value.serviceId, ref);
  if (error) {
    return {};
  }
  return deconstruct(data || {});
};

const $ref = ref();
const getPropsRawContent = async () => {
  const { content } = props.defaultValue;
  const type = _contentType.value;
  if (!type || !content) {
    return;
  }
  if (content[type]?.schema?.$ref && !content[type]?.schema?.type && !content[type]?.schema?.properties && !content[type]?.schema?.items) {
    const [error, res] = await getModelDataByRef(apiBaseInfo.value.serviceId, content[type]?.schema.$ref);
    if (error) {
      // emit('change', { ...props.defaultValue, content: { ...props.defaultValue.content, [type]: {...content[type], schema: { ...content[type]?.schema,}}} });
      return;
    }
    const model = JSON.parse(res.data.model);
    if (!model.type) {
      if (model.properties) {
        model.type = 'object';
      } else if (model.items) {
        model.type = 'array';
      } else {
        model.type = 'string';
      }
    }
    emit('change', { ...props.defaultValue, content: { ...props.defaultValue.content, [type]: { ...content[type], schema: { ...content[type]?.schema, ...model } } } });
    getPropsRawContent();
  }
  if (type === 'application/json') {
    if (content?.[type]?.schema?.format === 'binary') {
      state.rawContent = '';
      return;
    }
    const typeBodyContent = JSON.parse(JSON.stringify(content[type] || content['*/*'] || {}));
    if (typeBodyContent.schema && !typeBodyContent?.[valueKey]) {
      typeBodyContent.schema = await transRefJsonToDataJson(typeBodyContent.schema, apiBaseInfo.value?.serviceId);
      if (!typeBodyContent[valueKey]) {
        typeBodyContent[valueKey] = typeBodyContent.schema[valueKey];
      }
    }
    if (typeBodyContent?.[valueKey] === state.rawContent) {
      return;
    }
    state.rawContent = SwaggerUI.extension.sampleFromSchemaGeneric(typeBodyContent?.schema, { useValue: true }, (typeBodyContent?.[valueKey] !== undefined ? typeBodyContent?.[valueKey] : undefined));
    if (typeof state.rawContent === 'string') {
      state.rawContent = deepParseJson(state.rawContent);
    }
    if (typeof state.rawContent === 'object') {
      state.rawContent = JSON.stringify(state.rawContent, undefined, 2);
    }
    // state.rawContent = "\"{\\n    \\\"id\\\": 64845653,\\n    \\\"address\\\": \\\"dolor enim laborum Ut\\\",\\n    \\\"email\\\": \\\"consectetur commodo\\\",\\n    \\\"enterpriseCertData\\\": {\\n        \\\"businessLicensePicUrl\\\": \\\"dolore esse\\\",\\n        \\\"bussiness\\\": \\\"labore irure consectet\\\",\\n        \\\"creditCode\\\": \\\"consequat adipisicing laborum\\\",\\n        \\\"email\\\": \\\"culpa eu ut eiusmod laboris\\\",\\n        \\\"expiryTime\\\": \\\"2001-04-16T23:24:58.877Z\\\",\\n        \\\"issueTime\\\": \\\"1993-05-13T15:00:19.431Z\\\",\\n        \\\"legalPerson\\\": \\\"sint nostrud amet\\\",\\n        \\\"orgCode\\\": \\\"mollit fugiat sit consequat\\\",\\n        \\\"orgCodePicUrl\\\": \\\"consectetur labore aliqua ex\\\",\\n        \\\"phone\\\": \\\"id occaecat\\\",\\n        \\\"regAddress\\\": \\\"pariatur consequat\\\",\\n        \\\"type\\\": -90161412,\\n        \\\"webSite\\\": \\\"vo\\\"\\n    },\\n    \\\"legalCertData\\\": {\\n        \\\"birthday\\\": \\\"anim esse labore \\\",\\n        \\\"cerBackPicUrl\\\": \\\"minim laborum irure\\\",\\n        \\\"certFrontPicUrl\\\": \\\"dolore exercitation Lorem\\\",\\n        \\\"certNo\\\": \\\"laborum cillum aliqua deserunt\\\",\\n        \\\"name\\\": \\\"Excepteur culpa\\\",\\n        \\\"nation\\\": \\\"laboris\\\",\\n        \\\"sex\\\": \\\"Lorem esse ut amet\\\"\\n    },\\n    \\\"name\\\": \\\"sunt ad anim velit dolore\\\",\\n    \\\"orgCertData\\\": {\\n        \\\"certNo\\\": \\\"consectetur tempor ut id\\\",\\n        \\\"legalPerson\\\": \\\"in la\\\",\\n        \\\"legalPersonCertUrl\\\": \\\"tempor in Excepteur\\\",\\n        \\\"orgCode\\\": \\\"aute deserunt exerc\\\",\\n        \\\"orgCodePicUrl\\\": \\\"cillum officia proident Excepteur\\\",\\n        \\\"orgName\\\": \\\"Lorem Ut est\\\",\\n        \\\"type\\\": 75072651\\n    },\\n    \\\"userCertData\\\": {\\n        \\\"birthday\\\": \\\"exercitation elit Lorem\\\",\\n        \\\"cerBackPicUrl\\\": \\\"cillum laborum ea aliqua\\\",\\n        \\\"certFrontPicUrl\\\": \\\"sit cupidatat quis\\\",\\n        \\\"certNo\\\": \\\"incididunt qui aliquip\\\",\\n        \\\"name\\\": \\\"occaecat cillum fugia\\\",\\n        \\\"nation\\\": \\\"ad labore eu dolor ut\\\",\\n        \\\"sex\\\": \\\"qui esse quis\\\"\\n    }\\n}\""
    // state.rawContent = "{\n    \"groupId\": 13639619,\n    \"orgCode\": \"qui sunt ad nostrud\"\n}"
    return;
  }
  if (type === 'application/xml') {
    if (content?.[type]?.schema?.format === 'binary') {
      state.rawContent = '';
      return;
    }
    const typeBodyContent = JSON.parse(JSON.stringify(content[type] || content['*/*'] || {}));
    if (typeBodyContent.schema) {
      typeBodyContent.schema = await transRefJsonToDataJson(typeBodyContent.schema, apiBaseInfo.value?.serviceId);
      if (!typeBodyContent[valueKey]) {
        typeBodyContent[valueKey] = typeBodyContent.schema[valueKey];
      }
    }
    if (typeBodyContent[valueKey] === state.rawContent) {
      return;
    }
    state.rawContent = typeBodyContent[valueKey];
    if (state.rawContent) {
      return;
    }
    const xmlObj = SwaggerUI.extension.sampleFromSchemaGeneric(typeBodyContent?.schema, { useValue: true }, (typeBodyContent?.[valueKey] !== undefined ? typeBodyContent?.[valueKey] : undefined), true);
    if (typeof xmlObj === 'object' && (!xmlObj || typeof xmlObj.notagname !== 'string')) {
      state.rawContent = XML(xmlObj, { declaration: true, indent: '\t' });
    }
    return;
  }
  const typeBodyContent = JSON.parse(JSON.stringify(content[type] || content['*/*'] || {}));
  if (typeBodyContent.schema) {
    typeBodyContent.schema = await transRefJsonToDataJson(typeBodyContent.schema, apiBaseInfo.value?.serviceId);
    if (!typeBodyContent[valueKey]) {
      typeBodyContent[valueKey] = typeBodyContent.schema[valueKey];
    }
  }
  if (typeBodyContent[valueKey] === state.rawContent) {
    return;
  }
  state.rawContent = typeBodyContent[valueKey];
  if (state.rawContent) {
    return;
  }
  state.rawContent = SwaggerUI.extension.sampleFromSchemaGeneric(typeBodyContent?.schema, { useValue: true }, (typeBodyContent?.[valueKey] !== undefined ? typeBodyContent?.[valueKey] : undefined));
};
watch(() => props.defaultValue, async (newValue) => {
  const { content } = newValue;
  // if (!allContentTypes.value.length) {
  //   await loadSelectOpt();
  // }
  if (newValue.$ref) {
    $ref.value = newValue.$ref;
    if (!content) {
      const [error, res] = await getModelDataByRef(apiBaseInfo.value.serviceId, newValue.$ref);
      if (error) {
        emit('change', { ...newValue, content: {} });
        return;
      }
      if (!res.data?.model) {
        emit('change', { ...newValue, content: {} });
        return;
      }
      const model = JSON.parse(res.data.model);
      emit('change', model);
      return;
    }
  }

  _contentType.value = props.contentType;

  if (!_contentType.value) {
    _contentType.value = null;
  }
  for (const type in (content || {})) {
    if (type === 'application/x-www-form-urlencoded') {
      if ((state.encodeedList.filter(i => i.name || i[valueKey] || i.type !== 'string')).length < 1) {
        const use$ref = !content['application/x-www-form-urlencoded']?.schema?.properties && content['application/x-www-form-urlencoded']?.schema?.$ref;
        if (use$ref) {
          const model = await getModelData(use$ref);
          if (model) {
            if (!model.type) {
              if (model.properties) {
                model.type = 'object';
              } else if (model.items) {
                model.type = 'array';
              } else {
                model.type = 'string';
              }
            }
            content['application/x-www-form-urlencoded'].schema = {
              $ref: use$ref,
              ...model
            };
            urlencodedUseRef.value = use$ref;
          }
        }
        state.encodeedList = Object.keys(content['application/x-www-form-urlencoded']?.schema?.properties || {}).map(key => {
          const itemSchema = content['application/x-www-form-urlencoded'].schema?.properties?.[key];
          if (!itemSchema.type && itemSchema.format) {
            itemSchema.type = getDataTypeFromFormat(itemSchema.format);
          }
          if (!itemSchema.type && itemSchema.properties) {
            itemSchema.type = 'object';
          }
          if (!itemSchema.type && itemSchema.items) {
            itemSchema.type = 'array';
          }
          if (['object', 'array', 'number', 'integer', 'boolean'].includes(itemSchema.type) && typeof itemSchema[valueKey] === 'string') {
            try {
              itemSchema[valueKey] = JSON.parse(itemSchema[valueKey]);
            } catch {}
          }
          return {
            name: key,
            [enabledKey]: true,
            ...itemSchema
          };
        });
        if (state.encodeedList.every(i => !!i.name) && !urlencodedUseRef.value && !props.viewType) {
          state.encodeedList.push(getBodyDefaultItem({ type: 'string' }));
        }
      }
    } else if (type === 'multipart/form-data') {
      if (state.formDataList.filter(i => i.name || i[valueKey] || i.type !== 'string' || i.format).length < 1) {
        const use$ref = !content['multipart/form-data']?.schema?.properties && content['multipart/form-data']?.schema?.$ref;
        if (use$ref) {
          const model = await getModelData(use$ref);
          if (!model.type) {
            if (model.properties) {
              model.type = 'object';
            } else if (model.items) {
              model.type = 'array';
            } else {
              model.type = 'string';
            }
          }
          content['multipart/form-data'].schema = {
            $ref: use$ref,
            ...model
          };
          formDataUseRef.value = use$ref;
        }
        state.formDataList = Object.keys(content['multipart/form-data']?.schema?.properties || {}).map(key => {
          const itemSchema = content['multipart/form-data'].schema?.properties?.[key];
          if (!itemSchema.type && itemSchema.format) {
            itemSchema.type = getDataTypeFromFormat(itemSchema.format);
          }
          if (!itemSchema.type && itemSchema.properties) {
            itemSchema.type = 'object';
          }
          if (!itemSchema.type && itemSchema.items) {
            itemSchema.type = 'array';
          }
          if ((['object', 'array', 'number', 'integer', 'boolean'].includes(itemSchema.type) || itemSchema.format === 'binary') && typeof itemSchema[valueKey] === 'string') {
            try {
              itemSchema[valueKey] = JSON.parse(itemSchema[valueKey]);
            } catch {}
          }
          return {
            name: key,
            [enabledKey]: true,
            ...itemSchema
          };
        });
        if (state.formDataList.every(i => !!i.name) && !formDataUseRef.value && !props.viewType) {
          state.formDataList.push(getBodyDefaultItem({ type: 'string' }));
        }
      }
    } else if ((type && !allContentTypes.value.includes(type)) || type === 'application/octet-stream') {
      binaryCt.value = content[type].schema?.[valueKey] || '';
      filename.value = content[type].schema?.[fileNameKey] || '';
      contentMediaType.value = type || '';
    }
  }
  if (state.formDataList.every(i => !!i.name) && !formDataUseRef.value && !props.viewType) {
    state.formDataList.push(getBodyDefaultItem({ type: 'string' }));
  }
  if (state.encodeedList.every(i => !!i.name) && !urlencodedUseRef.value && !props.viewType) {
    state.encodeedList.push(getBodyDefaultItem({ type: 'string' }));
  }
  if (_contentType.value === 'application/json') {
    if (content?.[_contentType.value]?.schema?.format === 'binary') {
      binaryCt.value = content[_contentType.value].schema?.[valueKey] || '';
      filename.value = content[_contentType.value].schema?.[fileNameKey] || '';
      contentMediaType.value = 'application/json';
      _contentType.value = 'application/octet-stream';
    } else {
      await getPropsRawContent();
    }
    return;
  }
  if (_contentType.value === 'text/html') {
    if (content?.[_contentType.value]?.schema?.format === 'binary') {
      binaryCt.value = content[_contentType.value].schema?.[valueKey] || '';
      filename.value = content[_contentType.value].schema?.[fileNameKey] || '';
      contentMediaType.value = 'text/html';
      _contentType.value = 'application/octet-stream';
    } else {
      await getPropsRawContent();
    }
    return;
  }
  if (_contentType.value === 'application/javascript') {
    if (content[_contentType.value]?.schema?.format === 'binary') {
      binaryCt.value = content[_contentType.value].schema?.[valueKey] || '';
      filename.value = content[_contentType.value].schema?.[fileNameKey] || '';
      contentMediaType.value = 'application/javascript';
      _contentType.value = 'application/octet-stream';
    } else {
      await getPropsRawContent();
    }
    return;
  }
  if (_contentType.value === 'application/xml') {
    if (content[_contentType.value]?.schema?.format === 'binary') {
      binaryCt.value = content[_contentType.value].schema?.[valueKey] || '';
      filename.value = content[_contentType.value].schema?.[fileNameKey] || '';
      contentMediaType.value = 'application/xml';
      _contentType.value = 'application/octet-stream';
    } else {
      await getPropsRawContent();
    }
    return;
  }
  if (_contentType.value === 'text/plain') {
    if (content[_contentType.value]?.schema?.format === 'binary') {
      binaryCt.value = content[_contentType.value].schema?.[valueKey] || '';
      filename.value = content[_contentType.value].schema?.[fileNameKey] || '';
      contentMediaType.value = 'text/plain';
      _contentType.value = 'application/octet-stream';
    } else {
      await getPropsRawContent();
    }
    return;
  }

  if ((_contentType.value && !allContentTypes.value.includes(_contentType.value)) || _contentType.value === 'application/octet-stream') {
    binaryCt.value = content[_contentType.value]?.schema?.[valueKey] || '';
    filename.value = content[_contentType.value]?.schema?.[fileNameKey] || '';
    contentMediaType.value = _contentType.value || '';
    _contentType.value = 'application/octet-stream';
  }
}, {
  immediate: true,
  deep: true
});

// 格式化文本
const formatRawContent = () => {
  if (_contentType.value === 'application/json') {
    try {
      const json = JSON.parse(state.rawContent);
      state.rawContent = JSON.stringify(json, undefined, 2);
    } catch {}
  }
  if (_contentType.value === 'application/xml') {
    try {
      state.rawContent = state.rawContent.replace(/(>)(<)(\/*)/g, '$1\n$2$3');
    } catch {}
  }
  if (_contentType.value === 'text/html') {
    state.rawContent = pretty(state.rawContent, { indent_size: 2 });
  }

  if (_contentType.value === 'application/javascript') {
    state.rawContent = jsBeautify(state.rawContent, { indent_size: 2, space_in_empty_paren: true });
  }
};

// 压缩文本
const compressRawContent = () => {
  if (_contentType.value === 'application/json') {
    try {
      const json = JSON.parse(state.rawContent);
      state.rawContent = JSON.stringify(json, undefined, 0);
    } catch {}
    return;
  }
  if (_contentType.value === 'application/xml') {
    try {
      state.rawContent = state.rawContent.replace(/(>)(\n)(<)(\/*)/g, '$1$3$4');
    } catch {}
    return;
  }
  if (_contentType.value === 'text/html') {
    state.rawContent = pretty(state.rawContent, { indent_size: 0 });
    return;
  }
  if (_contentType.value === 'application/javascript') {
    state.rawContent = jsBeautify(state.rawContent, { indent_size: 0, space_in_empty_paren: true });
    return;
  }
  state.rawContent = state.rawContent.replace(/\n|\t|\r|\n\r/g, '');
};

const formFileSize = ref(0);

const gziping = ref(false);
const fileSize = ref(0);
const handleFile = async ({ file }) => {
  if (file.size > (plugins.value.maxFileSize - formFileSize.value)) {
    notification.error(t('xcan_apiBody.requestBodyFileSizeExceeded'));
    return;
  }
  fileSize.value = file.size;
  contentMediaType.value = file.type;
  emit('update:contentType', contentMediaType.value || 'application/octet-stream');

  gziping.value = true;
  setTimeout(async () => {
    const fileBase = await gzip(file);
    gziping.value = false;
    binaryCt.value = fileBase;
    binaryFile.value = file;
    filename.value = file.name;
    emitHandle();
  }, 100);
};

const handleRemove = () => {
  filename.value = undefined;
  binaryBase64.value = undefined;
  binaryFile.value = undefined;
  contentMediaType.value = undefined;
  binaryCt.value = undefined;
  fileSize.value = 0;
  emit('update:contentType', 'application/octet-stream');
  emitHandle();
};
// const formMaxFileSize

watch(() => state.rawContent, debounce(duration.search, () => {
  emitHandle();
}));

// 暴露给父级 返回binaryFile
const getBinaryFile = async () => {
  if (binaryFile.value) {
    return binaryFile.value;
  } else if (binaryCt.value) {
    const buf = await ungzip(binaryCt.value);
    const file = await new File([buf], filename.value);
    fileSize.value = file.size;
    binaryFile.value = file;
    return file;
  }
};
// 暴露给父级 更新binaryBase64
const getBinaryBase64 = () => {
  return binaryBase64.value;
};

const visible = ref(false);
const useCompRef = ref(false);
const toggleOpenModel = (useRef = false) => {
  useCompRef.value = useRef;
  visible.value = !visible.value;
};

const importModel = (data) => {
  if (useCompRef.value) {
    $ref.value = data.ref;
  } else {
    $ref.value = undefined;
  }
  emit('change', { ...data.schema, $ref: $ref.value || undefined });
};

watch(() => binaryCt.value, newValue => {
  if (!fileSize.value && newValue) {
    getBinaryFile();
  }
}, {
  immediate: true
});

watch(() => allContentTypes.value, (newValue) => {
  if (!newValue) {
    return;
  }
  const radioOptions:OptionItem[] = [{ value: null, label: 'none' }];
  radioOptions.push(...newValue?.filter(item => radioGroups.includes(item)).map(item => ({ label: item, value: item })));
  state.radioOptions = radioOptions;
  state.rawSelectOptions = newValue?.filter(item => !radioGroups.includes(item)).map(item => ({ label: item, value: item }));
}, {
  immediate: true
});

// 组装用于前端发送请求的body数据结构
const getBodyData = async (dataSource, contentType) => {
  const { content } = dataSource;
  if (!content) {
    return undefined;
  }
  if (!contentType) {
    return undefined;
  }
  // const contents = content[contentType];
  if (contentType === 'application/x-www-form-urlencoded') {
    // state.encodeedList = contents;
    // let data:any[] = [];
    // data = SwaggerUI.extension.sampleFromSchemaGeneric(content[contentType]?.schema || {}, { useValue: true });
    // data = Object.keys(data).map(key => {
    //   return {
    //     name: key,
    //     ...data[key]
    //     // ...content['application/x-www-form-urlencoded']?.schema?.properties?.[key].extensions
    //   };
    // });
    return state.encodeedList.filter(i => i[enabledKey] && !!i.name);
  } else if (contentType === 'multipart/form-data') {
    // const data: any[] = [];
    // const contentValue = SwaggerUI.extension.sampleFromSchemaGeneric(content[contentType]?.schema || {}, { useValue: true });
    // Object.keys(contentValue).forEach((key) => {
    //   data.push({
    //     name: key,
    //     ...contentValue?.[key]
    //   });
    // });
    return state.formDataList.filter(i => i[enabledKey] && !!i.name);
  } else if (_contentType.value === 'application/octet-stream') {
    return await getBinaryFile();
  } else {
    // let data = '';
    // if (contentType === 'application/json') {
    //   data = SwaggerUI.extension.sampleFromSchemaGeneric(content[contentType]?.schema, { useValue: true }, content[contentType]?.[valueKey] || undefined);
    //   if (typeof data === 'object') {
    //     data = JSON.stringify(data, undefined, 2);
    //   }
    // }
    // } else if (contentType === 'application/xml') {
    //   data = SwaggerUI.extension.sampleFromSchemaGeneric(content[contentType]?.schema, { useValue: true }, content[contentType]?.[valueKey] || undefined);
    // } else {
    //   data = SwaggerUI.extension.sampleFromSchemaGeneric(content[contentType]?.schema, { useValue: true }, content[contentType]?.[valueKey] || undefined);
    // }
    return state.rawContent;
  }
};

// 用于保存的body数据
const getData = () => {
  const param = packageParams();
  return param;
};

const updateComp = async () => {
  if (state.encodeedList && urlencodeFormRef.value) {
    await urlencodeFormRef.value.updateComp();
  }

  if (state.formDataList && formDataFormRef.value) {
    await formDataFormRef.value.updateComp();
  }
  const body = packageParams();
  if ($ref.value) {
    const paths = $ref.value.split('/');
    const name = paths[paths.length - 1];
    await http.put(`${TESTER}/services/${apiBaseInfo.value.serviceId}/comp/requestBodies/${name}`, body);
  }

  // for (const type in (body.content || {})) {
  //   if (body.content[type].$ref) {
  //     const paths = body.content[type].$re.split('/');
  //     const name = paths[paths.length - 1];
  //     await project.addComponent(apiBaseInfo.value.projectId, 'schemas', name, body.content[type]);
  //   }
  // }
};

// 重置body 数据
const resetBodyData = () => {
  state.formDataList = [];
  state.encodeedList = [];
  state.rawContent = '';
  filename.value = undefined;
  binaryBase64.value = undefined;
  binaryFile.value = undefined;
  contentMediaType.value = undefined;
  binaryCt.value = undefined;
  fileSize.value = 0;
};

const formDataFormRef = ref();
const urlencodeFormRef = ref();
const validate = (val = true) => {
  if (_contentType.value === 'application/x-www-form-urlencoded') {
    urlencodeFormRef.value.validate(val);
  }
  if (_contentType.value === 'multipart/form-data') {
    formDataFormRef.value.validate(val);
  }
};

const getModelResolve = () => {
  const models = {};
  if ($ref.value) {
    models[$ref.value] = props.defaultValue;
  }
  if (urlencodedUseRef.value) {
    models[urlencodedUseRef.value] = props.defaultValue.content['application/x-www-form-urlencoded'];
    delete models[urlencodedUseRef.value]?.schema?.$ref;
  }
  if (formDataUseRef.value) {
    models[formDataUseRef.value] = props.defaultValue.content['multipart/form-data'];
    delete models[formDataUseRef.value]?.schema?.$ref;
  }
  if (showEncodeedForm.value) {
    urlencodeFormRef.value.getModelResolve(models);
  } else if (formDataFormRef.value) {
    formDataFormRef.value.getModelResolve(models);
  } else if (_contentType.value && props.defaultValue.content?.[_contentType.value]) {
    const contents = props.defaultValue.content?.[_contentType.value];
    if (contents.schema.$ref) {
      models[contents.schema.$ref] = contents.schema;
    }
  }
  return models;
};

onMounted(() => {
  nextTick(() => {
    emit('rendered', true);
  });
});

defineExpose({ getBinaryFile, getBinaryBase64, getBodyData, getData, updateComp, validate, getModelResolve, resetBodyData });
</script>
<template>
  <div>
    <div class="flex items-center flex-wrap whitespace-nowrap">
      <label class="text-3 leading-3 text-gray-content mr-4 select-none">
        <span class="mr-0.25">Content-Type</span>:
      </label>
      <RadioGroup
        v-model:value="_contentType"
        class="select-none mr-2"
        name="radioGroup"
        :disabled="props.viewType"
        @change="handleRadioChange">
        <Radio
          v-for="item in state.radioOptions"
          :key="item.value"
          :value="item.value">
          {{ item.label === 'application/octet-stream' ? 'binary' : item.label }}
        </Radio>
      </RadioGroup>
      <Radio
        :checked="checkeRaw"
        :disabled="props.viewType"
        @change="clickRawRadio">
        raw
      </Radio>
      <Select
        v-show="isRaw"
        v-model:value="_contentType"
        class="w-40"
        dropdownClassName="response-body-select"
        :disabled="props.viewType"
        :allowClear="false"
        :options="state.rawSelectOptions"
        @change="handleSelectChange" />
      <template v-if="!!_contentType && apiBaseInfo?.serviceId && !props.viewType">
        <Button
          v-if="!props.hideImportBtn"
          type="link"
          size="small"
          @click="toggleOpenModel(true)">
          {{ t('xcan_apiBody.importComponent') }}
        </Button>
        <Button
          type="link"
          size="small"
          @click="toggleOpenModel(false)">
          {{ t('xcan_apiBody.copyComponent') }}
        </Button>
      </template>
      <template v-if="isRaw && state.rawContent && !props.viewType">
        <Button
          type="link"
          size="small"
          @click="formatRawContent">
          {{ t('xcan_apiBody.format') }}
        </Button>
        <Button
          type="link"
          size="small"
          @click="compressRawContent">
          {{ t('xcan_apiBody.compress') }}
        </Button>
      </template>
    </div>
    <Tooltip v-if="!props.useFlatForm" placement="topLeft">
      <ApiForm
        v-show="showEncodeedForm"
        key="encodeedList"
        ref="urlencodeFormRef"
        class="mt-5"
        :viewType="props.viewType"
        :formFileSize="formFileSize"
        :maxFileSize="0"
        :useModel="!!urlencodedUseRef"
        :value="state.encodeedList"
        :hideImportBtn="props.hideImportBtn"
        :fieldNames="props.fieldNames"
        @change="changeEncodeedList"
        @del="delEncodeedList" />
      <template v-if="urlencodedUseRef" #title>
        {{ t('xcan_apiBody.componentReference', { ref: urlencodedUseRef }) }}
                  <Button
            :disabled="props.viewType"
            size="small"
            type="link"
            @click="cancelUrlEncodedRef">
            {{ t('xcan_apiBody.cancelReference') }}
          </Button>
      </template>
    </Tooltip>
    <ApiFormForFlat
      v-else
      v-show="showEncodeedForm"
      key="encodeedList"
      ref="urlencodeFormRef"
      class="mt-5"
      :viewType="props.viewType"
      :formFileSize="formFileSize"
      :maxFileSize="0"
      :useModel="!!urlencodedUseRef"
      :value="state.encodeedList"
      :hideImportBtn="props.hideImportBtn"
      :fieldNames="props.fieldNames"
      @change="changeEncodeedList"
      @del="delEncodeedList" />
    <Tooltip v-if="!props.useFlatForm" placement="topLeft">
      <ApiForm
        v-show="showFormDataForm"
        ref="formDataFormRef"
        key="formDataList"
        v-model:formFileSize="formFileSize"
        :viewType="props.viewType"
        :maxFileSize="plugins.maxFileSize - fileSize"
        class="mt-5"
        :value="state.formDataList"
        :useModel="!!formDataUseRef"
        :hideImportBtn="props.hideImportBtn"
        :fieldNames="props.fieldNames"
        hasFileType
        @change="changeFormDataList"
        @del="delFormDataList" />
      <template v-if="formDataUseRef" #title>
        {{ t('xcan_apiBody.componentReference', { ref: formDataUseRef }) }}
                  <Button
            :disabled="props.viewType"
            size="small"
            type="link"
            @click="cancelformDataRef">
            {{ t('xcan_apiBody.cancelReference') }}
          </Button>
      </template>
    </Tooltip>
    <ApiFormForFlat
      v-else
      v-show="showFormDataForm"
      ref="formDataFormRef"
      key="formDataList"
      v-model:formFileSize="formFileSize"
      :viewType="props.viewType"
      :maxFileSize="plugins.maxFileSize - fileSize"
      class="mt-5"
      :value="state.formDataList"
      :useModel="!!formDataUseRef"
      :hideImportBtn="props.hideImportBtn"
      :fieldNames="props.fieldNames"
      hasFileType
      @change="changeFormDataList"
      @del="delFormDataList" />
    <AsyncComponent :visible="isRaw">
      <div
        v-show="isRaw"
        style="height: 220px;"
        class="w-full mt-2 py-2 border border-solid border-theme-divider rounded bg-white">
        <MonacoEditor
          v-model:value="state.rawContent"
          theme="vs"
          :readOnly="props.viewType"
          :isFormat="false"
          :notifyClear="clearRawContent"
          :language="language" />
      </div>
    </AsyncComponent>
    <div v-show="_contentType === 'application/octet-stream'" class="mt-2 flex items-center">
      <Upload
        :disabled="gziping || !!binaryCt || props.viewType"
        :showUploadList="false"
        :customRequest="handleFile"
        :multiple="false"
        :fieldName="props.fieldNames">
        <Button
          :loading="gziping"
          :disabled="filename || !!binaryCt || props.viewType"
          size="small">
          <Icon icon="icon-tuisongtongzhi" class="mr-1" />
          <span>{{ t('xcan_apiBody.uploadFile') }}</span>
        </Button>
      </Upload>
      <div v-show="filename || binaryCt" class="flex items-center h-5 leading-5 px-1.5 ml-1 select-none rounded text-3 border-none text-theme-content">
        <span>{{ filename || binaryCt }}</span>
        <Icon
          icon="icon-shanchuguanbi"
          class="ml-1 text-gray-line cursor-pointer text-theme-text-hover"
          @click="handleRemove" />
      </div>
    </div>
    <ModelModal v-model:visible="visible" @confirm="importModel" />
  </div>
</template>
<style>
.api-select-dropdown .ant-select-item {
  @apply text-3 leading-7 py-0 min-h-full;
}

.response-body-select .ant-select-item-option-content {
  @apply text-3;
}
</style>
<style scoped>
.ant-radio-wrapper {
  @apply text-3;
}

.ant-radio-wrapper :deep(.ant-radio-wrapper) {
  @apply leading-4;
}

.ant-select:not(.ant-select-customize-input) :deep(.ant-select-selector) {
  @apply h-7 text-3 leading-3 rounded;

  border-color: var(--border-text-box);
}

.ant-select:not(.ant-select-customize-input) :deep(.ant-select-selector) .ant-select-selection-item {
  @apply leading-6.5;
}
</style>
