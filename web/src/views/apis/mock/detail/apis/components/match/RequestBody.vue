<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Input, notification } from '@xcan-angus/vue-ui';

import SelectEnum from '@/components/enum/SelectEnum.vue';
import { ResponseMatchConfig } from './types';
import { FullMatchCondition, utils } from '@xcan-angus/infra';

/**
 * <p>Props interface for RequestBody component</p>
 * <p>Defines the structure of props passed to the component</p>
 */
interface Props {
  value: ResponseMatchConfig['body'];
  notify?: number;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  notify: 0
});

const { t } = useI18n();

// ==================== Async Components ====================
const CodeEditor = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/CodeEditor.vue'));

// ==================== Template Refs ====================
const editorRef = ref();

// ==================== Reactive State ====================
/**
 * <p>List of request body IDs for tracking</p>
 * <p>Used to manage form state (only one request body allowed)</p>
 */
const idList = ref<string[]>([]);

/**
 * <p>Input value for request body matching</p>
 * <p>Contains the actual content or expression to match</p>
 */
const inputValue = ref<string>();

/**
 * <p>Selected matching condition</p>
 * <p>Determines how the request body will be matched</p>
 */
const selectValue = ref<FullMatchCondition>(FullMatchCondition.EQUAL);

/**
 * <p>Error flag for validation</p>
 * <p>Indicates if there are validation errors</p>
 */
const errorFlag = ref(false);

// ==================== Event Handlers ====================
/**
 * <p>Handles condition change event</p>
 * <p>Clears error flag and resets input value</p>
 */
const handleConditionChange = () => {
  errorFlag.value = false;
  inputValue.value = undefined;
};

/**
 * <p>Handles input value change event</p>
 * <p>Clears error flag when user starts typing</p>
 */
const handleInputChange = () => {
  errorFlag.value = false;
};

/**
 * <p>Handles delete button click</p>
 * <p>Resets the form to initial state</p>
 */
const handleDelete = () => {
  resetComponent();
};

/**
 * <p>Handles format button click</p>
 * <p>Formats the code editor content</p>
 */
const handleFormat = () => {
  if (typeof editorRef.value?.format === 'function') {
    editorRef.value.format();
  }
};

/**
 * <p>Handles clear button click</p>
 * <p>Clears the input value and code editor</p>
 */
const handleClear = () => {
  inputValue.value = '';
  if (typeof editorRef.value?.clear === 'function') {
    editorRef.value.clear();
  }
};

// ==================== Utility Methods ====================
/**
 * <p>Resets component to initial state</p>
 * <p>Clears all form data and error states</p>
 */
const resetComponent = () => {
  idList.value = [];
  inputValue.value = undefined;
  selectValue.value = FullMatchCondition.EQUAL;
  errorFlag.value = false;
  if (typeof editorRef.value?.clear === 'function') {
    editorRef.value.clear();
  }
};

/**
 * <p>Adds a new request body matching condition</p>
 * <p>Only allows one request body condition at a time</p>
 */
const addRequestBody = () => {
  if (idList.value.length) {
    notification.info(t('mock.detail.apis.components.match.onlyOneRequestBody'));
    return;
  }

  idList.value = [utils.uuid()];
  inputValue.value = undefined;
  selectValue.value = FullMatchCondition.EQUAL;
  errorFlag.value = false;
};

// ==================== Validation Methods ====================
/**
 * <p>Validates the current form state</p>
 * <p>Checks if all required fields are filled and valid</p>
 *
 * @returns true if form is valid, false otherwise
 */
const isValid = (): boolean => {
  errorFlag.value = false;
  if (!idList.value.length) {
    return true;
  }

  if (!isNoInput.value && showEditor.value) {
    if (typeof editorRef.value?.isValid === 'function') {
      errorFlag.value = !editorRef.value.isValid();
    }
    return !errorFlag.value;
  }

  if (!inputValue.value) {
    errorFlag.value = true;
    return false;
  }

  return true;
};

// ==================== Data Methods ====================
/**
 * <p>Gets current form data</p>
 * <p>Returns formatted request body matching data</p>
 *
 * @returns ResponseMatchConfig body object with current configuration
 */
const getData = (): ResponseMatchConfig['body'] | undefined => {
  if (!idList.value.length) {
    return undefined;
  }

  const condition = selectValue.value;
  const data: ResponseMatchConfig['body'] = { condition, expected: undefined, expression: undefined };
  let value = inputValue.value;
  if (!isNoInput.value && showEditor.value) {
    if (typeof editorRef.value?.getData === 'function') {
      value = editorRef.value.getData();
    }
  }

  if ([FullMatchCondition.REG_MATCH, FullMatchCondition.XPATH_MATCH, FullMatchCondition.JSON_PATH_MATCH].includes(condition)) {
    data.expression = value;
  } else {
    data.expected = value;
  }
  return data;
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
    if (!newValue) {
      return;
    }

    const { condition, expected, expression } = newValue;
    let value: string | undefined;
    if ([FullMatchCondition.EQUAL, FullMatchCondition.NOT_EQUAL, FullMatchCondition.CONTAIN, FullMatchCondition.NOT_CONTAIN].includes(condition)) {
      value = expected;
    } else if ([FullMatchCondition.REG_MATCH, FullMatchCondition.XPATH_MATCH, FullMatchCondition.JSON_PATH_MATCH].includes(condition)) {
      value = expression;
    }

    idList.value = [utils.uuid()];
    selectValue.value = condition;

    // @TODO 编辑器内容改变没有同步到父级，父级数据一直是原始数据，刷新时由于父级数据没有变化，所以编辑器的watch不会触发
    setTimeout(() => {
      inputValue.value = value;
    }, 0);
  }, { immediate: true });
});

// ==================== Computed Properties ====================
/**
 * <p>Determines if input is not required</p>
 * <p>Returns true for conditions that don't need input values</p>
 */
const isNoInput = computed(() => {
  const condition = selectValue.value;
  if (!condition) {
    return false;
  }
  return [FullMatchCondition.IS_EMPTY, FullMatchCondition.NOT_EMPTY,
    FullMatchCondition.IS_NULL, FullMatchCondition.NOT_NULL].includes(condition);
});

/**
 * <p>Determines if code editor should be shown</p>
 * <p>Returns true for conditions that require content editing</p>
 */
const showEditor = computed(() => {
  const condition = selectValue.value;
  if (!condition) {
    return false;
  }
  return [FullMatchCondition.EQUAL, FullMatchCondition.NOT_EQUAL,
    FullMatchCondition.CONTAIN, FullMatchCondition.NOT_CONTAIN].includes(condition);
});

// ==================== Configuration ====================
/**
 * <p>Placeholder text map for different conditions</p>
 * <p>Returns appropriate placeholder based on condition type</p>
 */
const placeholderMap = {
  GREATER_THAN: t('mock.detail.apis.components.match.value'),
  GREATER_THAN_EQUAL: t('mock.detail.apis.components.match.value'),
  LESS_THAN: t('mock.detail.apis.components.match.value'),
  LESS_THAN_EQUAL: t('mock.detail.apis.components.match.value'),
  REG_MATCH: t('mock.detail.apis.components.match.regexExpression'),
  XPATH_MATCH: t('mock.detail.apis.components.match.xpathExpression'),
  JSON_PATH_MATCH: t('mock.detail.apis.components.match.jsonPathExpression')
};

// ==================== Public API ====================
/**
 * <p>Exposes component methods and data to parent components</p>
 * <p>Provides methods for managing request body matching and validation</p>
 */
defineExpose({
  getData,
  isValid,
  add: addRequestBody
});
</script>
<template>
  <div v-if="!!idList.length" class="leading-5">
    <div class="flex items-center mb-0.5">{{ t('mock.detail.apis.components.match.requestBody') }}</div>
    <div class="flex items-center justify-between space-x-2">
      <SelectEnum
        v-model:value="selectValue"
        class="w-48.5"
        enumKey="FullMatchCondition"
        @change="handleConditionChange" />
      <div v-if="!isNoInput" class="flex-1 flex items-center justify-end space-x-2">
        <Button
          style="padding: 0;"
          type="link"
          size="small"
          @click="handleDelete">
          <span>{{ t('actions.delete') }}</span>
        </Button>
        <template v-if="showEditor">
          <Button
            style="padding: 0;"
            type="link"
            size="small"
            @click="handleFormat">
            <span>{{ t('mock.detail.apis.components.match.format') }}</span>
          </Button>
          <Button
            style="padding: 0;"
            type="link"
            size="small"
            @click="handleClear">
            <span>{{ t('actions.clear') }}</span>
          </Button>
        </template>
      </div>
    </div>
    <template v-if="!isNoInput">
      <template v-if="showEditor">
        <CodeEditor
          ref="editorRef"
          v-model:value="inputValue"
          :showAction="false"
          class="mt-2" />
      </template>
      <template v-else>
        <Input
          v-model:value="inputValue"
          :maxlength="4096"
          :error="errorFlag"
          :placeholder="placeholderMap[selectValue]"
          trim
          class="w-full mt-2"
          @change="handleInputChange" />
      </template>
    </template>
  </div>
</template>
