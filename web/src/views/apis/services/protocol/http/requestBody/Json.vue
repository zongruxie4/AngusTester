<script setup lang="ts">
import { computed, inject, ref, watch, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { toClipboard, utils } from '@xcan-angus/infra';
import { Icon, Input, Select, SelectSchema, notification } from '@xcan-angus/vue-ui';
import { Button, Checkbox } from 'ant-design-vue';
import {
  API_EXTENSION_KEY,
  deepDelAttrFromObj,
  getModelDataByRef,
  QueryAndPathInOption,
  schemaTypeToOption
} from '@/utils/apis';
import SwaggerUI from '@xcan-angus/swagger-ui';
import { deconstruct } from '@/utils/swagger';
import Ajv from 'ajv';
import addFormats from 'ajv-formats';
import { services } from '@/api/tester';
import { SchemaType } from '@/types/openapi-types';
import { API_PARAMETER_NAME_LENGTH, API_PARAMETER_VALUE_LENGTH } from '@/utils/constant';
import { ParamsInfo } from '@/views/apis/services/protocol/http/types';

import { transJsonToList, transListToJson, transListToSchema } from '@/utils/apis/index';

const SimpleEditableSelect = defineAsyncComponent(() => import('@/components/form/EditableSelector.vue'));
const ParamInput = defineAsyncComponent(() => import('@/components/form/ParamInput/index.vue'));

const { t } = useI18n();

const { valueKey } = API_EXTENSION_KEY;
const apiBaseInfo = inject('apiBaseInfo', ref());

const ajv = new Ajv();
addFormats(ajv);

interface Item {
  type: string;
  [valueKey]: string | number | boolean | undefined;
  id: string;
  name?: string;
  pid?: string | number;
  level: number;
  idLine: (string | number)[];
  $ref?: string;
  enum?: string[];
  checked?: boolean;
  schema?: any;
}

interface Props {
  data: any,
  pType: SchemaType.array|SchemaType.object;
  disabled: boolean;
  schema: Record<string, any>;
  paramInType?: 'path'|'query'
}

const props = withDefaults(defineProps<Props>(), {
  data: () => ([]),
  pType: SchemaType.array,
  disabled: false,
  paramInType: undefined
});

const emit = defineEmits<{(e: 'update:data', value: any):void, (e: 'change', value: any): void}>();

/**
 * Emit data and schema changes to parent component
 */
const emitDataChange = () => {
  const jsonData = transListToJson(dataSource.value, props.pType);
  const jsonSchema = transListToSchema(dataSource.value, props.pType);
  emit('update:data', jsonData);
  emit('change', jsonSchema);
};

/**
 * Check if value input should be disabled for an item
 * @param item - Item to check
 * @returns True if value input should be disabled
 */
const isValueInputDisabled = item => {
  return item.type === SchemaType.object || item.type === SchemaType.array;
};

/**
 * Check if type selection should be disabled for an item
 * @param item - Item to check
 * @returns True if type selection should be disabled
 */
const isTypeSelectionDisabled = (item) => {
  if (props.disabled || item.$ref) {
    return true;
  }
  return isItemDisabled(item);
};

/**
 * Check if name input should be disabled for an item
 * @param item - Item to check
 * @returns True if name input should be disabled
 */
const isNameInputDisabled = (item) => {
  if (props.disabled) {
    return true;
  }
  if (isItemDisabled(item)) {
    return true;
  }
  if (item.pid === -1) {
    return props.pType !== SchemaType.object;
  }
  return dataSourceObj.value[item.pid as string] && dataSourceObj.value[item.pid as string].type === SchemaType.array;
};

/**
 * Check if delete button should be hidden for an item
 * @param item - Item to check
 * @returns True if delete button should be hidden
 */
const shouldHideDeleteButton = (item) => {
  return isItemDisabled(item) && dataSourceObj.value[item.pid as string]?.type !== SchemaType.array && item.pid !== -1;
};

const dataSourceObj = ref({});

const dataSource = ref<Item[]>([]);

const allArrFirstIds = computed(() => {
  const result: (string | number)[] = [];
  if (props.pType === SchemaType.array) {
    result.push(-1);
  }
  const other = dataSource.value.filter(i => {
    if (i.type === SchemaType.array) {
      if ((dataSourceObj.value[i.pid as string] === -1 && props.pType !== SchemaType.array) ||
        (dataSourceObj.value[i.pid as string] && dataSourceObj.value[i.pid as string].type !== SchemaType.array)) {
        return true;
      }
      if (dataSource.value.find(t => t.pid === i.pid)?.id === i.id) {
        return true;
      }
    }
    return false;
  }).map(i => i.id);
  return [...result, ...other].map(pid => {
    const first = dataSource.value.find(i => i.pid === pid);
    return first?.id;
  }).filter(Boolean);
});

/**
 * Check if an item should be disabled
 * @param item - Item to check
 * @returns True if item should be disabled
 */
const isItemDisabled = (item) => {
  if (item.$ref) {
    return true;
  }
  if (item.idLine.some((id, idx) => dataSourceObj.value[id]?.$ref && idx < (item.idLine.length - 1))) {
    return true;
  }
  if (item.pid === -1 && props.pType === SchemaType.array && !allArrFirstIds.value.includes(item.id)) {
    return true;
  }
  return dataSourceObj.value[item.pid as string]?.type === SchemaType.array && !allArrFirstIds.value.includes(item.id);
};

/**
 * Check if add button should be disabled for an item
 * @param item - Item to check
 * @returns True if add button should be disabled
 */
const isAddButtonDisabled = (item) => {
  return item.type === SchemaType.object && isItemDisabled(item);
};

/**
 * Handle data type change for an item
 * @param newType - New data type
 * @param item - Item to change type
 * @param index - Index of the item
 */
const handleDataTypeChange = (newType, item, index) => {
  if (newType === SchemaType.array || newType === SchemaType.object) {
    item[valueKey] = undefined;
  }

  // Find child items to remove
  const childItems = newType === SchemaType.array
    ? dataSource.value.filter(i => (i.idLine.includes(item.pid as string | number) || item.pid === -1) && item.id !== i.id)
    : dataSource.value.filter(i => i.idLine.includes(item.id) && item.id !== i.id);

  // Remove child items from data source
  dataSource.value.splice(index + 1, childItems.length);
  dataSourceObj.value[item.id].type = newType;

  // Clean up child items from data source object
  childItems.forEach((childItem, childIndex) => childIndex > 0 && delete dataSourceObj.value[childItem.id]);

  emitDataChange();

  // Add new child item if type is array or object
  if (newType === SchemaType.array || newType === SchemaType.object) {
    addItem(item, index);
  }
};

/**
 * Add a new child item to the data source
 * @param parentItem - Parent item to add child to
 * @param index - Index where to insert the new item
 */
const addItem = (parentItem, index = dataSource.value.length) => {
  const existingChildren = parentItem.level < 1
    ? dataSource.value.filter(item => item.level === 1)
    : dataSource.value.filter(item => item.idLine.includes(parentItem.id as string | number) && item.id !== parentItem.id);

  const defaultType = SchemaType.string;
  const newItemId = utils.uuid('api');

  if (parentItem.type === SchemaType.array) {
    if (existingChildren.length > 0) {
      // Clone existing array items
      const firstChildId = existingChildren[0].id;
      let allChildItems = dataSource.value.filter(i => i.idLine.includes(firstChildId as string | number));
      allChildItems = JSON.parse(JSON.stringify(allChildItems));

      // Generate new IDs for cloned items
      const newIds = allChildItems.map(() => utils.uuid('api'));
      const oldIds = allChildItems.map(i => i.id);

      // Update IDs and references in cloned items
      allChildItems.forEach((item, itemIndex) => {
        item[valueKey] = '';
        if (item.id === oldIds[itemIndex]) {
          item.id = newIds[itemIndex];
        }
        oldIds.forEach((oldId, oldIndex) => {
          const replaceIndex = item.idLine.findIndex(id => id === oldId);
          if (replaceIndex > -1) {
            item.idLine[replaceIndex] = newIds[oldIndex];
          }
          if (item.pid === oldId) {
            item.pid = newIds[oldIndex];
          }
        });
        dataSourceObj.value[item.id] = item;
      });

      dataSource.value.splice(index + existingChildren.length + 1, 0, ...allChildItems);
    } else {
      // Create new array item based on schema
      if (parentItem.items) {
        const sampleJson = SwaggerUI.extension.sampleFromSchemaGeneric(parentItem, { useValue: true });
        const childSchema = parentItem.items;
        const newItems = transJsonToList(sampleJson, parentItem.id, parentItem.level + 1, [parentItem], childSchema);
        dataSource.value.splice(index + existingChildren.length, 1, ...newItems);
        newItems.forEach(item => {
          dataSourceObj.value[item.id] = item;
        });
      } else {
        // Create default array item
        const newItem: Item = {
          name: 'item',
          pid: parentItem.id,
          type: defaultType,
          id: newItemId,
          idLine: [...parentItem.idLine, newItemId],
          checked: true,
          level: parentItem.level + 1
        };
        dataSource.value.splice(index + existingChildren.length + 1, 0, newItem);
        dataSourceObj.value[newItemId] = newItem;
      }
    }
  } else if (parentItem.type === SchemaType.object) {
    // Create new object property
    const newItem: Item = {
      name: '',
      pid: parentItem.id,
      type: defaultType,
      id: newItemId,
      idLine: [...parentItem.idLine, newItemId],
      checked: true,
      level: parentItem.level + 1
    };
    dataSource.value.splice(index + existingChildren.length + 1, 0, newItem);
    dataSourceObj.value[newItemId] = newItem;
  }
};

/**
 * Delete an item and all its children
 * @param item - Item to delete
 * @param index - Index of the item to delete
 */
const deleteItem = (item, index) => {
  const itemsToDelete = dataSource.value.filter(i => i.idLine.includes(item.id));
  dataSource.value.splice(index, itemsToDelete.length);

  // Clean up deleted items from data source object
  itemsToDelete.forEach(deletedItem => {
    delete dataSourceObj.value[deletedItem.id];
  });

  emitDataChange();
};

/**
 * Fetch model data by reference
 * @param ref - Model reference
 * @returns Deconstructed model data
 */
const fetchModelData = async (ref) => {
  const [error, { data }] = await getModelDataByRef(apiBaseInfo.value.serviceId, ref);
  if (error) {
    return {};
  }
  return deconstruct(data || {});
};

/**
 * Handle model selection from schema dropdown
 * @param selectedValue - Selected value
 * @param option - Selected option with model data
 * @param index - Index of the item
 * @param item - Item to update
 */
const handleModelSelection = async (selectedValue, option, index, item) => {
  if (option) {
    const model = await fetchModelData(option.ref);
    const sampleValue = SwaggerUI.extension.sampleFromSchemaGeneric(model, { useValue: true });

    // Update item with model data
    dataSource.value[index].type = model.type;
    dataSource.value[index] = {
      ...dataSource.value[index],
      ...model
    };

    // Set reference if option is readonly
    if (option.readonly) {
      dataSource.value[index].$ref = option.ref;
    }

    // Generate child items based on model schema
    const childSchema = model.type === SchemaType.object ? model.properties : model.type === SchemaType.array ? model.items : {};
    const childItems = transJsonToList(sampleValue, item.id, item.level + 1, [dataSource.value[index]], childSchema);
    dataSource.value.splice(index, 1, ...childItems);

    // Update data source object with new child items
    childItems.forEach(childItem => {
      dataSourceObj.value[childItem.id] = childItem;
    });

    emitDataChange();
  } else {
    // Update item name if no model selected
    dataSource.value[index].name = selectedValue;
  }
};

/**
 * Handle value blur event for parameter values
 * @param dom - HTML element that lost focus
 * @param item - Item to update
 */
const handleValueBlur = (dom, item) => {
  let value = dom.innerText;

  // Parse numeric and boolean values from string
  if ([SchemaType.integer, SchemaType.number, SchemaType.boolean].includes(item.schema?.type)) {
    try {
      const numericValue = Number(value);
      if (!isNaN(numericValue) && numericValue <= 9007199254740992) {
        value = JSON.parse(value);
      }
    } catch {
      // Keep original value if parsing fails
    }
  }

  dataSourceObj.value[item.id][valueKey] = value;
  emitDataChange();
};

/**
 * Copy parameter value to clipboard
 * @param data - Parameter data to copy
 */
const copyParameterValue = async (data: ParamsInfo) => {
  let textToCopy = data[valueKey];
  if (typeof textToCopy !== 'string') {
    textToCopy = JSON.stringify(textToCopy);
  }

  toClipboard(textToCopy).then(() => {
    notification.success(t('actions.tips.copySuccess'));
  });
};

// Track validation state
const isValidationEnabled = ref(false);

/**
 * Enable or disable validation
 * @param enable - Whether to enable validation
 */
const enableValidation = (enable = true) => {
  isValidationEnabled.value = enable;
};

/**
 * Validate a value against its schema
 * @param value - Value to validate
 * @param schema - Schema to validate against
 * @returns True if validation fails
 */
const hasValidationError = (value, schema) => {
  if (!isValidationEnabled.value) {
    return false;
  }

  let cleanSchema = JSON.parse(JSON.stringify(schema));
  cleanSchema = deepDelAttrFromObj(cleanSchema, ['name', 'id', 'level', 'pid', 'idLine', 'checked']);

  const validator = ajv.compile(cleanSchema);
  const isValid = validator(value);
  return !isValid;
};

/**
 * Resolve model references for all items
 * @param models - Models object to populate
 */
const resolveModelReferences = (models) => {
  dataSource.value.forEach(item => {
    if (item.$ref) {
      const childItems = dataSource.value.filter(childItem => childItem.idLine.includes(item.id as string | number));
      models[item.$ref] = transListToSchema(childItems, item.type, item.pid as number);
      delete models[item.$ref].schema?.$ref;
    }
  });
};

/**
 * Update components with current data
 */
const updateComponents = async () => {
  const itemsWithRefs = dataSource.value.filter(item => item.$ref);
  for (const item of itemsWithRefs) {
    const schema = transListToSchema(item, item.type, item.pid as number);
    await services.addComponent(apiBaseInfo.value.serviceId, 'schema', item.name as string, schema);
  }
};

watch(() => props.pType, () => {
  const childrenSchema = props.pType === SchemaType.array ? (props.schema || {}) : (props.schema.properties || {});
  dataSource.value = transJsonToList(props.data, -1, 1, [], childrenSchema, props.schema);
  dataSource.value.forEach(i => {
    dataSourceObj.value[i.id] = i;
  });
}, {
  immediate: true
});

// Expose methods to parent component
defineExpose({
  addItem,
  validate: enableValidation,
  getModelResolve: resolveModelReferences,
  updateComp: updateComponents
});
</script>
<template>
  <div
    v-for="(item, idx) in dataSource"
    :key="item.id"
    :data-id="item.id"
    :p-id="item.pid"
    class="flex items-center space-x-2">
    <Checkbox
      v-model:checked="item.checked"
      class="invisible" />
    <div :style="{paddingLeft: item.level * 10 + 'px'}" class="w-100">
      <SelectSchema
        v-if="apiBaseInfo?.serviceId"
        :id="apiBaseInfo?.serviceId"
        v-model:value="item.name"
        mode="pure"
        :placeholder="t('service.apiRequestBody.placeholder.inputParameterName')"
        :maxLength="API_PARAMETER_NAME_LENGTH"
        :type="['schemas']"
        :disabled="item.$ref || isNameInputDisabled(item)"
        @blur="emitDataChange"
        @change="(_value, option) => handleModelSelection(_value, option, idx, item)" />
      <Input
        v-else
        v-model:value="item.name"
        :placeholder="t('service.apiRequestBody.placeholder.inputParameterName')"
        :maxLength="API_PARAMETER_NAME_LENGTH"
        :disabled="isNameInputDisabled(item)"
        @blur="emitDataChange" />
    </div>
    <Select
      v-if="props.paramInType"
      :value="props.paramInType"
      class="w-25"
      :options="QueryAndPathInOption"
      :disabled="true" />
    <Select
      v-model:value="item.type"
      class="w-25"
      :options="schemaTypeToOption"
      :disabled="isTypeSelectionDisabled(item)"
      @change="handleDataTypeChange($event, item, idx)" />
    <Input
      v-if="isValueInputDisabled(item)"
      disabled
      class="flex-1" />
    <SimpleEditableSelect
      v-else-if="item.enum?.length"
      v-model:value="item[valueKey]"
      class="flex-1"
      :maxLength="API_PARAMETER_VALUE_LENGTH"
      :options="item.enum"
      @change="emitDataChange"
      @select="emitDataChange" />
    <ParamInput
      v-else
      class="flex-1"
      :maxLength="API_PARAMETER_VALUE_LENGTH"
      :value="item[valueKey]"
      :error="hasValidationError(item[valueKey], item)"
      @blur="handleValueBlur($event, item)" />
    <Button
      type="primary"
      size="small"
      :title="t('service.apiRequestBody.actions.copyValue')"
      class="ml-2"
      @click="copyParameterValue(item as any)">
      <Icon icon="icon-fuzhi" />
    </Button>
    <Button
      size="small"
      :disabled="(item.type !== SchemaType.array && item.type !== SchemaType.object || isAddButtonDisabled(item))"
      @click="addItem(item, idx)">
      <Icon icon="icon-jia" />
    </Button>
    <Button
      size="small"
      :disabled="shouldHideDeleteButton(item)"
      @click="deleteItem(item, idx)">
      <Icon icon="icon-shanchuguanbi" />
    </Button>
  </div>
</template>
