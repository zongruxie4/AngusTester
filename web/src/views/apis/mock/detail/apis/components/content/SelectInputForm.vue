<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Composite, Icon, Input, SelectInput } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

/**
 * <p>Parameter type definition</p>
 * <p>Defines the structure of parameter objects</p>
 */
type ParametersType = { name: string; value: string; disabled: boolean }

/**
 * <p>Props interface for SelectInputForm component</p>
 * <p>Defines the structure of props passed to the component</p>
 */
interface Props {
  enumKey: string;
  value: ParametersType[];
  fielaNames?: { label: string; value: string; };
  label?: string;
}

const props = withDefaults(defineProps<Props>(), {
  enumKey: undefined,
  value: () => [],
  fielaNames: () => ({ label: 'name', value: 'x-xc-value' }),
  label: undefined
});

const { t } = useI18n();

/**
 * <p>Component events interface</p>
 * <p>Defines the events that this component can emit</p>
 */
const emit = defineEmits<{
  (e: 'change', value: string[]): void;
}>();

// ==================== Reactive State ====================
/**
 * <p>List of parameter IDs for tracking</p>
 * <p>Used to manage form state and order</p>
 */
const idList = ref<string[]>([]);

/**
 * <p>Data map for parameter values</p>
 * <p>Stores parameter data indexed by ID</p>
 */
const dataMap = ref<{ [key: string]: ParametersType }>({});

/**
 * <p>Set of parameter IDs with name validation errors</p>
 * <p>Used to track which parameters have invalid names</p>
 */
const nameErrorSet = ref<Set<string>>(new Set<string>());

/**
 * <p>Set of parameter IDs with value validation errors</p>
 * <p>Used to track which parameters have invalid values</p>
 */
const valueErrorSet = ref<Set<string>>(new Set<string>());

// ==================== Computed Properties ====================
/**
 * <p>Label key for field mapping</p>
 * <p>Returns the label field name from props</p>
 */
const labelKey = computed(() => props.fielaNames.label);

/**
 * <p>Value key for field mapping</p>
 * <p>Returns the value field name from props</p>
 */
const valueKey = computed(() => props.fielaNames.value);

// ==================== Event Handlers ====================
/**
 * <p>Handles parameter name change event</p>
 * <p>Updates parameter name and clears validation errors</p>
 *
 * @param value - New parameter name
 * @param id - Parameter ID
 */
const handleNameChange = (value: string, id: string) => {
  nameErrorSet.value.delete(id);
  dataMap.value[id].name = value;
};

/**
 * <p>Handles parameter name search</p>
 * <p>Filters options based on search value</p>
 *
 * @param value - Search value
 * @param options - Available options
 * @returns Filtered options
 */
const handleNameSearch = (value: string, options: Record<string, any>[]) => {
  return options.filter(item => item._value?.includes(value) || item.message?.includes(value));
};

/**
 * <p>Handles parameter value change event</p>
 * <p>Updates parameter value and clears validation errors</p>
 *
 * @param event - Input change event
 * @param id - Parameter ID
 */
const handleValueChange = (event: { target: { value: string } }, id: string) => {
  valueErrorSet.value.delete(id);
  dataMap.value[id].value = event.target.value;
};

/**
 * <p>Handles parameter deletion</p>
 * <p>Removes parameter from form and clears related data</p>
 *
 * @param index - Parameter index
 * @param id - Parameter ID
 */
const handleDelete = (index: number, id: string) => {
  idList.value.splice(index, 1);
  delete dataMap.value[id];
  nameErrorSet.value.delete(id);
  valueErrorSet.value.delete(id);
};

// ==================== Utility Methods ====================
/**
 * <p>Resets component to initial state</p>
 * <p>Clears all form data and error states</p>
 */
const resetComponent = () => {
  idList.value = [];
  dataMap.value = {};
  nameErrorSet.value.clear();
  valueErrorSet.value.clear();
};

// ==================== Lifecycle Hooks ====================
/**
 * <p>Component mounted lifecycle hook</p>
 * <p>Sets up watchers and initializes component state</p>
 */
onMounted(() => {
  watch(() => props.value, (newValue) => {
    resetComponent();
    if (!newValue?.length) {
      return;
    }

    for (let i = 0, len = newValue.length; i < len; i++) {
      const id = utils.uuid();
      idList.value.push(id);
      const data = newValue[i];
      dataMap.value[id] = {
        disabled: data.disabled,
        name: data[labelKey.value],
        value: data[valueKey.value]
      };
    }
  }, { immediate: true });

  watch(() => idList.value, (newValue) => {
    emit('change', newValue);
  }, { immediate: true, deep: true });
});

// ==================== Form Management Methods ====================
/**
 * <p>Creates a new parameter field</p>
 * <p>Adds a new parameter if it's the last item in the list</p>
 *
 * @param index - Current parameter index
 */
const createParameter = (index: number) => {
  if (index < idList.value.length - 1) {
    return;
  }

  addParameter();
};

/**
 * <p>Adds a new parameter to the form</p>
 * <p>Creates a new parameter with default values</p>
 */
const addParameter = () => {
  const id = utils.uuid();
  idList.value.push(id);
  dataMap.value[id] = {
    disabled: false,
    name: '',
    value: ''
  };
};

// ==================== Validation Methods ====================
/**
 * <p>Validates all form fields</p>
 * <p>Checks if all required fields are filled</p>
 *
 * @returns true if form is valid, false otherwise
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

    if (!map[id].value) {
      valueErrorSet.value.add(id);
    }
  }

  return !nameErrorSet.value.size && !valueErrorSet.value.size;
};

// ==================== Data Methods ====================
/**
 * <p>Gets current form data</p>
 * <p>Returns formatted data from all parameters</p>
 *
 * @returns Array of parameter data objects
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

// ==================== Public API ====================
/**
 * <p>Exposes component methods and data to parent components</p>
 * <p>Provides methods for managing parameters and validation</p>
 */
defineExpose({
  /**
   * <p>Gets current form data</p>
   * <p>Returns formatted data from all parameters</p>
   *
   * @returns Array of parameter data objects
   */
  getData,

  /**
   * <p>Validates all form fields</p>
   * <p>Checks if all required fields are filled</p>
   *
   * @returns true if form is valid, false otherwise
   */
  isValid,

  /**
   * <p>Adds a parameter to the form</p>
   * <p>Can add with specific data or create a new empty parameter</p>
   *
   * @param data - Optional parameter data to add
   */
  add: (data?: { value: string; name: string; disabled: boolean, prevValue: string }) => {
    const id = utils.uuid();
    if (data) {
      const { name, value, disabled, prevValue } = data;
      const firstItemId = idList.value[0];
      if (firstItemId && dataMap.value[firstItemId]) {
        const firstItem = dataMap.value[firstItemId];
        if (firstItem.name === name && firstItem.value === prevValue) {
          dataMap.value[firstItemId].value = value;
          return;
        }
      }

      idList.value.unshift(id);
      dataMap.value[id] = {
        disabled,
        name,
        value
      };

      return;
    }

    idList.value.push(id);
    dataMap.value[id] = {
      disabled: false,
      name: '',
      value: ''
    };
  },

  /**
   * <p>Removes a parameter from the form</p>
   * <p>Finds and removes parameter by name and previous value</p>
   *
   * @param param - Parameter data to remove
   */
  del: ({ name, prevValue }: { prevValue: string, value: string; name: string; }) => {
    const ids = idList.value;
    for (let i = 0, len = ids.length; i < len; i++) {
      const id = ids[i];
      const data = dataMap.value[id];
      if (data.value === prevValue && name === data.name) {
        idList.value = idList.value.filter(item => item !== id);
        delete dataMap.value[id];
        break;
      }
    }
  },

  /**
   * <p>Clears all form data</p>
   * <p>Resets component to initial state</p>
   */
  clear: () => {
    resetComponent();
  }
});

// ==================== Configuration ====================
/**
 * <p>Field name mapping for select components</p>
 * <p>Defines how select options map to data properties</p>
 */
const fieldNames = { label: '_value', value: 'id' };

/**
 * <p>Input properties configuration</p>
 * <p>Common properties applied to input components</p>
 */
const inputProps = {
  maxlength: 400
};
</script>
<template>
  <div v-if="!!idList.length" class="leading-5">
    <div v-if="props.label" class="flex items-center mt-4 mb-0.5">{{ props.label }}</div>
    <div class="space-y-2">
      <div
        v-for="(item, index) in idList"
        :key="item"
        class="flex items-center space-x-2">
        <Composite :disabled="dataMap[item].disabled">
          <SelectInput
            :value="dataMap[item].name"
            :enumKey="props.enumKey"
            :inputProps="inputProps"
            :fieldNames="fieldNames"
            :error="nameErrorSet.has(item)"
            :disabled="dataMap[item].disabled"
            :search="handleNameSearch as any"
            showSearch
            mode="combination"
            style="flex: 1 1 40%;"
            trim
            :placeholder="t('mock.detail.apis.components.selectInputForm.parameterNamePlaceholder')"
            @change="handleNameChange($event, item)">
            <template #option="record">
              <div class="truncate" :title="record._value + '-' + record.message">
                {{ record._value }} - {{ record.message }}
              </div>
            </template>
          </SelectInput>
          <Input
            :value="dataMap[item].value"
            :maxlength="4096"
            :error="valueErrorSet.has(item)"
            :disabled="dataMap[item].disabled"
            trim
            style="flex: 1 1 60%;"
            :placeholder="t('mock.detail.apis.components.selectInputForm.parameterValuePlaceholder')"
            @change="handleValueChange($event, item)" />
        </Composite>
        <div class="flex-shrink-0 space-x-1">
          <Button
            :disabled="dataMap[item].disabled"
            type="text"
            size="small">
            <Icon
              icon="icon-qingchu"
              class="text-3.5"
              @click="handleDelete(index, item)" />
          </Button>
          <Button
            :class="{ invisible: index < (idList.length - 1) }"
            type="text"
            size="small"
            @click="createParameter(index)">
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

.ant-btn:not([disabled]):hover {
  color: var(--content-special-text-hover);
}
</style>
