<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Composite, Icon, Input } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

// ==================== Props & Emits ====================
interface Props {
  value?: { [key: string]: string; }[];
  fielaNames?: { label: string; value: string; };
  label?: string;
  notify?: number;
  valueRequired?:boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: () => [],
  fielaNames: () => ({ label: 'name', value: 'x-xc-value' }),
  label: undefined,
  notify: 0,
  valueRequired: false
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', value: string[]): void;
}>();

// ==================== Reactive State ====================
const idList = ref<string[]>([]);
const dataMap = ref<{ [key: string]: { name: string; value: string; } }>({});
const nameErrorSet = ref<Set<string>>(new Set<string>());
const valueErrorSet = ref<Set<string>>(new Set<string>());

// ==================== Computed Properties ====================
/**
 * Get the label key from field names configuration
 */
const labelKey = computed(() => props.fielaNames.label);

/**
 * Get the value key from field names configuration
 */
const valueKey = computed(() => props.fielaNames.value);

// ==================== Methods ====================
/**
 * Handle name field change event
 * @param event - Input change event
 * @param id - Field ID
 */
const handleNameChange = (event: { target: { value: string } }, id: string) => {
  nameErrorSet.value.delete(id);
  dataMap.value[id].name = event.target.value;
};

/**
 * Handle value field change event
 * @param event - Input change event
 * @param id - Field ID
 */
const handleValueChange = (event: { target: { value: string } }, id: string) => {
  valueErrorSet.value.delete(id);
  dataMap.value[id].value = event.target.value;
};

/**
 * Handle field deletion
 * @param index - Field index
 * @param id - Field ID
 */
const handleFieldDelete = (index: number, id: string) => {
  idList.value.splice(index, 1);
  delete dataMap.value[id];
  nameErrorSet.value.delete(id);
  valueErrorSet.value.delete(id);
};

/**
 * Reset all form data and error states
 */
const resetFormData = () => {
  idList.value = [];
  dataMap.value = {};
  nameErrorSet.value.clear();
  valueErrorSet.value.clear();
};

/**
 * Handle field creation at specific index
 * @param index - Field index
 */
const handleFieldCreate = (index: number) => {
  if (index < (idList.value.length - 1)) {
    return;
  }

  addNewField();
};

/**
 * Add a new field to the form
 */
const addNewField = () => {
  const id = utils.uuid();
  idList.value.push(id);
  dataMap.value[id] = {
    name: '',
    value: ''
  };
};

/**
 * Validate all form fields
 * @returns Whether all fields are valid
 */
const isValid = (): boolean => {
  nameErrorSet.value.clear();
  valueErrorSet.value.clear();
  const ids = idList.value;
  const map = dataMap.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    if (!map[id].name) {
      nameErrorSet.value.add(id);
    }

    if (props.valueRequired) {
      if (!map[id].value) {
        valueErrorSet.value.add(id);
      }
    }
  }

  return !nameErrorSet.value.size && !valueErrorSet.value.size;
};

/**
 * Get all form data
 * @returns Array of form field data
 */
const getData = (): { [key: string]: string }[] => {
  const _labelKey = labelKey.value;
  const _valueKey = valueKey.value;
  const map = dataMap.value;
  return idList.value.map(item => {
    const data = map[item];

    return {
      [_labelKey]: data.name,
      [_valueKey]: data.value
    };
  });
};

// ==================== Watchers ====================
onMounted(() => {
  watch(() => props.notify, () => {
    resetFormData();
  });

  watch(() => props.value, (newValue) => {
    resetFormData();
    if (!newValue?.length) {
      return;
    }

    for (let i = 0, len = newValue.length; i < len; i++) {
      const id = utils.uuid();
      idList.value.push(id);
      dataMap.value[id] = {
        name: newValue[i][labelKey.value],
        value: newValue[i][valueKey.value]
      };
    }
  }, { immediate: true });

  watch(() => idList.value, (newValue) => {
    emit('change', newValue);
  }, { immediate: true, deep: true });
});

// ==================== Expose Methods ====================
defineExpose({
  getData,
  isValid,
  add: addNewField,
  clear: resetFormData
});
</script>
<template>
  <div v-if="!!idList.length" class="leading-5">
    <div v-if="props.label" class="flex items-center mt-4 mb-0.5">{{ props.label }}</div>

    <div class="space-y-2">
      <div
        v-for="(item, index) in idList"
        :key="item"
        class="flex items-center space-x-2">
        <Composite>
          <Input
            :value="dataMap[item].name"
            :maxlength="400"
            :error="nameErrorSet.has(item)"
            style="flex: 1 1 40%;"
            trim
            :placeholder="t('mock.detail.apis.components.pureFormInput.parameterNamePlaceholder')"
            @change="handleNameChange($event, item)" />
          <Input
            :value="dataMap[item].value"
            :maxlength="4096"
            :error="valueErrorSet.has(item)"
            trim
            style="flex: 1 1 60%;"
            :placeholder="t('mock.detail.apis.components.pureFormInput.parameterValuePlaceholder')"
            @change="handleValueChange($event, item)" />
        </Composite>
        <div class="flex-shrink-0 space-x-1">
          <Button type="text" size="small">
            <Icon
              icon="icon-qingchu"
              class="text-3.5"
              @click="handleFieldDelete(index, item)" />
          </Button>
          <Button
            :class="{ invisible: index < (idList.length - 1) }"
            type="text"
            size="small"
            @click="handleFieldCreate(index)">
            <Icon icon="icon-jia" class="text-3.5" />
          </Button>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped>
.ant-btn {
  padding: 0;
}

.ant-btn:hover {
  color: var(--content-special-text-hover);
}
</style>
