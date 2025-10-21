<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { IconRequired, Input, Modal, Select, SelectInput } from '@xcan-angus/vue-ui';
import { enumUtils } from '@xcan-angus/infra';
import { Button, Divider } from 'ant-design-vue';
import YAML from 'yaml';
import { useI18n } from 'vue-i18n';
import { services } from '@/api/tester';
import { API_EXTENSION_KEY } from '@/utils/apis';
import { OpenAPIV3_1 } from '@/types/openapi-types';
import { ServicesCompType } from '@/enums/enums';
import { ServicesCompDetail } from '@/views/apis/services/services/types';

import SelectEnum from '@/components/form/enum/SelectEnum.vue';

// Props for Add/Edit/View component modal
interface Props {
  visible: boolean;
  id: string;
  modalType:'add' | 'edit' | 'view';
  component?: Partial<ServicesCompDetail>
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  id: '',
  modalType: 'view',
  component: undefined
});

const { t } = useI18n();

// Emits: close modal and confirm save
const emit = defineEmits<{(e: 'update:visible', value:boolean): void, (e: 'ok'): void}>();

// Close modal without saving
const handleCancel = () => {
  emit('update:visible', false);
};

// Currently selected component collection type
const compType = ref<ServicesCompType>(ServicesCompType.schemas);

// Select options for component collections
const compTypesEnum = ref<{label:string, value:string, disabled:boolean}[]>([]);
const getCompTypesEnum = () => {
  const data = enumUtils.enumToMessages(ServicesCompType);
  compTypesEnum.value = data.map(item => ({
    label: item.message,
    value: item.value,
    disabled: [
      ServicesCompType.securitySchemes,
      ServicesCompType.links,
      ServicesCompType.callbacks,
      ServicesCompType.extensions,
      ServicesCompType.pathItems
    ].includes(item.value as unknown as ServicesCompType)
  }));
};

// Modal title derived from mode
const title = computed(() => {
  switch (props.modalType) {
    case 'view':
      return t('actions.view');
    case 'edit':
      return t('actions.edit');
    case 'add':
      return t('actions.add');
    default:
      return t('actions.detail');
  }
});

// Component name input
const compName = ref('');

// Example object state for "examples" type
const examples = ref<OpenAPIV3_1.ExampleObject>({
  value: '',
  summary: '',
  description: ''
});

// Header object state for "headers" type
const headers = ref<OpenAPIV3_1.HeaderObject>({
  description: '',
  schema: {
    type: 'string',
    format: undefined,
    [API_EXTENSION_KEY.valueKey]: ''
  }
});

// Safe access helpers for Header.schema (avoid $ref/boolean cases)
const isSchemaObject = (s: OpenAPIV3_1.ReferenceObject | OpenAPIV3_1.SchemaObject | undefined): s is OpenAPIV3_1.SchemaObject => {
  return !!s && typeof s === 'object' && !('$ref' in (s as any));
};

const schemaType = computed<string | undefined>({
  get () {
    return isSchemaObject(headers.value.schema) ? (headers.value.schema as any).type : undefined;
  },
  set (v) {
    if (isSchemaObject(headers.value.schema)) {
      (headers.value.schema as any).type = v as any;
    }
  }
});

const schemaFormat = computed<string | undefined>({
  get () {
    return isSchemaObject(headers.value.schema) ? (headers.value.schema as any).format : undefined;
  },
  set (v) {
    if (isSchemaObject(headers.value.schema)) {
      (headers.value.schema as any).format = v as any;
    }
  }
});

const schemaExtValue = computed<string | undefined>({
  get () {
    return isSchemaObject(headers.value.schema) ? (headers.value.schema as any)[API_EXTENSION_KEY.valueKey] : undefined;
  },
  set (v) {
    if (isSchemaObject(headers.value.schema)) {
      (headers.value.schema as any)[API_EXTENSION_KEY.valueKey] = v;
    }
  }
});

// Validate and dispatch save based on current component type
const handleSave = () => {
  if (!compName.value) {
    compNameErr.value = true;
    return;
  }
  switch (compType.value) {
    case ServicesCompType.examples:
      {
        if (!examples.value.value) {
          exampleValueErr.value = true;
          return;
        }
        let params:any = {
          ...examples.value
        };

        if (props.component?.isQuote) {
          params = {
            ...params,
            resolvedRefModels: null
          };
        }

        if (!examples.value.description) {
          delete params.description;
        }

        if (!examples.value.summary) {
          delete params.summary;
        }
        saveHeaderTypeData(params);
      }
      break;
    case ServicesCompType.headers:
      {
        const params: any = {
          ...headers.value.schema,
          description: headers.value.description
        };

        if (props.component?.isQuote) {
          (params as any).resolvedRefModels = null;
        }

        if (!headers.value.description) {
          delete params.description;
        }
        saveHeaderTypeData(params);
      }
      break;
  }
};

const loading = ref(false);
// Save component data by type
const saveHeaderTypeData = async (params):Promise<void> => {
  if (loading.value) {
    return;
  }
  loading.value = true;
  const [error] = await services.addComponent(props.id, compType.value, compName.value, params);
  loading.value = false;
  if (error) {
    return;
  }
  emit('update:visible', false);
  emit('ok');
};

// Populate form with defaults from quoted component
const setDefaultData = () => {
  const model = props.component?.model;
  switch (props.component?.type?.value) {
    case ServicesCompType.headers: {
      schemaType.value = (model?.schema as any)?.type;
      if (!['object', 'array'].includes((model?.schema as any)?.type)) {
        schemaFormat.value = (model?.schema as any)?.format;
      }
      if (model.description) {
        headers.value.description = model?.description;
      }
      if (model?.schema?.[API_EXTENSION_KEY.valueKey]) {
        schemaExtValue.value = (model.schema as any)[API_EXTENSION_KEY.valueKey];
      }
      break;
    }
    case ServicesCompType.examples: {
      examples.value.value = model?.value;
      if (model.description) {
        examples.value.description = model?.description;
      }
      if (model.summary) {
        examples.value.summary = model?.summary;
      }
      break;
    }
  }
};

// Exclude complex types for header format selection
const getExcludes = (option:{message:string, value:string}):boolean => {
  return ['object', 'array'].includes(option.value);
};

// Field validations (UI only)
const compNameErr = ref(false);
const compNameChange = (event:ChangeEvent) => {
  const value = event.target.value;
  compNameErr.value = !value;
};

const exampleValueErr = ref(false);
const exampleValueChange = (event:ChangeEvent) => {
  const value = event.target.value;
  exampleValueErr.value = !value;
};

// Mapping for number/integer formats displayed by SelectEnum
const enumKeyMap = {
  integer: 'IntegerParameterFormat',
  number: 'NumberParameterFormat'
};

// When editing quoted refs, allow temporary local change and recovery
const openEdit = ref(false);

const component = ref<ServicesCompDetail>();
const cancelRef = () => {
  component.value && (component.value.quoteName = '');
  openEdit.value = true;
};

const recoveryRef = () => {
  component.value && (component.value.quoteName = props.component?.quoteName);
  openEdit.value = false;
  setDefaultData();
};

// Stringified YAML of current component model for read-only view
const modeContent = computed(() => {
  if (!props.component) {
    return;
  }
  return YAML.stringify(props.component.model, null, 2);
});

onMounted(() => {
  getCompTypesEnum();
  if (props.component) {
    setDefaultData();
  }
});

onMounted(() => {
  if (props.component) {
    component.value = JSON.parse(JSON.stringify(props.component));
    compName.value = props.component.key || '';
    compType.value = (props.component?.type?.value || ServicesCompType.schemas) as ServicesCompType;
  }
});
</script>
<template>
  <Modal
    :visible="props.visible"
    :title="title"
    :footer="props.modalType === 'view'?null:''"
    :width="680"
    @cancel="handleCancel">
    <div class="text-3 text-text-sub-content">
      <div class="flex flex-col">
        <div>
          <IconRequired />
          {{ t('common.type') }}
        </div>
        <Select
          v-model:value="compType"
          :disabled="props.modalType === 'view' || props.modalType === 'edit'"
          :options="compTypesEnum"
          :placeholder="t('service.oas.addModal.typePlaceholder')"
          class="w-full mb-2" />
        <div>
          <IconRequired />
          {{ t('common.name') }}
        </div>
        <Input
          v-model:value="compName"
          :disabled="props.modalType === 'view' || props.modalType === 'edit'"
          :maxlength="400"
          :error="compNameErr"
          :placeholder="t('common.placeholders.searchKeyword')"
          class="w-full"
          @change="compNameChange" />
        <template v-if="props.component?.isQuote">
          <div class="pl-1.5 mt-2">{{ t('service.oas.addModal.refLabel') }}</div>
          <Input :value="component?.quoteName" disabled>
            <template #suffix>
              <a
                class="text-3 hover:text-text-link-hover text-text-content"
                @click="cancelRef">{{ t('service.oas.addModal.cancelRefs') }}</a>
              <Divider type="vertical" />
              <a
                class="text-3 hover:text-text-link-hover text-text-content"
                @click="recoveryRef">{{ t('service.oas.addModal.useRefs') }}</a>
            </template>
          </Input>
        </template>
        <template v-if="props.modalType === 'view' && ![ServicesCompType.headers,ServicesCompType.examples].includes(compType)">
          <div
            class="whitespace-pre border border-border-divider p-2 rounded mt-5 overflow-y-auto text-text-content"
            style="max-height: 500px;scrollbar-gutter: stable;">
            {{ modeContent }}
          </div>
        </template>
        <template v-else>
          <template v-if="compType === ServicesCompType.examples">
            <div class="mt-2 pl-1.75">{{ t('service.oas.addModal.summaryLabel') }}</div>
            <Input
              v-model:value="examples.summary"
              :maxlength="400"
              :disabled="props.modalType === 'view' || (!openEdit && modalType === 'edit' && props.component?.isQuote)"
              type="textarea"
              :placeholder="t('service.oas.addModal.summaryPlaceholder')"
              class="w-full" />
            <div class="mt-2 pl-1.75">{{ t('common.description') }}</div>
            <Input
              v-model:value="examples.description"
              :maxlength="2000"
              :disabled="props.modalType === 'view' || (!openEdit && modalType === 'edit' && props.component?.isQuote)"
              type="textarea"
              :placeholder="t('service.oas.addModal.descriptionPlaceholder')"
              class="w-full" />
            <div class="mt-2">
              <IconRequired />
              {{ t('common.example') }}
            </div>
            <Input
              v-model:value="examples.value"
              :disabled="props.modalType === 'view' || (!openEdit && modalType === 'edit' && props.component?.isQuote)"
              :maxlength="400"
              :autoSize="{ minRows: 8, maxRows: 20 }"
              :error="exampleValueErr"
              type="textarea"
              :placeholder="t('service.oas.addModal.examplePlaceholder')"
              class="w-full"
              @change="exampleValueChange" />
          </template>
          <template v-if="compType === ServicesCompType.headers">
            <div class="mt-2"><IconRequired />{{ t('service.oas.addModal.schemaTypeLabel') }}</div>
            <SelectEnum
              v-model:value="schemaType"
              :excludes="getExcludes"
              :disabled="props.modalType === 'view' || (!openEdit && modalType === 'edit' && props.component?.isQuote)"
              size="small"
              enumKey="ParameterType"
              :placeholder="t('service.oas.addModal.schemaTypePlaceholder')"
              class="w-full" />
            <div class="mt-2 pl-1.75">{{ t('common.format') }}</div>
            <template v-if="['integer','number'].includes(schemaType as any)">
              <SelectEnum
                v-model:value="schemaFormat"
                :enumKey="enumKeyMap[schemaType as any]"
                :disabled="props.modalType === 'view' || (!openEdit && modalType === 'edit' && props.component?.isQuote)"
                size="small"
                :placeholder="t('common.format')"
                class="w-full" />
            </template>
            <template v-else-if="schemaType === 'string'">
              <SelectInput
                v-model:value="schemaFormat"
                enumKey="StringParameterFormat"
                :fieldNames="{label:'message',value:'value'}"
                :disabled="props.modalType === 'view' || (!openEdit && modalType === 'edit' && props.component?.isQuote)"
                :placeholder="t('common.format')"
                class="w-full" />
            </template>
            <template v-else>
              <Input
                :value="schemaType"
                disabled
                class="w-full"
                @change="()=>{schemaFormat = schemaType as any}" />
            </template>
            <div class="pl-1.75 mt-2">{{ t('common.value') }}</div>
            <Input
              v-model:value="schemaExtValue"
              :maxlength="400"
              :disabled="props.modalType === 'view' || (!openEdit && modalType === 'edit' && props.component?.isQuote)"
              :placeholder="t('service.oas.addModal.valuePlaceholder')"
              class="w-full" />
            <div class="mt-2 pl-1.75">{{ t('common.description') }}</div>
            <Input
              v-model:value="headers.description"
              :maxlength="2000"
              :disabled="props.modalType === 'view' || (!openEdit && modalType === 'edit' && props.component?.isQuote)"
              :autoSize="{ minRows: 8, maxRows: 20 }"
              type="textarea"
              :placeholder="t('service.oas.addModal.descriptionPlaceholder')"
              class="w-full" />
          </template>
        </template>
      </div>
    </div>
    <template v-if="props.modalType !=='view'" #footer>
      <Button size="small" @click="handleCancel">{{ t('actions.cancel') }}</Button>
      <Button
        :disabled="!openEdit && modalType === 'edit' && props.component?.isQuote"
        type="primary"
        size="small"
        :loading="loading"
        @click="handleSave">
        {{ t('actions.confirm') }}
      </Button>
    </template>
  </Modal>
</template>
