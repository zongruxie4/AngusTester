<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Composite, Icon, Input, notification, Popover } from '@xcan-angus/vue-ui';

import SelectEnum from '@/components/enum/SelectEnum.vue';
import { StringMatchCondition, utils } from '@xcan-angus/infra';

/**
 * <p>Path information type definition</p>
 * <p>Defines the structure of path matching conditions</p>
 */
type PathInfo = {
  condition: StringMatchCondition;
  expected: string | undefined;
  expression: string | undefined
}

/**
 * <p>Props interface for PathForm component</p>
 * <p>Defines the structure of props passed to the component</p>
 */
interface Props {
  value: { [key: string]: any };
  notify: number;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  notify: 0
});

const { t } = useI18n();

// ==================== Reactive State ====================
/**
 * <p>List of path IDs for tracking</p>
 * <p>Used to manage form state (only one path allowed)</p>
 */
const idList = ref<string[]>([]);

/**
 * <p>Selected matching condition</p>
 * <p>Determines how the path will be matched</p>
 */
const selectValue = ref<StringMatchCondition>(StringMatchCondition.EQUAL);

/**
 * <p>Input value for path matching</p>
 * <p>Contains the actual path or expression to match</p>
 */
const inputValue = ref<string>();

/**
 * <p>Error flag for validation</p>
 * <p>Indicates if there are validation errors</p>
 */
const errorFlag = ref(false);

// ==================== Constants ====================
/**
 * <p>Supported string match conditions</p>
 * <p>Defines which conditions are available for path matching</p>
 */
const ENUMS: readonly StringMatchCondition[] = [
  StringMatchCondition.EQUAL,
  StringMatchCondition.NOT_EQUAL,
  StringMatchCondition.CONTAIN,
  StringMatchCondition.NOT_CONTAIN,
  StringMatchCondition.REG_MATCH
];

// ==================== Event Handlers ====================
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

// ==================== Utility Methods ====================
/**
 * <p>Resets component to initial state</p>
 * <p>Clears all form data and error states</p>
 */
const resetComponent = () => {
  idList.value = [];
  selectValue.value = StringMatchCondition.EQUAL;
  inputValue.value = undefined;
  errorFlag.value = false;
};

/**
 * <p>Adds a new path matching condition</p>
 * <p>Only allows one path condition at a time</p>
 */
const addPath = () => {
  if (idList.value.length) {
    notification.info(t('mock.detail.apis.components.match.onlyOnePath'));
    return;
  }

  idList.value = [utils.uuid()];
  selectValue.value = StringMatchCondition.EQUAL;
  inputValue.value = undefined;
  errorFlag.value = false;
};

// ==================== Validation Methods ====================
/**
 * <p>Validates path format (placeholder for future implementation)</p>
 * <p>Currently only checks if path is not empty</p>
 *
 * @param _pathname - Path to validate
 * @returns true if path is valid, false otherwise
 */
const isValidPath = (_pathname: string): boolean => {
  if (!_pathname) {
    return false;
  }

  return true;
};

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

  const value = inputValue.value;
  if (!value) {
    errorFlag.value = true;
    return false;
  }

  if (selectValue.value === StringMatchCondition.REG_MATCH) {
    return true;
  }

  if (!isValidPath(value)) {
    errorFlag.value = true;
    return false;
  }

  return true;
};

// ==================== Data Methods ====================
/**
 * <p>Gets current form data</p>
 * <p>Returns formatted path matching data</p>
 *
 * @returns PathInfo object with current configuration
 */
const getData = (): PathInfo | undefined => {
  if (!idList.value.length) {
    return undefined;
  }

  const condition = selectValue.value;
  const value = inputValue.value;
  const data: PathInfo = { condition, expected: undefined, expression: undefined };
  if (condition === StringMatchCondition.REG_MATCH) {
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
    selectValue.value = condition;
    if (condition === StringMatchCondition.REG_MATCH) {
      inputValue.value = expression;
    } else {
      inputValue.value = expected;
    }

    idList.value = [utils.uuid()];
  }, { immediate: true });
});

// ==================== Configuration ====================
/**
 * <p>Excludes function for condition selection</p>
 * <p>Filters out conditions that are not supported</p>
 */
const excludes = ({ value }: { value: any }) => {
  return !ENUMS.includes(value);
};

/**
 * <p>Placeholder text map for different conditions</p>
 * <p>Returns appropriate placeholder based on condition type</p>
 */
const placeholderMap = {
  REG_MATCH: t('mock.detail.apis.components.match.regexPlaceholder'),
  EQUAL: t('mock.detail.apis.components.match.pathPlaceholder'),
  NOT_EQUAL: t('mock.detail.apis.components.match.pathPlaceholder'),
  CONTAIN: t('mock.detail.apis.components.match.pathPlaceholder'),
  NOT_CONTAIN: t('mock.detail.apis.components.match.pathPlaceholder')
};

// ==================== Public API ====================
/**
 * <p>Exposes component methods and data to parent components</p>
 * <p>Provides methods for managing path matching and validation</p>
 */
defineExpose({
  getData,
  isValid,
  add: addPath
});
</script>
<template>
  <div v-if="idList.length" class="leading-5">
    <div class="flex items-center">
      <span class="mr-1 mb-0.5">{{ t('common.path') }}</span>
      <Popover destroyTooltipOnHide>
        <template #content>
          <ul style="max-width: 520px;" class="pl-4 list-disc whitespace-pre-line break-all space-y-1">
            <li>{{ t('mock.detail.apis.components.match.pathTooltip') }}</li>
          </ul>
        </template>
        <Icon icon="icon-shuoming" class="text-tips cursor-pointer text-3.5" />
      </Popover>
    </div>
    <div class="flex items-center space-x-2">
      <Composite>
        <SelectEnum
          v-model:value="selectValue"
          :excludes="excludes"
          enumKey="FullMatchCondition"
          class="w-48"
          :placeholder="t('mock.detail.apis.components.match.operatorPlaceholder')" />
        <Input
          v-model:value="inputValue"
          :maxlength="4096"
          :placeholder="placeholderMap[selectValue]"
          :error="errorFlag"
          trim
          class="flex-1"
          @change="handleInputChange" />
      </Composite>
      <div class="flex-shrink-0 space-x-1">
        <Button type="text" size="small">
          <Icon
            icon="icon-qingchu"
            class="text-3.5"
            @click="handleDelete" />
        </Button>
        <Button
          class="invisible"
          type="text"
          size="small">
          <Icon icon="icon-jia" class="text-3.5" />
        </Button>
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
