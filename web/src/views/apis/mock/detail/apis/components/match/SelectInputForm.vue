<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Composite, Icon, Input, SelectInput, Validate } from '@xcan-angus/vue-ui';
import { ParameterIn, FullMatchCondition, utils } from '@xcan-angus/infra';

import SelectEnum from '@/components/enum/SelectEnum.vue';

/**
 * <p>Parameter type definition for matching conditions</p>
 * <p>Defines the structure of parameter objects with matching conditions</p>
 */
type Parameter = {
  condition: FullMatchCondition;
  expected: string | undefined;
  expression: string | undefined;
  in: ParameterIn.header | ParameterIn.query;
  name: string;
}

/**
 * <p>Props interface for SelectInputForm component</p>
 * <p>Defines the structure of props passed to the component</p>
 */
interface Props {
  in: ParameterIn.header | ParameterIn.query;
  value: Parameter[];
  notify: number;
}

const props = withDefaults(defineProps<Props>(), {
  in: ParameterIn.header,
  value: () => [],
  notify: 0
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
 * <p>Stores parameter data indexed by ID with additional value field</p>
 */
const dataMap = ref<{ [key: string]: Parameter & { value: string | undefined } }>({});

/**
 * <p>Set of parameter IDs with name validation errors</p>
 * <p>Used to track which parameters have invalid names</p>
 */
const nameErrorSet = ref<Set<string>>(new Set());

/**
 * <p>Set of parameter IDs with value validation errors</p>
 * <p>Used to track which parameters have invalid values</p>
 */
const valueErrorSet = ref<Set<string>>(new Set());

/**
 * <p>Map of error messages for parameters</p>
 * <p>Stores specific error messages for each parameter</p>
 */
const errorMessageMap = ref<Map<string, string>>(new Map());

// ==================== Constants ====================
/**
 * <p>Enum values that require expression input</p>
 * <p>These conditions use expression field instead of expected field</p>
 */
const EXPRESSION_ENUMS = [FullMatchCondition.REG_MATCH, FullMatchCondition.XPATH_MATCH, FullMatchCondition.JSON_PATH_MATCH];

// ==================== Event Handlers ====================
/**
 * <p>Handles parameter name change event</p>
 * <p>Updates parameter name and clears validation errors</p>
 *
 * @param value - New parameter name
 * @param id - Parameter ID
 */
const handleNameChange = (value: string, id: string) => {
  dataMap.value[id].name = value;
  nameErrorSet.value.delete(id);
};

/**
 * <p>Handles parameter name search</p>
 * <p>Filters options based on search value</p>
 *
 * @param value - Search value
 * @param options - Available options
 * @returns Filtered options
 */
const handleNameSearch = (value: string, options: { _value: string; message: string }[]) => {
  return options.filter(item => item._value.includes(value) || item.message.includes(value));
};

/**
 * <p>Handles parameter condition change event</p>
 * <p>Updates parameter condition and clears value for empty conditions</p>
 *
 * @param value - New condition value
 * @param id - Parameter ID
 */
const handleConditionChange = (value: FullMatchCondition, id: string) => {
  dataMap.value[id].condition = value;
  if ([FullMatchCondition.IS_EMPTY, FullMatchCondition.IS_NULL, FullMatchCondition.NOT_EMPTY,
    FullMatchCondition.NOT_NULL].includes(value)) {
    dataMap.value[id].value = undefined;
    valueErrorSet.value.delete(id);
  }
};

/**
 * <p>Handles parameter value change event</p>
 * <p>Updates parameter value and clears validation errors</p>
 *
 * @param event - Input change event
 * @param id - Parameter ID
 */
const handleValueChange = (event: { target: { value: string } }, id: string) => {
  const value = event.target.value;
  dataMap.value[id].value = value;
  valueErrorSet.value.delete(id);
  errorMessageMap.value.delete(id);
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
  errorMessageMap.value.delete(id);
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
  errorMessageMap.value.clear();
};

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
    condition: FullMatchCondition.EQUAL,
    expected: undefined,
    expression: undefined,
    in: props.in,
    name: '',
    value: undefined
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
  errorMessageMap.value.clear();
  if (!idList.value.length) {
    return true;
  }

  const ids = idList.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const { name, condition, value } = dataMap.value[id];
    if (!name) {
      nameErrorSet.value.add(id);
    }

    if (![FullMatchCondition.IS_EMPTY, FullMatchCondition.IS_NULL, FullMatchCondition.NOT_EMPTY,
      FullMatchCondition.NOT_NULL].includes(condition)) {
      if (!value) {
        valueErrorSet.value.add(id);
      }
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
const getData = (): Parameter[] | undefined => {
  if (!idList.value.length) {
    return undefined;
  }

  const map = dataMap.value;
  const _in = props.in;
  return idList.value.map(item => {
    const data = map[item];
    const _data: Parameter = {
      in: _in,
      name: data.name,
      condition: data.condition,
      expected: undefined,
      expression: undefined
    };

    if (EXPRESSION_ENUMS.includes(data.condition)) {
      _data.expression = data.value;
    } else {
      _data.expected = data.value;
    }

    return _data;
  });
};

// ==================== Lifecycle Hooks ====================
/**
 * <p>Component mounted lifecycle hook</p>
 * <p>Sets up watchers and initializes component state</p>
 */
onMounted(() => {
  watch(() => props.notify, () => {
    resetComponent();
  });

  watch(() => props.value, (newValue) => {
    resetComponent();
    if (!newValue?.length) {
      return;
    }

    for (let i = 0, len = newValue.length; i < len; i++) {
      const data = newValue[i];
      const id = utils.uuid();
      idList.value.push(id);
      dataMap.value[id] = {
        ...data,
        value: undefined
      };

      if (EXPRESSION_ENUMS.includes(data.condition)) {
        dataMap.value[id].value = data.expression;
      } else {
        dataMap.value[id].value = data.expected;
      }
    }
  }, { immediate: true });

  watch(() => idList.value, (newValue) => {
    emit('change', newValue);
  }, { immediate: true, deep: true });
});

// ==================== Computed Properties ====================
/**
 * <p>Placeholder text map for parameter values</p>
 * <p>Returns appropriate placeholder based on condition type</p>
 */
const placeholderMap = computed(() => {
  const ids = idList.value;
  const data = dataMap.value;
  const emptyConditions = [FullMatchCondition.IS_EMPTY, FullMatchCondition.IS_NULL,
    FullMatchCondition.NOT_EMPTY, FullMatchCondition.NOT_NULL];
  const map: { [key: string]: string } = {};
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const condition = data[id].condition;
    if (emptyConditions.includes(condition)) {
      map[id] = '';
    } else if (condition === FullMatchCondition.REG_MATCH) {
      map[id] = t('mock.detail.apis.components.match.regexExpression');
    } else {
      map[id] = t('mock.detail.apis.components.match.parameterValuePlaceholder');
    }
  }

  return map;
});

/**
 * <p>Disabled state map for parameter values</p>
 * <p>Returns true for conditions that don't require input</p>
 */
const disabledMap = computed(() => {
  const ids = idList.value;
  const data = dataMap.value;
  const disabledConditions = [FullMatchCondition.IS_EMPTY, FullMatchCondition.IS_NULL,
    FullMatchCondition.NOT_EMPTY, FullMatchCondition.NOT_NULL];
  const map: { [key: string]: boolean } = {};
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const condition = data[id].condition;
    if (disabledConditions.includes(condition)) {
      map[id] = true;
    }
  }

  return map;
});

// ==================== Configuration ====================
/**
 * <p>Excludes function for condition selection</p>
 * <p>Filters out conditions that are not supported</p>
 */
const excludes = ({ value }: { value: any }) => {
  return [FullMatchCondition.XPATH_MATCH, FullMatchCondition.JSON_PATH_MATCH].includes(value);
};

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

// ==================== Public API ====================
/**
 * <p>Exposes component methods and data to parent components</p>
 * <p>Provides methods for managing parameters and validation</p>
 */
defineExpose({
  getData,
  isValid,
  add: addParameter
});
</script>
<template>
  <div v-if="!!idList.length" class="leading-5">
    <div class="flex items-center mb-0.5">{{ t('mock.detail.apis.components.match.header') }}</div>
    <div class="space-y-2">
      <div
        v-for="(item,index) in idList"
        :key="item"
        class="flex items-center space-x-2">
        <Composite>
          <SelectInput
            :value="dataMap[item].name"
            :inputProps="inputProps"
            :fieldNames="fieldNames"
            :maxlength="400"
            :error="nameErrorSet.has(item)"
            :search="handleNameSearch as any"
            showSearch
            enumKey="HttpRequestHeader"
            mode="combination"
            style="flex: 0 0 calc((100% - 200px) * 2/5);"
            trim
            :placeholder="t('mock.detail.apis.components.match.parameterNamePlaceholder')"
            @change="handleNameChange($event,item)">
            <template #option="record">
              <div class="truncate" :title="record._value+'-'+record.message">{{ record._value }} - {{ record.message }}</div>
            </template>
          </SelectInput>
          <SelectEnum
            :value="dataMap[item].condition"
            :excludes="excludes"
            enumKey="FullMatchCondition"
            class="flex-shrink-0 w-48"
            :placeholder="t('mock.detail.apis.components.match.operatorPlaceholder')"
            @change="(value: any) => handleConditionChange(value, item)" />
          <Validate
            class="flex-1"
            :fixed="true"
            :text="errorMessageMap[item]">
            <Input
              :disabled="disabledMap[item]"
              :value="dataMap[item].value"
              :maxlength="4096"
              :error="valueErrorSet.has(item)"
              :placeholder="placeholderMap[item]"
              trim
              @change="handleValueChange($event,item)" />
          </Validate>
        </Composite>
        <div class="flex-shrink-0 space-x-1">
          <Button type="text" size="small">
            <Icon
              icon="icon-qingchu"
              class="text-3.5"
              @click="handleDelete(index,item)" />
          </Button>
          <Button
            :class="{invisible:index<(idList.length-1)}"
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
.ant-btn{
  padding: 0;
}

.ant-btn:hover{
  color: var(--content-special-text-hover);
}
</style>
