<script setup lang="ts">
// Vue core imports
import { computed, defineAsyncComponent, reactive, ref, watch, inject, onMounted, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Icon, notification, Select, AsyncComponent } from '@xcan-angus/vue-ui';
import { Button, RadioGroup, Radio, Upload, Tooltip } from 'ant-design-vue';

// Infrastructure imports
import { http, duration, TESTER, codeUtils } from '@xcan-angus/infra';
import SwaggerUI from '@xcan-angus/swagger-ui';

// Third-party library imports
import XML from 'xml';
import pretty from 'pretty';
import jsBeautify from 'js-beautify';
import { debounce } from 'throttle-debounce';

// Utility imports
import { deconstruct, API_EXTENSION_KEY, getDataTypeFromFormat, CONTENT_TYPE, getNewItem, getBodyDefaultItem, ParamsItem, getModelDataByRef } from '@/utils/apis';
import { radioGroups, RequestBodyParam, StateItem, OptionItem, deepParseJson } from './interface';

const { t } = useI18n();

// Code utilities
const { gzip, ungzip } = codeUtils;

/**
 * Component props interface for API body configuration
 */
export interface Props {
  defaultValue: RequestBodyParam;
  binaryFile?: any;
  contentType: string | null;
  pluginsJSON: Record<string, any>;
  hideImportBtn: boolean;
  viewType: boolean;
  fieldNames?: { valueKey: string; enabledKey: string; fileNameKey: string };
  useFlatForm: boolean;
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  defaultValue: undefined,
  binaryFile: undefined,
  contentType: null,
  pluginsJSON: () => ({}),
  hideImportBtn: false,
  viewType: false,
  useFlatForm: false,
  fieldNames: () => ({
    valueKey: API_EXTENSION_KEY.valueKey,
    enabledKey: API_EXTENSION_KEY.enabledKey,
    fileNameKey: API_EXTENSION_KEY.fileNameKey,
    formContentTypeKey: API_EXTENSION_KEY.formContentTypeKey
  })
});

// Component events
const emit = defineEmits<{
  (e: 'change', value: RequestBodyParam): void;
  (e: 'update:binaryFile', value: any): void;
  (e: 'update:contentType', value: string): void;
  (e: 'rendered', value: true);
}>();

// Async component definitions
const ApiForm = defineAsyncComponent(() => import('./ApiForm.vue'));
const ApiFormForFlat = defineAsyncComponent(() => import('./ApiFormForFlat.vue'));
const MonacoEditor = defineAsyncComponent(() => import('@/components/editor/MonacoEditor/index.vue'));
const ModelModal = defineAsyncComponent(() => import('./modelModal.vue'));

// Field name configuration
// eslint-disable-next-line vue/no-setup-props-destructure
const valueKey = props.fieldNames.valueKey || API_EXTENSION_KEY.valueKey;
// eslint-disable-next-line vue/no-setup-props-destructure
const enabledKey = props.fieldNames.enabledKey || API_EXTENSION_KEY.enabledKey;
// eslint-disable-next-line vue/no-setup-props-destructure
const fileNameKey = props.fieldNames.fileNameKey || API_EXTENSION_KEY.fileNameKey;

// Plugin configuration
const plugins = ref(props.pluginsJSON);

// Injected API base information
const apiBaseInfo = inject('apiBaseInfo', ref());

/**
 * Get reference data by service ID and reference path
 * @param ref - Reference path
 * @param serviceId - Service identifier
 * @returns Deconstructed reference data or empty string on error
 */
const getReferenceData = async (ref: string, serviceId: string) => {
  const [error, resp] = await getModelDataByRef(serviceId, ref);
  if (error) {
    return '';
  }
  return deconstruct(resp.data || {});
};

/**
 * Transform reference JSON to data JSON by resolving all references
 * @param schema - Schema object to transform
 * @param serviceId - Service identifier
 * @returns Transformed schema with resolved references
 */
const transformReferenceJsonToDataJson = async (schema: any = {}, serviceId: string) => {
  if (!serviceId) {
    return schema;
  }
  for (const key in schema) {
    if (key === '$ref') {
      const referenceData = await getReferenceData(schema.$ref, serviceId);
      schema = { ...schema, ...referenceData };
      delete schema.$ref;
    }
    if (Object.prototype.toString.call(schema[key]) === '[object Object]') {
      schema[key] = await transformReferenceJsonToDataJson(schema[key], serviceId);
    }
  }
  return schema;
};

// Content management state
const clearRawContentTrigger = ref(0);
const currentContentType = ref<string>();
const binaryContentBase64 = ref(); // Binary file base64 content
const fileContentMediaType = ref(); // File media type

/**
 * Computed property to check if raw content type is selected
 */
const isRawContentTypeSelected = computed(() => {
  return !!state.rawSelectOptions.find(option => option.value === currentContentType.value);
});

// Main component state
const state = reactive<StateItem>({
  encodeedList: [], // URL encoded form parameters
  formDataList: [], // Form data parameters
  rawContent: '',
  radioOptions: [],
  rawSelectOptions: []
});

// Available content types
const allContentTypes = ref<string[]>(CONTENT_TYPE);

/**
 * Package request body parameters based on content type
 * @returns Packaged request body parameters
 */
const packageRequestBodyParameters = () => {
  if (!currentContentType.value) {
    return {
      ...props.defaultValue,
      content: null
    };
  }

  const content = {
    ...props.defaultValue.content
  };
  if (!content[currentContentType.value]) {
    content[currentContentType.value] = {
      schema: {}
    };
  }
  if (currentContentType.value === 'application/x-www-form-urlencoded') {
    content[currentContentType.value as string].schema.properties = {};
    state.encodeedList.forEach(({ name, ...item }) => {
      if (!name) {
        return;
      }
      if (!content[currentContentType.value as string].schema.properties) {
        content[currentContentType.value as string].schema.properties = {};
      }
      content[currentContentType.value as string].schema.properties[name as string] = {
        ...item
      };
    });
    if (!urlencodedUseRef.value) {
      delete content[currentContentType.value as string].schema.$ref;
    }
  } else if (currentContentType.value === 'multipart/form-data') {
    content[currentContentType.value as string].schema.properties = {};
    state.formDataList.forEach(({ name, ...item }) => {
      if (!name) {
        return;
      }
      if (!content[currentContentType.value as string].schema.properties) {
        content[currentContentType.value as string].schema.properties = {};
      }
      content[currentContentType.value as string].schema.properties[name as string] = {
        ...item
      };
    });
    if (!formDataUseRef.value) {
      delete content[currentContentType.value as string].schema.$ref;
    }
  } else if (currentContentType.value === 'application/octet-stream') {
    const binaryContentType = fileContentMediaType.value || 'application/octet-stream';
    if (!content[binaryContentType]) {
      content[binaryContentType] = {
        schema: {}
      };
    }
    if (!content[binaryContentType].schema) {
      content[binaryContentType].schema = {};
    }
    content[binaryContentType].schema.type = 'string';
    content[binaryContentType].schema.format = 'binary';
    content[binaryContentType].schema['x-xc-contentEncoding'] = 'gzip-base64';
    content[binaryContentType].schema[valueKey] = binaryContentBase64.value;
    content[binaryContentType].schema[fileNameKey] = uploadedFileName.value;
    if (binaryContentType !== 'application/octet-stream') {
      delete content['application/octet-stream'];
    }
    Object.keys(content).forEach(key => {
      if (key !== binaryContentType && content[key]?.schema?.format === 'binary') {
        delete content[key];
      }
    });
  } else {
    if (!content[currentContentType.value].schema) {
      content[currentContentType.value].schema = {};
    }
    content[currentContentType.value][valueKey] = state.rawContent;
  }
  const data = {
    ...props.defaultValue,
    content,
    $ref: componentReference.value
  };
  return data;
};

/**
 * Handle radio button change for content type selection
 * @param e - Radio change event
 */
const handleContentTypeRadioChange = (e: any): void => {
  const value = e.target.value;
  currentContentType.value = value;
  emit('update:contentType', currentContentType.value);
};

/**
 * Handle raw content type radio button click
 * @param RadioEvent - Radio button event
 */
const handleRawContentTypeRadioClick = (RadioEvent: any) => {
  const checked = RadioEvent.target.checked;
  if (checked) {
    currentContentType.value = state.rawSelectOptions[0]?.value;
  }
  emit('update:contentType', currentContentType.value);
  loadPropsRawContent();
};

/**
 * Handle content type select dropdown change
 */
const handleContentTypeSelectChange = (): void => {
  emit('update:contentType', currentContentType.value);
  loadPropsRawContent();
};

// URL encoded form data management
const urlencodedUseRef = ref();
/**
 * Handle URL encoded form data list change
 * @param index - Index of the changed item
 * @param data - New parameter data
 */
const handleUrlEncodedListChange = (index: number, data: ParamsItem): void => {
  state.encodeedList[index] = data;
  emitParameterChange();
  if (urlencodedUseRef.value) {
    return;
  }
  // Add new empty item if no empty name elements exist
  const newItem = getNewItem(state.encodeedList);
  if (newItem && !props.viewType) {
    state.encodeedList.push(newItem);
  }
};

/**
 * Cancel URL encoded reference usage
 */
const cancelUrlEncodedReference = () => {
  urlencodedUseRef.value = false;
  emitParameterChange();
  const newItem = getNewItem(state.encodeedList);
  if (newItem && !props.viewType) {
    state.encodeedList.push(newItem);
  }
};

/**
 * Delete URL encoded form data item
 * @param index - Index of item to delete
 */
const deleteUrlEncodedListItem = (index: number): void => {
  state.encodeedList.splice(index, 1);
  emitParameterChange();
  // Add new empty item if no empty name elements exist
  const newItem = getNewItem(state.encodeedList);
  if (newItem && !props.viewType) {
    state.encodeedList.push(newItem);
  }
};

// Form data management
const formDataUseRef = ref();
/**
 * Handle form data list change
 * @param index - Index of the changed item
 * @param data - New parameter data
 */
const handleFormDataListChange = (index: number, data: ParamsItem) => {
  state.formDataList[index] = data;
  emitParameterChange();
  if (formDataUseRef.value) {
    return;
  }
  // Add new empty item if no empty name elements exist
  const newItem = getNewItem(state.formDataList);
  if (newItem && !props.viewType) {
    state.formDataList.push(newItem);
  }
};

/**
 * Delete form data item
 * @param index - Index of item to delete
 */
const deleteFormDataListItem = (index: number) => {
  state.formDataList.splice(index, 1);
  // Add new empty item if no empty name elements exist
  const newItem = getNewItem(state.formDataList);
  if (newItem && !props.viewType) {
    state.formDataList.push(newItem);
  }
  emitParameterChange();
};

/**
 * Cancel form data reference usage
 */
const cancelFormDataReference = () => {
  formDataUseRef.value = false;
  emitParameterChange();
  const newItem = getNewItem(state.formDataList);
  if (newItem && !props.viewType) {
    state.formDataList.push(newItem);
  }
};

/**
 * Emit parameter change event
 */
const emitParameterChange = (): void => {
  const params = packageRequestBodyParameters();
  emit('change', params);
};

/**
 * Computed property to check if content type is raw
 */
const isRawContentType = computed(() => {
  return ['application/json', 'text/html', 'application/javascript', 'application/xml', '*/*', 'text/plain'].includes(currentContentType.value);
});

/**
 * Computed property to show URL encoded form
 */
const shouldShowUrlEncodedForm = computed(() => {
  return currentContentType.value === 'application/x-www-form-urlencoded';
});

/**
 * Computed property to show form data form
 */
const shouldShowFormDataForm = computed(() => {
  return currentContentType.value === 'multipart/form-data';
});

/**
 * Computed property to get Monaco editor language based on content type
 */
const monacoEditorLanguage = computed(() => {
  switch (currentContentType.value) {
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
// File upload management
const uploadedFileName = ref(); // Uploaded file name
const uploadedBinaryFile = ref(); // Uploaded file blob object
const uploadedBinaryBase64 = ref(); // Uploaded file base64 content

/**
 * Get model data by reference
 * @param ref - Reference path
 * @returns Model data or empty object on error
 */
const getModelDataByReference = async (ref: string) => {
  const [error, { data }] = await getModelDataByRef(apiBaseInfo.value.serviceId, ref);
  if (error) {
    return {};
  }
  return deconstruct(data || {});
};

// Component reference
const componentReference = ref();
/**
 * Load raw content from props based on content type
 */
const loadPropsRawContent = async () => {
  const { content } = props.defaultValue;
  const contentType = currentContentType.value;
  if (!contentType || !content) {
    return;
  }
  if (content[contentType]?.schema?.$ref && !content[contentType]?.schema?.type && !content[contentType]?.schema?.properties && !content[contentType]?.schema?.items) {
    const [error, res] = await getModelDataByRef(apiBaseInfo.value.serviceId, content[contentType]?.schema.$ref);
    if (error) {
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
    emit('change', { ...props.defaultValue, content: { ...props.defaultValue.content, [contentType]: { ...content[contentType], schema: { ...content[contentType]?.schema, ...model } } } });
    loadPropsRawContent();
  }
  if (contentType === 'application/json') {
    if (content?.[contentType]?.schema?.format === 'binary') {
      state.rawContent = '';
      return;
    }
    const typeBodyContent = JSON.parse(JSON.stringify(content[contentType] || content['*/*'] || {}));
    if (typeBodyContent.schema && !typeBodyContent?.[valueKey]) {
      typeBodyContent.schema = await transformReferenceJsonToDataJson(typeBodyContent.schema, apiBaseInfo.value?.serviceId);
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
  if (contentType === 'application/xml') {
    if (content?.[contentType]?.schema?.format === 'binary') {
      state.rawContent = '';
      return;
    }
    const typeBodyContent = JSON.parse(JSON.stringify(content[contentType] || content['*/*'] || {}));
    if (typeBodyContent.schema) {
      typeBodyContent.schema = await transformReferenceJsonToDataJson(typeBodyContent.schema, apiBaseInfo.value?.serviceId);
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
    const xmlObject = SwaggerUI.extension.sampleFromSchemaGeneric(typeBodyContent?.schema, { useValue: true }, (typeBodyContent?.[valueKey] !== undefined ? typeBodyContent?.[valueKey] : undefined), true);
    if (typeof xmlObject === 'object' && (!xmlObject || typeof xmlObject.notagname !== 'string')) {
      state.rawContent = XML(xmlObject, { declaration: true, indent: '\t' });
    }
    return;
  }
  const typeBodyContent = JSON.parse(JSON.stringify(content[contentType] || content['*/*'] || {}));
  if (typeBodyContent.schema) {
    typeBodyContent.schema = await transformReferenceJsonToDataJson(typeBodyContent.schema, apiBaseInfo.value?.serviceId);
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
// Watch for default value changes
watch(() => props.defaultValue, async (newValue) => {
  const { content } = newValue;
  if (newValue.$ref) {
    componentReference.value = newValue.$ref;
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

  currentContentType.value = props.contentType;

  if (!currentContentType.value) {
    currentContentType.value = null;
  }
  for (const contentType in (content || {})) {
    if (contentType === 'application/x-www-form-urlencoded') {
      if ((state.encodeedList.filter(item => item.name || item[valueKey] || item.type !== 'string')).length < 1) {
        const useReference = !content['application/x-www-form-urlencoded']?.schema?.properties && content['application/x-www-form-urlencoded']?.schema?.$ref;
        if (useReference) {
          const model = await getModelDataByReference(useReference);
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
              $ref: useReference,
              ...model
            };
            urlencodedUseRef.value = useReference;
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
      binaryContentBase64.value = content[type].schema?.[valueKey] || '';
      uploadedFileName.value = content[type].schema?.[fileNameKey] || '';
      fileContentMediaType.value = type || '';
    }
  }
  if (state.formDataList.every(i => !!i.name) && !formDataUseRef.value && !props.viewType) {
    state.formDataList.push(getBodyDefaultItem({ type: 'string' }));
  }
  if (state.encodeedList.every(i => !!i.name) && !urlencodedUseRef.value && !props.viewType) {
    state.encodeedList.push(getBodyDefaultItem({ type: 'string' }));
  }
  if (currentContentType.value === 'application/json') {
    if (content?.[currentContentType.value]?.schema?.format === 'binary') {
      binaryContentBase64.value = content[currentContentType.value].schema?.[valueKey] || '';
      uploadedFileName.value = content[currentContentType.value].schema?.[fileNameKey] || '';
      fileContentMediaType.value = 'application/json';
      currentContentType.value = 'application/octet-stream';
    } else {
      await loadPropsRawContent();
    }
    return;
  }
  if (currentContentType.value === 'text/html') {
    if (content?.[currentContentType.value]?.schema?.format === 'binary') {
      binaryContentBase64.value = content[currentContentType.value].schema?.[valueKey] || '';
      uploadedFileName.value = content[currentContentType.value].schema?.[fileNameKey] || '';
      fileContentMediaType.value = 'text/html';
      currentContentType.value = 'application/octet-stream';
    } else {
      await loadPropsRawContent();
    }
    return;
  }
  if (currentContentType.value === 'application/javascript') {
    if (content[currentContentType.value]?.schema?.format === 'binary') {
      binaryContentBase64.value = content[currentContentType.value].schema?.[valueKey] || '';
      uploadedFileName.value = content[currentContentType.value].schema?.[fileNameKey] || '';
      fileContentMediaType.value = 'application/javascript';
      currentContentType.value = 'application/octet-stream';
    } else {
      await loadPropsRawContent();
    }
    return;
  }
  if (currentContentType.value === 'application/xml') {
    if (content[currentContentType.value]?.schema?.format === 'binary') {
      binaryContentBase64.value = content[currentContentType.value].schema?.[valueKey] || '';
      uploadedFileName.value = content[currentContentType.value].schema?.[fileNameKey] || '';
      fileContentMediaType.value = 'application/xml';
      currentContentType.value = 'application/octet-stream';
    } else {
      await loadPropsRawContent();
    }
    return;
  }
  if (currentContentType.value === 'text/plain') {
    if (content[currentContentType.value]?.schema?.format === 'binary') {
      binaryContentBase64.value = content[currentContentType.value].schema?.[valueKey] || '';
      uploadedFileName.value = content[currentContentType.value].schema?.[fileNameKey] || '';
      fileContentMediaType.value = 'text/plain';
      currentContentType.value = 'application/octet-stream';
    } else {
      await loadPropsRawContent();
    }
    return;
  }

  if ((currentContentType.value && !allContentTypes.value.includes(currentContentType.value)) || currentContentType.value === 'application/octet-stream') {
    binaryContentBase64.value = content[currentContentType.value]?.schema?.[valueKey] || '';
    uploadedFileName.value = content[currentContentType.value]?.schema?.[fileNameKey] || '';
    fileContentMediaType.value = currentContentType.value || '';
    currentContentType.value = 'application/octet-stream';
  }
}, {
  immediate: true,
  deep: true
});

/**
 * Format raw content based on content type
 */
const formatRawContent = () => {
  if (currentContentType.value === 'application/json') {
    try {
      const json = JSON.parse(state.rawContent);
      state.rawContent = JSON.stringify(json, undefined, 2);
    } catch {}
  }
  if (currentContentType.value === 'application/xml') {
    try {
      state.rawContent = state.rawContent.replace(/(>)(<)(\/*)/g, '$1\n$2$3');
    } catch {}
  }
  if (currentContentType.value === 'text/html') {
    state.rawContent = pretty(state.rawContent, { indent_size: 2 });
  }

  if (currentContentType.value === 'application/javascript') {
    state.rawContent = jsBeautify(state.rawContent, { indent_size: 2, space_in_empty_paren: true });
  }
};

/**
 * Compress raw content by removing formatting
 */
const compressRawContent = () => {
  if (currentContentType.value === 'application/json') {
    try {
      const json = JSON.parse(state.rawContent);
      state.rawContent = JSON.stringify(json, undefined, 0);
    } catch {}
    return;
  }
  if (currentContentType.value === 'application/xml') {
    try {
      state.rawContent = state.rawContent.replace(/(>)(\n)(<)(\/*)/g, '$1$3$4');
    } catch {}
    return;
  }
  if (currentContentType.value === 'text/html') {
    state.rawContent = pretty(state.rawContent, { indent_size: 0 });
    return;
  }
  if (currentContentType.value === 'application/javascript') {
    state.rawContent = jsBeautify(state.rawContent, { indent_size: 0, space_in_empty_paren: true });
    return;
  }
  state.rawContent = state.rawContent.replace(/\n|\t|\r|\n\r/g, '');
};

// File size management
const formFileSize = ref(0);
const isGzipping = ref(false);
const uploadedFileSize = ref(0);

/**
 * Handle file upload
 * @param fileData - File upload data
 */
const handleFileUpload = async ({ file }: { file: File }) => {
  if (file.size > (plugins.value.maxFileSize - formFileSize.value)) {
    notification.error(t('xcan_apiBody.requestBodyFileSizeExceeded'));
    return;
  }
  uploadedFileSize.value = file.size;
  fileContentMediaType.value = file.type;
  emit('update:contentType', fileContentMediaType.value || 'application/octet-stream');

  isGzipping.value = true;
  setTimeout(async () => {
    const compressedFileData = await gzip(file);
    isGzipping.value = false;
    binaryContentBase64.value = compressedFileData;
    uploadedBinaryFile.value = file;
    uploadedFileName.value = file.name;
    emitParameterChange();
  }, 100);
};

/**
 * Handle file removal
 */
const handleFileRemoval = () => {
  uploadedFileName.value = undefined;
  uploadedBinaryBase64.value = undefined;
  uploadedBinaryFile.value = undefined;
  fileContentMediaType.value = undefined;
  binaryContentBase64.value = undefined;
  uploadedFileSize.value = 0;
  emit('update:contentType', 'application/octet-stream');
  emitParameterChange();
};
// const formMaxFileSize

// Watch for raw content changes with debounce
watch(() => state.rawContent, debounce(duration.search, () => {
  emitParameterChange();
}));

/**
 * Get binary file for parent component
 * @returns Binary file object
 */
const getBinaryFile = async () => {
  if (uploadedBinaryFile.value) {
    return uploadedBinaryFile.value;
  } else if (binaryContentBase64.value) {
    const buffer = await ungzip(binaryContentBase64.value);
    const file = await new File([buffer], uploadedFileName.value);
    uploadedFileSize.value = file.size;
    uploadedBinaryFile.value = file;
    return file;
  }
};

/**
 * Get binary base64 content for parent component
 * @returns Binary base64 content
 */
const getBinaryBase64 = () => {
  return uploadedBinaryBase64.value;
};

// Modal management
const isModalVisible = ref(false);
const shouldUseComponentReference = ref(false);

/**
 * Toggle model modal visibility
 * @param useRef - Whether to use component reference
 */
const toggleModelModal = (useRef = false) => {
  shouldUseComponentReference.value = useRef;
  isModalVisible.value = !isModalVisible.value;
};

/**
 * Import model data
 * @param data - Model data to import
 */
const importModelData = (data: any) => {
  if (shouldUseComponentReference.value) {
    componentReference.value = data.ref;
  } else {
    componentReference.value = undefined;
  }
  emit('change', { ...data.schema, $ref: componentReference.value || undefined });
};

// Watch for binary content changes
watch(() => binaryContentBase64.value, newValue => {
  if (!uploadedFileSize.value && newValue) {
    getBinaryFile();
  }
}, {
  immediate: true
});

// Watch for content types changes
watch(() => allContentTypes.value, (newValue) => {
  if (!newValue) {
    return;
  }
  const radioOptions: OptionItem[] = [{ value: null, label: 'none' }];
  radioOptions.push(...newValue?.filter(item => radioGroups.includes(item)).map(item => ({ label: item, value: item })));
  state.radioOptions = radioOptions;
  state.rawSelectOptions = newValue?.filter(item => !radioGroups.includes(item)).map(item => ({ label: item, value: item }));
}, {
  immediate: true
});

/**
 * Assemble request body data structure for frontend requests
 * @param dataSource - Data source object
 * @param contentType - Content type
 * @returns Request body data
 */
const getRequestBodyData = async (dataSource: any, contentType: string) => {
  const { content } = dataSource;
  if (!content) {
    return undefined;
  }
  if (!contentType) {
    return undefined;
  }
  if (contentType === 'application/x-www-form-urlencoded') {
    return state.encodeedList.filter(item => item[enabledKey] && !!item.name);
  } else if (contentType === 'multipart/form-data') {
    return state.formDataList.filter(item => item[enabledKey] && !!item.name);
  } else if (currentContentType.value === 'application/octet-stream') {
    return await getBinaryFile();
  } else {
    return state.rawContent;
  }
};

/**
 * Get data for saving body configuration
 * @returns Packaged body parameters
 */
const getData = () => {
  const param = packageRequestBodyParameters();
  return param;
};

/**
 * Update component data
 */
const updateComponentData = async () => {
  if (state.encodeedList && urlencodeFormRef.value) {
    await urlencodeFormRef.value.updateComp();
  }

  if (state.formDataList && formDataFormRef.value) {
    await formDataFormRef.value.updateComp();
  }
  const body = packageRequestBodyParameters();
  if (componentReference.value) {
    const paths = componentReference.value.split('/');
    const name = paths[paths.length - 1];
    await http.put(`${TESTER}/services/${apiBaseInfo.value.serviceId}/comp/requestBodies/${name}`, body);
  }
};

/**
 * Reset body data to initial state
 */
const resetRequestBodyData = () => {
  state.formDataList = [];
  state.encodeedList = [];
  state.rawContent = '';
  uploadedFileName.value = undefined;
  uploadedBinaryBase64.value = undefined;
  uploadedBinaryFile.value = undefined;
  fileContentMediaType.value = undefined;
  binaryContentBase64.value = undefined;
  uploadedFileSize.value = 0;
};

// Form references
const formDataFormRef = ref();
const urlencodeFormRef = ref();

/**
 * Validate form data
 * @param val - Validation flag
 */
const validateFormData = (val = true) => {
  if (currentContentType.value === 'application/x-www-form-urlencoded') {
    urlencodeFormRef.value.validate(val);
  }
  if (currentContentType.value === 'multipart/form-data') {
    formDataFormRef.value.validate(val);
  }
};

/**
 * Get model resolution data
 * @returns Model resolution object
 */
const getModelResolution = () => {
  const models = {};
  if (componentReference.value) {
    models[componentReference.value] = props.defaultValue;
  }
  if (urlencodedUseRef.value) {
    models[urlencodedUseRef.value] = props.defaultValue.content['application/x-www-form-urlencoded'];
    delete models[urlencodedUseRef.value]?.schema?.$ref;
  }
  if (formDataUseRef.value) {
    models[formDataUseRef.value] = props.defaultValue.content['multipart/form-data'];
    delete models[formDataUseRef.value]?.schema?.$ref;
  }
  if (shouldShowUrlEncodedForm.value) {
    urlencodeFormRef.value.getModelResolve(models);
  } else if (formDataFormRef.value) {
    formDataFormRef.value.getModelResolve(models);
  } else if (currentContentType.value && props.defaultValue.content?.[currentContentType.value]) {
    const contents = props.defaultValue.content?.[currentContentType.value];
    if (contents.schema.$ref) {
      models[contents.schema.$ref] = contents.schema;
    }
  }
  return models;
};

/**
 * Component mounted lifecycle hook
 */
onMounted(() => {
  nextTick(() => {
    emit('rendered', true);
  });
});

// Expose component methods
defineExpose({
  getBinaryFile,
  getBinaryBase64,
  getRequestBodyData,
  getBodyData: getRequestBodyData,
  getData,
  updateComponentData,
  validateFormData,
  getModelResolution,
  resetRequestBodyData
});
</script>
<template>
  <div>
    <div class="flex items-center flex-wrap whitespace-nowrap">
      <label class="text-3 leading-3 text-gray-content mr-4 select-none">
        <span class="mr-0.25">Content-Type</span>:
      </label>
      <RadioGroup
        v-model:value="currentContentType"
        class="select-none mr-2"
        name="radioGroup"
        :disabled="props.viewType"
        @change="handleContentTypeRadioChange">
        <Radio
          v-for="item in state.radioOptions"
          :key="item.value"
          :value="item.value">
          {{ item.label === 'application/octet-stream' ? 'binary' : item.label }}
        </Radio>
      </RadioGroup>
      <Radio
        :checked="isRawContentTypeSelected"
        :disabled="props.viewType"
        @change="handleRawContentTypeRadioClick">
        raw
      </Radio>
      <Select
        v-show="isRawContentType"
        v-model:value="currentContentType"
        class="w-40"
        dropdownClassName="response-body-select"
        :disabled="props.viewType"
        :allowClear="false"
        :options="state.rawSelectOptions"
        @change="handleContentTypeSelectChange" />
      <template v-if="!!currentContentType && apiBaseInfo?.serviceId && !props.viewType">
        <Button
          v-if="!props.hideImportBtn"
          type="link"
          size="small"
          @click="toggleModelModal(true)">
          {{ t('xcan_apiBody.importComponent') }}
        </Button>
        <Button
          type="link"
          size="small"
          @click="toggleModelModal(false)">
          {{ t('xcan_apiBody.copyComponent') }}
        </Button>
      </template>
      <template v-if="isRawContentType && state.rawContent && !props.viewType">
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
        v-show="shouldShowUrlEncodedForm"
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
        @change="handleUrlEncodedListChange"
        @del="deleteUrlEncodedListItem" />
      <template v-if="urlencodedUseRef" #title>
        {{ t('xcan_apiBody.componentReference', { ref: urlencodedUseRef }) }}
        <Button
          :disabled="props.viewType"
          size="small"
          type="link"
          @click="cancelUrlEncodedReference">
          {{ t('xcan_apiBody.cancelReference') }}
        </Button>
      </template>
    </Tooltip>
    <ApiFormForFlat
      v-else
      v-show="shouldShowUrlEncodedForm"
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
      @change="handleUrlEncodedListChange"
      @del="deleteUrlEncodedListItem" />
    <Tooltip v-if="!props.useFlatForm" placement="topLeft">
      <ApiForm
        v-show="shouldShowFormDataForm"
        ref="formDataFormRef"
        key="formDataList"
        v-model:formFileSize="formFileSize"
        :viewType="props.viewType"
        :maxFileSize="plugins.maxFileSize - uploadedFileSize"
        class="mt-5"
        :value="state.formDataList"
        :useModel="!!formDataUseRef"
        :hideImportBtn="props.hideImportBtn"
        :fieldNames="props.fieldNames"
        hasFileType
        @change="handleFormDataListChange"
        @del="deleteFormDataListItem" />
      <template v-if="formDataUseRef" #title>
        {{ t('xcan_apiBody.componentReference', { ref: formDataUseRef }) }}
        <Button
          :disabled="props.viewType"
          size="small"
          type="link"
          @click="cancelFormDataReference">
          {{ t('xcan_apiBody.cancelReference') }}
        </Button>
      </template>
    </Tooltip>
    <ApiFormForFlat
      v-else
      v-show="shouldShowFormDataForm"
      ref="formDataFormRef"
      key="formDataList"
      v-model:formFileSize="formFileSize"
      :viewType="props.viewType"
      :maxFileSize="plugins.maxFileSize - uploadedFileSize"
      class="mt-5"
      :value="state.formDataList"
      :useModel="!!formDataUseRef"
      :hideImportBtn="props.hideImportBtn"
      :fieldNames="props.fieldNames"
      hasFileType
      @change="handleFormDataListChange"
      @del="deleteFormDataListItem" />
    <AsyncComponent :visible="isRawContentType">
      <div
        v-show="isRawContentType"
        style="height: 220px;"
        class="w-full mt-2 py-2 border border-solid border-theme-divider rounded bg-white">
        <MonacoEditor
          v-model:value="state.rawContent"
          theme="vs"
          :readOnly="props.viewType"
          :isFormat="false"
          :notifyClear="clearRawContentTrigger"
          :language="monacoEditorLanguage" />
      </div>
    </AsyncComponent>
    <div v-show="currentContentType === 'application/octet-stream'" class="mt-2 flex items-center">
      <Upload
        :disabled="isGzipping || !!binaryContentBase64 || props.viewType"
        :showUploadList="false"
        :customRequest="handleFileUpload"
        :multiple="false"
        :fieldName="props.fieldNames">
        <Button
          :loading="isGzipping"
          :disabled="uploadedFileName || !!binaryContentBase64 || props.viewType"
          size="small">
          <Icon icon="icon-tuisongtongzhi" class="mr-1" />
          <span>{{ t('xcan_apiBody.uploadFile') }}</span>
        </Button>
      </Upload>
      <div v-show="uploadedFileName || binaryContentBase64" class="flex items-center h-5 leading-5 px-1.5 ml-1 select-none rounded text-3 border-none text-theme-content">
        <span>{{ uploadedFileName || binaryContentBase64 }}</span>
        <Icon
          icon="icon-shanchuguanbi"
          class="ml-1 text-gray-line cursor-pointer text-theme-text-hover"
          @click="handleFileRemoval" />
      </div>
    </div>
    <ModelModal v-model:visible="isModalVisible" @confirm="importModelData" />
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
