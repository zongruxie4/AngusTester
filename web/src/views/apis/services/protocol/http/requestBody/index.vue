<script setup lang="ts">
import { computed, defineAsyncComponent, inject, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Icon, notification, Select } from '@xcan-angus/vue-ui';
import XML from 'xml';
import pretty from 'pretty';
import jsBeautify from 'js-beautify';

import { Button, Radio, RadioGroup, Tooltip, Upload } from 'ant-design-vue';
import SwaggerUI from '@xcan-angus/swagger-ui';
import { debounce } from 'throttle-debounce';
import { deconstruct } from '@/utils/swagger';
import { duration, codeUtils } from '@xcan-angus/infra';
import { services } from '@/api/tester';
import { SchemaType, SchemaFormat } from '@/types/openapi-types';

import { OptionItem, radioGroups, RequestBodyParam, StateItem } from './types';
import { deepParseJson, getBodyDefaultItem, getNewItem, transformRefJsonToDataJson } from '../utils';
import { API_EXTENSION_KEY, CONTENT_TYPE, getDataTypeFromFormat, getModelDataByRef } from '@/utils/apis';

import { ParamsItem } from '@/views/apis/services/protocol/types';
import { API_DEBUG_MAX_FILE_SIZE, API_DEBUG_MAX_FILES_SIZE, CONTENT_TYPE_KEYS, RADIO_TYPE_KEYS } from '@/utils/constant';

const ApiForm = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/requestBody/Form.vue'));
const MonacoEditor = defineAsyncComponent(() => import('@/components/MonacoEditor/index.vue'));
const ModelModal = defineAsyncComponent(() => import('./ModelModal.vue'));

const { t } = useI18n();
const { gzip, ungzip } = codeUtils;
const { valueKey, fileNameKey, enabledKey, contentEncoding } = API_EXTENSION_KEY;

interface Props {
  defaultValue: RequestBodyParam,
  binaryFile?: any;
  contentType: string | null;
}

const props = withDefaults(defineProps<Props>(), {
  defaultValue: undefined,
  binaryFile: undefined,
  contentType: null
});

const emits = defineEmits<{
  (e: 'change', value: RequestBodyParam): void;
  (e: 'update:binaryFile', value: any): void;
  (e: 'update:contentType', value: string): void;
}>();

const apiBaseInfo = inject('apiBaseInfo', ref());

// Clear raw content trigger
const clearRawContent = ref(0);

// Current content type for request body
const currentContentType = ref<string>();

// Binary file content in base64 format
const binaryContent = ref();

// File media type
const fileMediaType = ref();

/**
 * Check if raw content type is selected
 */
const isRawContentSelected = computed(() => {
  return !!state.rawSelectOptions.find(i => i.value === currentContentType.value);
});

// Component state management
const state = reactive<StateItem>({
  encodeedList: [], // URL encoded form parameters
  formDataList: [], // Multipart form data parameters
  rawContent: '', // Raw content for text-based content types
  radioOptions: [], // Radio button options for content types
  rawSelectOptions: [] // Select dropdown options for raw content types
});

const allContentTypes = ref<string[]>(CONTENT_TYPE);

/**
 * Package parameters into the correct format based on content type
 * @returns Packaged request body parameters
 */
const packageRequestParameters = () => {
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

  // Handle URL encoded form data
  if (currentContentType.value === CONTENT_TYPE_KEYS.FORM_URLENCODED) {
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
  } else if (currentContentType.value === CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA) {
    // Handle multipart form data
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
  } else if (currentContentType.value === RADIO_TYPE_KEYS.OCTET_STREAM) {
    // Handle binary file content
    const mediaType = fileMediaType.value || RADIO_TYPE_KEYS.OCTET_STREAM;
    if (!content[mediaType]) {
      content[mediaType] = {
        schema: {}
      };
    }
    if (!content[mediaType].schema) {
      content[mediaType].schema = {};
    }
    content[mediaType].schema.type = SchemaType.string;
    content[mediaType].schema.format = SchemaFormat.binary;
    content[mediaType].schema[contentEncoding] = 'gzip-base64';
    content[mediaType].schema[valueKey] = binaryContent.value;
    content[mediaType].schema[fileNameKey] = filename.value;

    // Clean up unused binary content types
    if (mediaType !== RADIO_TYPE_KEYS.OCTET_STREAM) {
      delete content[RADIO_TYPE_KEYS.OCTET_STREAM];
    }
    Object.keys(content).forEach(key => {
      if (key !== mediaType && content[key]?.schema?.format === SchemaFormat.binary) {
        delete content[key];
      }
    });
  } else {
    // Handle raw content types (JSON, XML, etc.)
    if (!content[currentContentType.value].schema) {
      content[currentContentType.value].schema = {};
    }
    content[currentContentType.value][valueKey] = state.rawContent;

    // Clean up unused content types
    Object.keys(content).forEach(key => {
      if (!content[key]?.[valueKey]) {
        delete content[key];
      }
    });
  }

  return {
    ...props.defaultValue,
    content,
    $ref: $ref.value
  };
};

/**
 * Handle radio button change for content type selection
 * @param e - Change event
 */
const handleContentTypeRadioChange = (e: any): void => {
  currentContentType.value = e.target.value;
  emits('update:contentType', currentContentType.value || '');
};

/**
 * Handle raw content radio button click
 * @param radioEvent - Radio button change event
 */
const handleRawContentRadioClick = (radioEvent) => {
  const isChecked = radioEvent.target.checked;
  if (isChecked) {
    currentContentType.value = state.rawSelectOptions[0]?.value;
  }
  emits('update:contentType', currentContentType.value || '');
  loadRawContentFromProps();
};

/**
 * Handle content type select dropdown change
 */
const handleContentTypeSelectChange = (): void => {
  emits('update:contentType', currentContentType.value || '');
  loadRawContentFromProps();
};

// URL encoded form data management
const urlencodedUseRef = ref();

/**
 * Handle URL encoded form data changes
 * @param index - Index of the parameter
 * @param data - Updated parameter data
 */
const handleUrlEncodedListChange = (index: number, data: ParamsItem): void => {
  state.encodeedList[index] = data;
  emitChange();
  if (urlencodedUseRef.value) {
    return;
  }
  // Add new empty item if no empty name elements exist
  const newItem = getNewItem(state.encodeedList);
  if (newItem) {
    state.encodeedList.push(newItem);
  }
};

/**
 * Cancel URL encoded reference
 */
const cancelUrlEncodedReference = () => {
  urlencodedUseRef.value = false;
  emitChange();
  const newItem = getNewItem(state.encodeedList);
  if (newItem) {
    state.encodeedList.push(newItem);
  }
};

/**
 * Delete URL encoded list item
 * @param index - Index of the item to delete
 */
const deleteUrlEncodedListItem = (index: number): void => {
  state.encodeedList.splice(index, 1);
  emitChange();
  // Add new empty item if no empty name elements exist
  const newItem = getNewItem(state.encodeedList);
  if (newItem) {
    state.encodeedList.push(newItem);
  }
};

// Multipart form data management
const formDataUseRef = ref();

/**
 * Handle multipart form data changes
 * @param index - Index of the parameter
 * @param data - Updated parameter data
 */
const handleFormDataListChange = (index: number, data: ParamsItem) => {
  state.formDataList[index] = data;
  emitChange();
  if (formDataUseRef.value) {
    return;
  }
  // Add new empty item if no empty name elements exist
  const newItem = getNewItem(state.formDataList);
  if (newItem) {
    state.formDataList.push(newItem);
  }
};

/**
 * Delete multipart form data list item
 * @param index - Index of the item to delete
 */
const deleteFormDataListItem = (index: number) => {
  state.formDataList.splice(index, 1);
  // Add new empty item if no empty name elements exist
  const newItem = getNewItem(state.formDataList);
  if (newItem) {
    state.formDataList.push(newItem);
  }
  emitChange();
};

/**
 * Cancel multipart form data reference
 */
const cancelFormDataReference = () => {
  formDataUseRef.value = false;
  emitChange();
  const newItem = getNewItem(state.formDataList);
  if (newItem) {
    state.formDataList.push(newItem);
  }
};

const emitChange = (): void => {
  const params = packageRequestParameters();
  emits('change', params);
};

const isRaw = computed(() => {
  return currentContentType.value && [CONTENT_TYPE_KEYS.JSON, CONTENT_TYPE_KEYS.HTML, CONTENT_TYPE_KEYS.JAVASCRIPT, CONTENT_TYPE_KEYS.XML, CONTENT_TYPE_KEYS.WILDCARD, CONTENT_TYPE_KEYS.TEXT_PLAIN].includes(currentContentType.value);
});

const showEncodedForm = computed(() => {
  return currentContentType.value === CONTENT_TYPE_KEYS.FORM_URLENCODED;
});

const showFormDataForm = computed(() => {
  return currentContentType.value === CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA;
});

const language = computed(() => {
  switch (currentContentType.value) {
    case CONTENT_TYPE_KEYS.JSON:
      return 'json';
    case CONTENT_TYPE_KEYS.HTML:
    case CONTENT_TYPE_KEYS.XML:
      return 'html';
    case CONTENT_TYPE_KEYS.JAVASCRIPT:
      return 'typescript';
    case CONTENT_TYPE_KEYS.TEXT_PLAIN:
      return 'text';
    default:
      return 'text';
  }
});
// File upload management
const filename = ref(); // File name
const binaryFile = ref(); // Uploaded file as blob
const binaryBase64 = ref(); // Uploaded file as base64

/**
 * Fetch model data by reference
 * @param ref - Model reference
 * @returns Deconstructed model data
 */
const fetchModelData = async (ref) => {
  hasUseDefaultValue = true;
  const [error, { data }] = await getModelDataByRef(apiBaseInfo.value.serviceId, ref);
  if (error) {
    return {};
  }
  return deconstruct(data || {});
};

const $ref = ref();
let hasUseDefaultValue = false;
/**
 * Load raw content from props based on current content type
 */
const loadRawContentFromProps = async () => {
  const { content } = props.defaultValue;
  const type = currentContentType.value;
  if (!type || !content) {
    return;
  }

  // Handle schema reference
  if (content[type]?.schema?.$ref && !content[type]?.schema?.type && !content[type]?.schema?.properties && !content[type]?.schema?.items) {
    hasUseDefaultValue = true;
    const [error, res] = await getModelDataByRef(apiBaseInfo.value.serviceId, content[type]?.schema.$ref);
    if (error) {
      return;
    }
    const model = JSON.parse(res.data.model);
    if (!model.type) {
      if (model.properties) {
        model.type = SchemaType.object;
      } else if (model.items) {
        model.type = SchemaType.array;
      } else {
        model.type = SchemaType.string;
      }
    }
    emits('change', {
      ...props.defaultValue,
      content: { ...props.defaultValue.content, [type]: { ...content[type], schema: { ...content[type]?.schema, ...model } } }
    });
    await loadRawContentFromProps();
  }
  if (type === CONTENT_TYPE_KEYS.JSON) {
    if (content?.[type]?.schema?.format === SchemaFormat.binary) {
      state.rawContent = '';
      return;
    }
    const typeBodyContent = JSON.parse(JSON.stringify(content[type] || content[CONTENT_TYPE_KEYS.WILDCARD] || {}));
    if (typeBodyContent.schema && typeBodyContent?.[valueKey] === undefined) {
      typeBodyContent.schema = await transformRefJsonToDataJson(typeBodyContent.schema, apiBaseInfo.value.serviceId);
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
    return;
  }
  if (type === CONTENT_TYPE_KEYS.XML) {
    if (content?.[type]?.schema?.format === SchemaFormat.binary) {
      state.rawContent = '';
      return;
    }
    const typeBodyContent = JSON.parse(JSON.stringify(content[type] || content[CONTENT_TYPE_KEYS.WILDCARD] || {}));
    if (typeBodyContent.schema && typeBodyContent?.[valueKey] === undefined) {
      typeBodyContent.schema = await transformRefJsonToDataJson(typeBodyContent.schema, apiBaseInfo.value.serviceId);
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
  const typeBodyContent = JSON.parse(JSON.stringify(content[type] || content[CONTENT_TYPE_KEYS.WILDCARD] || {}));
  if (typeBodyContent.schema && typeBodyContent?.[valueKey] === undefined) {
    typeBodyContent.schema = await transformRefJsonToDataJson(typeBodyContent.schema, apiBaseInfo.value.serviceId);
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
  if (hasUseDefaultValue) {
    return;
  }
  const { content } = newValue;
  if (newValue.$ref) {
    $ref.value = newValue.$ref;
    if (!content) {
      hasUseDefaultValue = true;
      const [error, res] = await getModelDataByRef(apiBaseInfo.value.serviceId, newValue.$ref);
      if (error) {
        emits('change', { ...newValue, content: {} });
        return;
      }
      if (!res.data?.model) {
        emits('change', { ...newValue, content: {} });
        return;
      }
      const model = JSON.parse(res.data.model);
      emits('change', model);
      return;
    }
  }

  currentContentType.value = props.contentType || undefined;

  if (!currentContentType.value) {
    currentContentType.value = undefined;
  }
  for (const type in (content || {})) {
    if (type === CONTENT_TYPE_KEYS.FORM_URLENCODED) {
      if ((state.encodeedList.filter(i => i.name || i[valueKey] || i.type !== SchemaType.string)).length < 1) {
        const use$ref = !content[CONTENT_TYPE_KEYS.FORM_URLENCODED]?.schema?.properties && content[CONTENT_TYPE_KEYS.FORM_URLENCODED]?.schema?.$ref;
        if (use$ref) {
          const model = await fetchModelData(use$ref);
          if (model) {
            if (!model.type) {
              if (model.properties) {
                model.type = SchemaType.object;
              } else if (model.items) {
                model.type = SchemaType.array;
              } else {
                model.type = SchemaType.string;
              }
            }
            content[CONTENT_TYPE_KEYS.FORM_URLENCODED].schema = {
              $ref: use$ref,
              ...model
            };
            urlencodedUseRef.value = use$ref;
          }
        }
        state.encodeedList = Object.keys(content[CONTENT_TYPE_KEYS.FORM_URLENCODED]?.schema?.properties || {}).map(key => {
          const itemSchema = content[CONTENT_TYPE_KEYS.FORM_URLENCODED].schema?.properties?.[key];
          if (!itemSchema.type && itemSchema.format) {
            itemSchema.type = getDataTypeFromFormat(itemSchema.format);
          }
          if (!itemSchema.type && itemSchema.properties) {
            itemSchema.type = SchemaType.object;
          }
          if (!itemSchema.type && itemSchema.items) {
            itemSchema.type = SchemaType.array;
          }
          if ([SchemaType.object, SchemaType.array, SchemaType.number, SchemaType.integer, SchemaType.boolean].includes(itemSchema.type) && typeof itemSchema[valueKey] === 'string') {
            try {
              itemSchema[valueKey] = JSON.parse(itemSchema[valueKey]);
            } catch {
            }
          }
          return {
            name: key,
            [enabledKey]: true,
            ...itemSchema
          };
        });
        if (state.encodeedList.every(i => !!i.name) && !urlencodedUseRef.value) {
          state.encodeedList.push(getBodyDefaultItem({ type: SchemaType.string }));
        }
      }
    } else if (type === CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA) {
      if (state.formDataList.filter(i => i.name || i[valueKey] || i.type !== SchemaType.string || i.format).length < 1) {
        const use$ref = !content[CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA]?.schema?.properties && content[CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA]?.schema?.$ref;
        if (use$ref) {
          const model = await fetchModelData(use$ref);
          if (!model.type) {
            if (model.properties) {
              model.type = SchemaType.object;
            } else if (model.items) {
              model.type = SchemaType.array;
            } else {
              model.type = SchemaType.string;
            }
          }
          content[CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA].schema = {
            $ref: use$ref,
            ...model
          };
          formDataUseRef.value = use$ref;
        }
        state.formDataList = Object.keys(content[CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA]?.schema?.properties || {}).map(key => {
          const itemSchema = content[CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA].schema?.properties?.[key];
          if (!itemSchema.type && itemSchema.format) {
            itemSchema.type = getDataTypeFromFormat(itemSchema.format);
          }
          if (!itemSchema.type && itemSchema.properties) {
            itemSchema.type = SchemaType.object;
          }
          if (!itemSchema.type && itemSchema.items) {
            itemSchema.type = SchemaType.array;
          }
          if (([SchemaType.object, SchemaType.array, SchemaType.number, SchemaType.integer, SchemaType.boolean].includes(itemSchema.type) || itemSchema.format === SchemaFormat.binary) && typeof itemSchema[valueKey] === 'string') {
            try {
              itemSchema[valueKey] = JSON.parse(itemSchema[valueKey]);
            } catch {
            }
          }
          return {
            name: key,
            [enabledKey]: true,
            ...itemSchema
          };
        });
        if (state.formDataList.every(i => !!i.name) && !formDataUseRef.value) {
          state.formDataList.push(getBodyDefaultItem({ type: SchemaType.string }));
        }
      }
    } else if ((type && !allContentTypes.value.includes(type)) || type === RADIO_TYPE_KEYS.OCTET_STREAM) {
      binaryContent.value = content[type].schema?.[valueKey] || '';
      filename.value = content[type].schema?.[fileNameKey] || '';
      fileMediaType.value = type || '';
    }
  }
  if (state.formDataList.every(i => !!i.name) && !formDataUseRef.value) {
    state.formDataList.push(getBodyDefaultItem({ type: SchemaType.string }));
  }
  if (state.encodeedList.every(i => !!i.name) && !urlencodedUseRef.value) {
    state.encodeedList.push(getBodyDefaultItem({ type: SchemaType.string }));
  }
  if (currentContentType.value === CONTENT_TYPE_KEYS.JSON) {
    if (content?.[currentContentType.value]?.schema?.format === SchemaFormat.binary) {
      binaryContent.value = content[currentContentType.value].schema?.[valueKey] || '';
      filename.value = content[currentContentType.value].schema?.[fileNameKey] || '';
      fileMediaType.value = CONTENT_TYPE_KEYS.JSON;
      currentContentType.value = RADIO_TYPE_KEYS.OCTET_STREAM;
    } else {
      await loadRawContentFromProps();
    }
    return;
  }
  if (currentContentType.value === CONTENT_TYPE_KEYS.HTML) {
    if (content?.[currentContentType.value]?.schema?.format === SchemaFormat.binary) {
      binaryContent.value = content[currentContentType.value].schema?.[valueKey] || '';
      filename.value = content[currentContentType.value].schema?.[fileNameKey] || '';
      fileMediaType.value = CONTENT_TYPE_KEYS.HTML;
      currentContentType.value = RADIO_TYPE_KEYS.OCTET_STREAM;
    } else {
      await loadRawContentFromProps();
    }
    return;
  }
  if (currentContentType.value === CONTENT_TYPE_KEYS.JAVASCRIPT) {
    if (content[currentContentType.value]?.schema?.format === SchemaFormat.binary) {
      binaryContent.value = content[currentContentType.value].schema?.[valueKey] || '';
      filename.value = content[currentContentType.value].schema?.[fileNameKey] || '';
      fileMediaType.value = CONTENT_TYPE_KEYS.JAVASCRIPT;
      currentContentType.value = RADIO_TYPE_KEYS.OCTET_STREAM;
    } else {
      await loadRawContentFromProps();
    }
    return;
  }
  if (currentContentType.value === CONTENT_TYPE_KEYS.XML) {
    if (content[currentContentType.value]?.schema?.format === SchemaFormat.binary) {
      binaryContent.value = content[currentContentType.value].schema?.[valueKey] || '';
      filename.value = content[currentContentType.value].schema?.[fileNameKey] || '';
      fileMediaType.value = CONTENT_TYPE_KEYS.XML;
      currentContentType.value = RADIO_TYPE_KEYS.OCTET_STREAM;
    } else {
      await loadRawContentFromProps();
    }
    return;
  }
  if (currentContentType.value === CONTENT_TYPE_KEYS.TEXT_PLAIN) {
    if (content[currentContentType.value]?.schema?.format === SchemaFormat.binary) {
      binaryContent.value = content[currentContentType.value].schema?.[valueKey] || '';
      filename.value = content[currentContentType.value].schema?.[fileNameKey] || '';
      fileMediaType.value = CONTENT_TYPE_KEYS.TEXT_PLAIN;
      currentContentType.value = RADIO_TYPE_KEYS.OCTET_STREAM;
    } else {
      await loadRawContentFromProps();
    }
    return;
  }

  if ((currentContentType.value && !allContentTypes.value.includes(currentContentType.value)) || currentContentType.value === RADIO_TYPE_KEYS.OCTET_STREAM) {
    binaryContent.value = content[currentContentType.value]?.schema?.[valueKey] || '';
    filename.value = content[currentContentType.value]?.schema?.[fileNameKey] || '';
    fileMediaType.value = currentContentType.value || '';
    currentContentType.value = RADIO_TYPE_KEYS.OCTET_STREAM;
  }
}, {
  immediate: true,
  deep: true
});

/**
 * Format raw content based on content type
 */
const formatRawContent = () => {
  if (currentContentType.value === CONTENT_TYPE_KEYS.JSON) {
    try {
      const json = JSON.parse(state.rawContent);
      state.rawContent = JSON.stringify(json, undefined, 2);
    } catch {
      // Keep original content if parsing fails
    }
  }
  if (currentContentType.value === CONTENT_TYPE_KEYS.XML) {
    try {
      state.rawContent = state.rawContent.replace(/(>)(<)(\/*)/g, '$1\n$2$3');
    } catch {
      // Keep original content if processing fails
    }
  }
  if (currentContentType.value === CONTENT_TYPE_KEYS.HTML) {
    state.rawContent = pretty(state.rawContent, { indent_size: 2 });
  }

  if (currentContentType.value === CONTENT_TYPE_KEYS.JAVASCRIPT) {
    state.rawContent = jsBeautify(state.rawContent, { indent_size: 2, space_in_empty_paren: true });
  }
};

/**
 * Compress raw content by removing unnecessary whitespace
 */
const compressRawContent = () => {
  if (currentContentType.value === CONTENT_TYPE_KEYS.JSON) {
    try {
      const json = JSON.parse(state.rawContent);
      state.rawContent = JSON.stringify(json, undefined, 0);
    } catch {
      // Keep original content if parsing fails
    }
    return;
  }
  if (currentContentType.value === CONTENT_TYPE_KEYS.XML) {
    try {
      state.rawContent = state.rawContent.replace(/(>)(\n)(<)(\/*)/g, '$1$3$4');
    } catch {
      // Keep original content if processing fails
    }
    return;
  }
  if (currentContentType.value === CONTENT_TYPE_KEYS.HTML) {
    state.rawContent = pretty(state.rawContent, { indent_size: 0 });
    return;
  }
  if (currentContentType.value === CONTENT_TYPE_KEYS.JAVASCRIPT) {
    state.rawContent = jsBeautify(state.rawContent, { indent_size: 0, space_in_empty_paren: true });
    return;
  }
  state.rawContent = state.rawContent.replace(/\n|\t|\r|\n\r/g, '');
};

const formFileSize = ref(0);

// File upload state management
const isGzipping = ref(false);
const fileSize = ref(0);

/**
 * Handle file upload
 * @param param - Upload parameter containing file
 */
const handleFileUpload = async ({ file }) => {
  if (file.size > (API_DEBUG_MAX_FILES_SIZE - formFileSize.value)) {
    notification.error(t('service.apiRequestBody.messages.debugFileSizeLimit'));
    return;
  }
  fileSize.value = file.size;
  fileMediaType.value = file.type;
  emits('update:contentType', fileMediaType.value || RADIO_TYPE_KEYS.OCTET_STREAM);

  isGzipping.value = true;
  setTimeout(async () => {
    const fileBase = await gzip(file);
    isGzipping.value = false;
    binaryContent.value = fileBase;
    binaryFile.value = file;
    filename.value = file.name;
    emitChange();
  }, 100);
};

/**
 * Handle file removal
 */
const handleFileRemoval = () => {
  filename.value = undefined;
  binaryBase64.value = undefined;
  binaryFile.value = undefined;
  fileMediaType.value = undefined;
  binaryContent.value = undefined;
  fileSize.value = 0;
  emits('update:contentType', RADIO_TYPE_KEYS.OCTET_STREAM);
  emitChange();
};

/**
 * Get binary file for request
 * @returns Binary file or undefined
 */
const getBinaryFile = async () => {
  if (binaryFile.value) {
    return binaryFile.value;
  } else if (binaryContent.value) {
    const buf = await ungzip(binaryContent.value);
    const file = new File([buf], filename.value);
    fileSize.value = file.size;
    binaryFile.value = file;
    return file;
  }
};

/**
 * Get binary file as base64 string
 * @returns Base64 string or undefined
 */
const getBinaryBase64 = () => {
  return binaryBase64.value;
};

// Model modal state management
const isModalVisible = ref(false);
const useComponentReference = ref(false);

/**
 * Toggle model modal visibility
 * @param useRef - Whether to use component reference
 */
const toggleModelModal = (useRef = false) => {
  useComponentReference.value = useRef;
  isModalVisible.value = !isModalVisible.value;
};

/**
 * Import model data
 * @param data - Model data to import
 */
const importModelData = (data) => {
  if (useComponentReference.value) {
    $ref.value = data.ref;
  } else {
    $ref.value = undefined;
  }
  emits('change', { ...data.schema, $ref: $ref.value || undefined });
};

/**
 * Assemble body data structure for frontend request
 * @param dataSource - Data source object
 * @param contentType - Content type for the request
 * @returns Body data or undefined
 */
const getBodyData = async (dataSource, contentType) => {
  const { content } = dataSource;
  if (!content) {
    return undefined;
  }
  if (!contentType) {
    return undefined;
  }

  if (contentType === CONTENT_TYPE_KEYS.FORM_URLENCODED) {
    return state.encodeedList.filter(i => i[enabledKey] && !!i.name);
  } else if (contentType === CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA) {
    return state.formDataList.filter(i => i[enabledKey] && !!i.name);
  } else if (currentContentType.value === RADIO_TYPE_KEYS.OCTET_STREAM) {
    return await getBinaryFile();
  } else {
    return state.rawContent;
  }
};

/**
 * Update components with current data
 */
const updateComponents = async () => {
  if (state.encodeedList && urlEncodeFormRef.value) {
    await urlEncodeFormRef.value.updateComp();
  }

  if (state.formDataList && formDataFormRef.value) {
    await formDataFormRef.value.updateComp();
  }
  const body = packageRequestParameters();
  if ($ref.value) {
    const paths = $ref.value.split('/');
    const name = paths[paths.length - 1];
    await services.addComponent(apiBaseInfo.value.serviceId, 'requestBodies', name, body);
  }
};

// Form component references
const formDataFormRef = ref();
const urlEncodeFormRef = ref();

/**
 * Validate form data based on content type
 * @param enable - Whether to enable validation
 */
const validateFormData = (enable = true) => {
  if (currentContentType.value === CONTENT_TYPE_KEYS.FORM_URLENCODED) {
    urlEncodeFormRef.value.validate(enable);
  }
  if (currentContentType.value === CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA) {
    formDataFormRef.value.validate(enable);
  }
};

/**
 * Resolve model references for all components
 * @returns Models object with resolved references
 */
const resolveModelReferences = () => {
  const models = {};
  if ($ref.value) {
    models[$ref.value] = props.defaultValue;
  }
  if (urlencodedUseRef.value) {
    models[urlencodedUseRef.value] = props.defaultValue.content[CONTENT_TYPE_KEYS.FORM_URLENCODED];
    delete models[urlencodedUseRef.value]?.schema?.$ref;
  }
  if (formDataUseRef.value) {
    models[formDataUseRef.value] = props.defaultValue.content[CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA];
    delete models[formDataUseRef.value]?.schema?.$ref;
  }
  if (showEncodedForm.value) {
    urlEncodeFormRef.value.getModelResolve(models);
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
 * Check if file size exceeds maximum allowed size
 * @returns True if file size exceeds limit
 */
const isFileSizeExceeded = () => {
  return formFileSize.value + fileSize.value > API_DEBUG_MAX_FILE_SIZE;
};

/**
 * Check if file size exceeds maximum request size
 * @returns True if file size exceeds request limit
 */
const isRequestSizeExceeded = () => {
  return formFileSize.value + fileSize.value > (API_DEBUG_MAX_FILE_SIZE * 2);
};

watch(() => state.rawContent, debounce(duration.search, () => {
  emitChange();
}));

watch(() => binaryContent.value, newValue => {
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
  const radioOptions: OptionItem[] = [{ value: null, label: 'none' }];
  radioOptions.push(...newValue?.filter(item => radioGroups.includes(item)).map(item => ({ label: item, value: item })));
  state.radioOptions = radioOptions;
  state.rawSelectOptions = newValue?.filter(item => !radioGroups.includes(item)).map(item => ({
    label: item,
    value: item
  }));
}, {
  immediate: true
});

defineExpose({
  getBinaryFile,
  getBinaryBase64,
  getBodyData,
  updateComp: updateComponents,
  validate: validateFormData,
  getModelResolve: resolveModelReferences,
  ifExceedSize: isFileSizeExceeded,
  ifExceedRequestSize: isRequestSizeExceeded
});
</script>
<template>
  <div>
    <div class="flex items-center h-8 flex-nowrap whitespace-nowrap">
      <label class="text-3 leading-3 text-gray-content mr-4 select-none">
        <span class="mr-0.25">{{ t('protocol.contentType') }}</span>:
      </label>
      <RadioGroup
        v-model:value="currentContentType"
        class="select-none"
        name="radioGroup"
        @change="handleContentTypeRadioChange">
        <Radio
          v-for="item in state.radioOptions"
          :key="item.value"
          :value="item.value">
          {{ item.label === RADIO_TYPE_KEYS.OCTET_STREAM ? SchemaFormat.binary : item.label }}
        </Radio>
      </RadioGroup>
      <Radio :checked="isRawContentSelected" @change="handleRawContentRadioClick">raw</Radio>
      <Select
        v-show="isRaw"
        v-model:value="currentContentType"
        class="w-40"
        dropdownClassName="response-body-select"
        :allowClear="false"
        :options="state.rawSelectOptions"
        @change="handleContentTypeSelectChange" />
      <template v-if="!!currentContentType && apiBaseInfo.serviceId">
        <Button
          type="link"
          size="small"
          @click="toggleModelModal(true)">
          {{ t('service.apiRequestBody.actions.importComponent') }}
        </Button>
        <Button
          type="link"
          size="small"
          @click="toggleModelModal(false)">
          {{ t('service.apiRequestBody.actions.copyComponent') }}
        </Button>
      </template>
      <template v-if="isRaw && state.rawContent">
        <Button
          type="link"
          size="small"
          @click="formatRawContent">
          {{ t('service.apiRequestBody.actions.format') }}
        </Button>
        <Button
          type="link"
          size="small"
          @click="compressRawContent">
          {{ t('service.apiRequestBody.actions.compress') }}
        </Button>
      </template>
      <template
        v-if="currentContentType && [CONTENT_TYPE_KEYS.MULTIPART_FORM_DATA, RADIO_TYPE_KEYS.OCTET_STREAM].includes(currentContentType)">
        <Tooltip>
          <template #title><span>{{ t('service.apiRequestBody.tips.fileSizeLimit') }}</span></template>
          <Icon icon="icon-tishi1" class="ml-2 text-http-put text-3.5" />
        </Tooltip>
      </template>
    </div>
    <Tooltip placement="topLeft">
      <ApiForm
        v-show="showEncodedForm"
        key="encodeedList"
        ref="urlEncodeFormRef"
        v-model:formFileSize="formFileSize"
        class="mt-5"
        :maxFileSize="0"
        :useModel="!!urlencodedUseRef"
        :value="state.encodeedList"
        @change="handleUrlEncodedListChange"
        @del="deleteUrlEncodedListItem" />
      <template v-if="urlencodedUseRef" #title>
        {{ t('service.apiRequestBody.tips.componentReference', {ref: urlencodedUseRef}) }}
        <Button
          size="small"
          type="link"
          @click="cancelUrlEncodedReference">
          {{ t('service.apiRequestBody.actions.cancelReference') }}
        </Button>
      </template>
    </Tooltip>
    <Tooltip placement="topLeft">
      <ApiForm
        v-show="showFormDataForm"
        ref="formDataFormRef"
        key="formDataList"
        v-model:formFileSize="formFileSize"
        :maxFileSize="API_DEBUG_MAX_FILES_SIZE - fileSize"
        class="mt-5"
        :value="state.formDataList"
        :useModel="!!formDataUseRef"
        hasFileType
        @change="handleFormDataListChange"
        @del="deleteFormDataListItem" />
      <template v-if="formDataUseRef" #title>
        {{ t('service.apiRequestBody.tips.componentReference', {ref: formDataUseRef}) }}
        <Button
          size="small"
          type="link"
          @click="cancelFormDataReference">
          {{ t('service.apiRequestBody.actions.cancelReference') }}
        </Button>
      </template>
    </Tooltip>
    <AsyncComponent :visible="isRaw">
      <div
        v-show="isRaw"
        style="height: calc(100% - 50px);"
        class="w-full mt-2 py-4 border border-solid border-theme-text-box rounded">
        <MonacoEditor
          v-model:value="state.rawContent"
          theme="vs"
          :notifyClear="clearRawContent"
          :language="language"
          class="w-full h-full" />
      </div>
    </AsyncComponent>
    <div v-show="currentContentType === RADIO_TYPE_KEYS.OCTET_STREAM" class="mt-2 flex items-center">
      <Upload
        :disabled="isGzipping || !!binaryContent"
        :showUploadList="false"
        :customRequest="handleFileUpload"
        :multiple="false">
        <Button
          :loading="isGzipping"
          :disabled="filename || !!binaryContent"
          class="text-3 bg-gray-bg rounded">
          <Icon icon="icon-tuisongtongzhi" class="mr-2" />
          {{ t('service.apiRequestBody.actions.uploadFile') }}
        </Button>
      </Upload>
      <div
        v-show="filename || binaryContent"
        class="flex items-center h-5 px-1.5 ml-1 select-none rounded text-3 border-none text-text-content bg-gray-bg">
        <span>{{ filename || binaryContent }}</span>
        <Icon
          icon="icon-shanchuguanbi"
          class="ml-1 text-gray-line cursor-pointer text-theme-text-hover"
          @click="handleFileRemoval" />
      </div>
    </div>
    <ModelModal v-model:visible="isModalVisible" @confirm="importModelData">
    </ModelModal>
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
  @apply h-7 text-3 leading-3 rounded border-border-input;
}

.ant-select:not(.ant-select-customize-input) :deep(.ant-select-selector) .ant-select-selection-item {
  @apply leading-6.5;
}
</style>
