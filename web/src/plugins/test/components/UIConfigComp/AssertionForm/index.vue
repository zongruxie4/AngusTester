<script setup lang="ts">
import { ref, onMounted, computed, watchEffect } from 'vue';
import { useI18n } from 'vue-i18n';
import { Checkbox } from 'ant-design-vue';
import { Arrow, Icon, Input, SelectEnum, Select, Tooltip } from '@xcan-angus/vue-ui';
import { BasicAssertionType, AssertionCondition, enumUtils, utils } from '@xcan-angus/infra';

import { AssertionConfig } from './PropsType';
import ExpectedPopover from './ExpectedPopover.vue';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Component props interface
 */
export interface Props {
  value: AssertionConfig[];  // Array of assertion configurations
  errorNum?: number;         // Number of validation errors (v-model)
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  errorNum: 0
});

/**
 * Event emitters
 */
const emit = defineEmits<{
  (e: 'change', value: AssertionConfig[]): void;      // Emit data changes
  (e: 'update:errorNum', value: number): void;        // Update error count
}>();

/**
 * Data Management
 * Using idList for order and dataMap for data storage
 */
const idList = ref<string[]>([]);                     // Ordered list of assertion IDs
const dataMap = ref<{ [key: string]: AssertionConfig }>({});  // Map of ID to assertion data

/**
 * Condition options for different assertion types
 * Different types support different conditions (e.g., BODY_SIZE only supports number conditions)
 */
const conditionOptions = ref<{
  'BODY': { message: string; value: string; }[];
  'BODY_SIZE': { message: string; value: string; }[];
  'DURATION': { message: string; value: string; }[];
}>({ BODY: [], BODY_SIZE: [], DURATION: [] });

/**
 * Validation Error Sets
 * Track which assertions have validation errors
 */
const nameErrorSet = ref<Set<string>>(new Set());         // Name field errors
const typeErrorSet = ref<Set<string>>(new Set());         // Type field errors
const conditionErrorSet = ref<Set<string>>(new Set());    // Condition field errors
const expressionErrorSet = ref<Set<string>>(new Set());   // Expression field errors
const expectedErrorSet = ref<Set<string>>(new Set());     // Expected value field errors

/**
 * UI State Sets
 */
const unfoldSet = ref<Set<string>>(new Set());            // Expanded assertion IDs
const checkedSet = ref<Set<string>>(new Set());           // Enabled assertion IDs
const repeatNameSet = ref<Set<string>>(new Set());        // Duplicate name IDs

/**
 * Constants for assertion validation and condition filtering
 */
const EXPRESSION_CONDITIONS = ['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'];
const EXPECTED_DISABLED = ['IS_EMPTY', 'NOT_EMPTY', 'IS_NULL', 'NOT_NULL'];
const NUMBER_CONDITIONS = ['EQUAL', 'NOT_EQUAL', 'GREATER_THAN', 'GREATER_THAN_EQUAL', 'LESS_THAN', 'LESS_THAN_EQUAL'];

/**
 * Load condition options based on assertion conditions enum
 * Filters number-only conditions for BODY_SIZE and DURATION types
 */
const loadConditionOptions = (): void => {
  const data = enumUtils.enumToMessages(AssertionCondition);
  const numberConditions = data.filter(item => NUMBER_CONDITIONS.includes(item.value));
  conditionOptions.value = {
    BODY: data,                    // BODY supports all conditions
    BODY_SIZE: numberConditions,   // BODY_SIZE only supports number comparisons
    DURATION: numberConditions     // DURATION only supports number comparisons
  };
};

/**
 * Add a new empty assertion item
 * Always adds to the end and automatically expands it
 */
const addNewItem = (): void => {
  const id = utils.uuid();
  unfoldSet.value.add(id);  // Auto-expand new item
  idList.value.push(id);
  dataMap.value[id] = {
    assertionCondition: undefined as any,
    description: '',
    enabled: false,
    expected: '',
    expression: '',
    matchItem: '',
    name: '',
    type: undefined as any
  };
};

/**
 * Delete an assertion item
 * If deleting the last item, automatically adds a new empty one
 * 
 * @param index - Index in the list
 * @param id - Assertion ID
 */
const delHandler = (index: number, id: string): void => {
  const length = idList.value.length - 1;
  idList.value.splice(index, 1);
  delete dataMap.value[id];
  
  // Clean up all state related to this item
  nameErrorSet.value.delete(id);
  typeErrorSet.value.delete(id);
  conditionErrorSet.value.delete(id);
  expressionErrorSet.value.delete(id);
  expectedErrorSet.value.delete(id);
  unfoldSet.value.delete(id);
  checkedSet.value.delete(id);
  repeatNameSet.value.delete(id);
  
  // If deleting the last item, auto-add a new empty one
  if (index === length) {
    addNewItem();
  }
};

/**
 * Handle expand/collapse state change
 * 
 * @param value - New expanded state
 * @param id - Assertion ID
 */
const unfoldChange = (value: boolean, id: string): void => {
  if (value) {
    unfoldSet.value.add(id);
    return;
  }

  unfoldSet.value.delete(id);
};

/**
 * Handle checkbox (enable) state change
 * Tracks that this assertion has been interacted with
 * 
 * @param id - Assertion ID
 */
const checkboxChange = (id: string): void => {
  checkedSet.value.add(id);
};

/**
 * Handle name field change
 * - Updates name value
 * - Auto-enables the assertion on first edit
 * - Checks for duplicate names across all assertions
 * - Auto-adds new empty item if editing the last one
 * 
 * @param event - Input change event
 * @param id - Assertion ID
 * @param index - Assertion index in list
 */
const nameChange = (event: { target: { value: string; } }, id: string, index: number): void => {
  const value = event.target.value;
  dataMap.value[id].name = value;
  nameErrorSet.value.delete(id);

  // Auto-enable assertion when name is entered
  if (!checkedSet.value.has(id)) {
    dataMap.value[id].enabled = true;
    checkedSet.value.add(id);
  }

  // Check for duplicate names
  const duplicates: string[] = [];
  const uniqueNames = new Set<string>();
  const names = Object.values(dataMap.value).map(item => item.name).filter(item => item);
  
  for (let i = 0, len = names.length; i < len; i++) {
    const name = names[i];
    if (uniqueNames.has(name)) {
      duplicates.push(name);
    } else {
      uniqueNames.add(name);
    }
  }

  // Mark all assertions with duplicate names as errors
  const ids = idList.value;
  const data = dataMap.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const _id = ids[i];
    if (duplicates.includes(data[_id].name)) {
      nameErrorSet.value.add(_id);
      repeatNameSet.value.add(_id);
    } else {
      nameErrorSet.value.delete(_id);
      repeatNameSet.value.delete(_id);
    }
  }

  // Ensure last item is always empty (auto-add new one if user fills the last)
  if (index === idList.value.length - 1) {
    if (value) {
      addNewItem();
    }
  }
};

/**
 * Handle assertion type change
 * Resets condition if current condition is incompatible with new type
 * (e.g., BODY_SIZE only supports number conditions)
 * 
 * @param value - New assertion type
 * @param id - Assertion ID
 */
const typeChange = (value: BasicAssertionType, id: string): void => {
  dataMap.value[id].type = value;
  const condition = dataMap.value[id].assertionCondition;
  typeErrorSet.value.delete(id);
  
  // Reset condition if incompatible with new type
  if (!NUMBER_CONDITIONS.includes(condition)) {
    dataMap.value[id].assertionCondition = undefined as any;
    if (EXPRESSION_CONDITIONS.includes(condition)) {
      dataMap.value[id].expression = '';
      dataMap.value[id].expected = '';
      dataMap.value[id].matchItem = '';
    }
  }
};

/**
 * Handle assertion condition change
 * Resets related fields based on new condition type:
 * - Expression conditions: show expression and match item fields
 * - Empty/null conditions: hide expected value field
 * - Standard conditions: show expected value field
 * 
 * @param value - New assertion condition
 * @param id - Assertion ID
 */
const conditionChange = (value: AssertionCondition, id: string): void => {
  dataMap.value[id].assertionCondition = value;
  conditionErrorSet.value.delete(id);

  // Reset expression on condition change
  expressionErrorSet.value.delete(id);
  dataMap.value[id].expression = '';

  // For IS_EMPTY, NOT_EMPTY, IS_NULL, NOT_NULL: no expected value needed
  if (EXPECTED_DISABLED.includes(value)) {
    dataMap.value[id].expected = '';
    dataMap.value[id].expression = '';
    dataMap.value[id].matchItem = '';
    return;
  }

  // For non-expression conditions: clear expression fields
  if (!EXPRESSION_CONDITIONS.includes(value)) {
    dataMap.value[id].expression = '';
    dataMap.value[id].matchItem = '';
  }
};

/**
 * Handle expression field change (for regex, XPath, JSON path)
 * 
 * @param event - Input change event
 * @param id - Assertion ID
 */
const expressionChange = (event: { target: { value: string; } }, id: string): void => {
  const value = event.target.value;
  dataMap.value[id].expression = value;
  expressionErrorSet.value.delete(id);
};

/**
 * Handle match item index change (for expression-based assertions)
 * Specifies which match result to use (e.g., 0 for first match)
 * 
 * @param event - Input change event
 * @param id - Assertion ID
 */
const matchItemChange = (event: { target: { value: string; } }, id: string): void => {
  const value = event.target.value;
  dataMap.value[id].matchItem = value;
};

/**
 * Handle expected value field change
 * 
 * @param event - Input change event
 * @param id - Assertion ID
 */
const expectedChange = (event: { target: { value: string; } }, id: string): void => {
  const value = event.target.value;
  dataMap.value[id].expected = value;
  expectedErrorSet.value.delete(id);
};

/**
 * Handle description field change
 * 
 * @param event - Input change event
 * @param id - Assertion ID
 */
const descriptionChange = (event: { target: { value: string; } }, id: string): void => {
  const value = event.target.value;
  dataMap.value[id].description = value;
};

/**
 * Initialize form data from props
 * Converts prop data into internal state structure and adds empty item at end
 */
const initializedData = (): void => {
  const list = props.value;
  if (list?.length) {
    for (let i = 0, len = list.length; i < len; i++) {
      const id = utils.uuid();
      const { 
        assertionCondition, 
        description, 
        enabled, 
        expected, 
        expression, 
        matchItem, 
        name, 
        type 
      } = list[i];
      
      // Mark as checked and expanded by default
      checkedSet.value.add(id);
      unfoldSet.value.add(id);
      idList.value.push(id);
      dataMap.value[id] = {
        assertionCondition,
        description,
        enabled,
        expected,
        expression,
        matchItem,
        name,
        type
      };
    }
  }

  // Always add empty item at the end for easy addition
  addNewItem();
};

/**
 * Component mount lifecycle hook
 * Sets up condition options, initializes data, and watches for changes
 */
onMounted(() => {
  // Load condition options from enum
  loadConditionOptions();
  
  // Initialize form data from props
  initializedData();

  // Watch for data changes and emit to parent
  watchEffect(() => {
    const data = getData();
    emit('change', data);
  });

  // Watch for validation errors and emit count to parent
  watchEffect(() => {
    const size = nameErrorSet.value.size +
      typeErrorSet.value.size +
      conditionErrorSet.value.size +
      expressionErrorSet.value.size +
      expectedErrorSet.value.size;
    emit('update:errorNum', size);
  });
});

/**
 * Filter function for assertion type enum
 * Excludes STATUS, HEADER, and SIZE types
 * 
 * @param param - Enum item with value property
 * @returns true if should be excluded
 */
const excludes = ({ value }: { value: string }): boolean => {
  return ['STATUS', 'HEADER', 'SIZE'].includes(value);
};

/**
 * Computed style map for expand/collapse animation
 * Returns style object for each assertion based on expanded state
 */
const styleMap = computed(() => {
  const set = unfoldSet.value;
  return idList.value.reduce((prev, cur) => {
    if (set.has(cur)) {
      // Expanded state
      prev[cur] = {
        height: 'auto',
        marginTop: '8px',
        opacity: '100'
      };
    } else {
      // Collapsed state
      prev[cur] = {
        height: '0',
        marginTop: '0',
        opacity: '0'
      };
    }

    return prev;
  }, {} as Record<string, any>);
});

/**
 * Placeholder text for expression fields
 */
const PLACEHOLDER = {
  REG_MATCH: t('jdbcPlugin.UIConfigJdbc.assertionForm.expressionMap.REG_MATCH'),
  XPATH_MATCH: t('jdbcPlugin.UIConfigJdbc.assertionForm.expressionMap.XPATH_MATCH'),
  JSON_PATH_MATCH: t('jdbcPlugin.UIConfigJdbc.assertionForm.expressionMap.JSON_PATH_MATCH')
};

/**
 * Get assertion data for submission
 * Excludes the last empty item
 * 
 * @returns Array of assertion configurations
 */
const getData = (): AssertionConfig[] => {
  const ids = idList.value;
  const map = dataMap.value;
  const data: AssertionConfig[] = [];
  
  // Skip last item (always empty)
  for (let i = 0, len = ids.length - 1; i < len; i++) {
    data.push({ ...map[ids[i]] });
  }

  return data;
};

/**
 * Validate all assertions
 * Clears previous errors and re-validates all non-empty assertions
 * Checks for:
 * - Required fields (name, type, condition)
 * - Duplicate names
 * - Expected value (when required by condition)
 * - Expression (for regex/XPath/JSON path conditions)
 * 
 * @returns Total number of validation errors
 */
const isValid = (): number => {
  // Clear previous validation errors
  nameErrorSet.value.clear();
  typeErrorSet.value.clear();
  conditionErrorSet.value.clear();
  expressionErrorSet.value.clear();
  expectedErrorSet.value.clear();
  repeatNameSet.value.clear();

  // Check for duplicate names
  const duplicates: string[] = [];
  const uniqueNames = new Set<string>();
  const names = Object.values(dataMap.value).map(item => item.name).filter(item => item);
  
  for (let i = 0, len = names.length; i < len; i++) {
    const name = names[i];
    if (uniqueNames.has(name)) {
      duplicates.push(name);
    } else {
      uniqueNames.add(name);
    }
  }

  const ids = idList.value;
  const data = dataMap.value;
  
  // Validate each assertion (skip last empty item)
  for (let i = 0, len = ids.length - 1; i < len; i++) {
    const id = ids[i];
    const {
      assertionCondition,
      expected,
      expression,
      name,
      type
    } = data[id];

    // Validate name (required)
    if (!name) {
      nameErrorSet.value.add(id);
    } else {
      // Check for duplicate name
      if (duplicates.includes(name)) {
        nameErrorSet.value.add(id);
        repeatNameSet.value.add(id);
      }
    }

    // Validate type (required)
    if (!type) {
      typeErrorSet.value.add(id);
    }

    // Validate condition (required)
    if (!assertionCondition) {
      conditionErrorSet.value.add(id);
    }

    // Validate expected value (required for most conditions)
    if (!EXPECTED_DISABLED.includes(assertionCondition) && !EXPRESSION_CONDITIONS.includes(assertionCondition)) {
      if (!expected) {
        expectedErrorSet.value.add(id);
      }
    }

    // Validate expression (required for expression-based conditions)
    if (EXPRESSION_CONDITIONS.includes(assertionCondition)) {
      if (!expression) {
        expressionErrorSet.value.add(id);
      }
    }
  }

  // Return total error count
  return nameErrorSet.value.size +
    typeErrorSet.value.size +
    conditionErrorSet.value.size +
    expressionErrorSet.value.size +
    expectedErrorSet.value.size;
};

/**
 * Expose methods for parent component access
 */
defineExpose({
  isValid,    // Validate all assertions
  getData     // Get assertion data for submission
});
</script>

<template>
  <!-- Assertion list container -->
  <div class="space-y-4">
    <!-- Individual assertion item -->
    <div
      v-for="(item, index) in idList"
      :key="item"
      :class="{ 'opacity-70': !dataMap[item].enabled }"
      class="flex items-start">
      <!-- Enable/disable checkbox -->
      <div class="flex items-center h-7 mr-2">
        <Checkbox 
          v-model:checked="dataMap[item].enabled" 
          @change="checkboxChange(item)" />
      </div>
      
      <!-- Main form fields container -->
      <div class="flex-1 space-y-2 mr-3">
        <!-- Primary row: Name, Type, Condition, Expression/Expected -->
        <div class="flex items-start space-x-2">
          <Tooltip
            :title="t('jdbcPlugin.UIConfigJdbc.assertionForm.nameDuplicate')"
            internal
            placement="right"
            destroyTooltipOnHide
            :visible="repeatNameSet.has(item)">
            <Input
              :placeholder="t('jdbcPlugin.UIConfigJdbc.assertionForm.assertionNamePlaceholder')"
              :title="t('jdbcPlugin.UIConfigJdbc.assertionForm.assertionName')"
              trim
              style="flex: 1 1 calc((100% - 32px)/7*2);"
              :maxlength="200"
              :value="dataMap[item].name"
              :error="nameErrorSet.has(item)"
              @change="nameChange($event, item,index)" />
          </Tooltip>
          <SelectEnum
            style="flex: 0 0 calc((100% - 32px)/7);"
            enumKey="BasicAssertionType"
            :placeholder="t('jdbcPlugin.UIConfigJdbc.assertionForm.assertionTypePlaceholder')"
            :title="t('jdbcPlugin.UIConfigJdbc.assertionForm.assertionType')"
            :error="typeErrorSet.has(item)"
            :value="dataMap[item].type"
            :excludes="excludes"
            @change="typeChange($event, item)" />
          <Select
            style="flex: 0 0 calc((100% - 32px)/7);"
            :placeholder="t('jdbcPlugin.UIConfigJdbc.assertionForm.assertionConditionPlaceholder')"
            :title="t('jdbcPlugin.UIConfigJdbc.assertionForm.assertionCondition')"
            class="flex-1"
            :disabled="!dataMap[item].type"
            :error="conditionErrorSet.has(item)"
            :value="dataMap[item].assertionCondition"
            :fieldNames="{ label: 'message', value: 'value' }"
            :options="conditionOptions[dataMap[item].type]"
            @change="conditionChange($event, item)" />
          <template v-if="EXPRESSION_CONDITIONS.includes(dataMap[item].assertionCondition)">
            <Tooltip
              :title="t('jdbcPlugin.UIConfigJdbc.assertionForm.expressionFormatError')"
              placement="top"
              destroyTooltipOnHide
              :visible="expressionErrorSet.has(item)">
              <Input
                trim
                style="flex: 0 0 calc((100% - 32px)/7*2);"
                :maxlength="400"
                :placeholder="PLACEHOLDER[dataMap[item].assertionCondition]"
                :value="dataMap[item].expression"
                :error="expressionErrorSet.has(item)"
                @change="expressionChange($event, item)" />
            </Tooltip>
            <Input
              :placeholder="t('jdbcPlugin.UIConfigJdbc.assertionForm.matchItemPlaceholder')"
              :title="t('jdbcPlugin.UIConfigJdbc.assertionForm.matchItem')"
              trim
              dataType="number"
              style="flex: 0 0 calc((100% - 32px)/7);"
              :value="dataMap[item].matchItem"
              :max="2000"
              :maxlength="4"
              @change="matchItemChange($event, item)" />
          </template>
          <template v-else>
            <Input
              :placeholder="t('jdbcPlugin.UIConfigJdbc.assertionForm.expectedValuePlaceholder')"
              :title="t('jdbcPlugin.UIConfigJdbc.assertionForm.expectedValue')"
              style="flex: 0 0 calc((100% - 32px)/7*2);"
              trim
              :disabled="EXPECTED_DISABLED.includes(dataMap[item].assertionCondition)"
              :value="dataMap[item].expected"
              :error="expectedErrorSet.has(item)"
              @change="expectedChange($event, item)" />
          </template>
        </div>

        <div :style="styleMap[item]" class="transition-all space-y-2 overflow-hidden">
          <div
            v-if="EXPRESSION_CONDITIONS.includes(dataMap[item].assertionCondition)"
            class="flex items-center space-x-2">
            <Input
              class="flex-1"
              :placeholder="t('jdbcPlugin.UIConfigJdbc.assertionForm.expectedValueOptional')"
              :title="t('jdbcPlugin.UIConfigJdbc.assertionForm.expectedValue')"
              trim
              :value="dataMap[item].expected"
              :error="expectedErrorSet.has(item)"
              @change="expectedChange($event, item)" />
            <ExpectedPopover class="flex-shrink-0" />
          </div>
          <Input
            :placeholder="t('jdbcPlugin.UIConfigJdbc.assertionForm.descriptionPlaceholder')"
            :title="t('common.description')"
            type="textarea"
            trim
            :value="dataMap[item].description"
            @change="descriptionChange($event, item)" />
        </div>
      </div>

      <div class="flex items-center h-7 space-x-2">
        <Icon
          class="cursor-pointer text-theme-sub-content hover:text-text-link-hover"
          icon="icon-shanchuguanbi"
          @click="delHandler(index, item)" />
        <Arrow
          class="hover:text-text-link-hover"
          :open="unfoldSet.has(item)"
          @change="unfoldChange($event, item)" />
      </div>
    </div>
  </div>
</template>

<style scoped>
/**
 * Force white background for all enabled form controls
 * Overrides theme defaults to ensure consistent appearance
 */
.ant-input-affix-wrapper:not(.ant-input-affix-wrapper-disabled),
:deep(.ant-checkbox),
.ant-input:not(.ant-input-disabled),
.ant-select:not(.ant-select-disabled) :deep(.ant-select-selector) {
  background-color: #fff;
}
</style>
