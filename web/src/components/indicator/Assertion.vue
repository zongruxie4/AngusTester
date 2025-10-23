<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Switch } from 'ant-design-vue';
import { Input, SelectEnum, Select, Validate } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import jsonpath from '@/components/apis/assertion/utils/JsonPath';
import xpath from '@/components/apis/assertion/utils/XPath';
import regexp from '@/components/apis/assertion/utils/Regexp';
import { Extraction } from '@/components/apis/assertion/utils/types';
import { ApiAssertionFormItem } from '@/components/apis/assertion/PropsType';

import { EnumMessage, HttpExtractionLocation, AssertionCondition, AssertionType, enumUtils, ExtractionMethod } from '@xcan-angus/infra';

import MatchItemPopover from '@/components/apis/assertion/MacthItemPopover.vue';
import ExpectedPopover from '@/components/apis/assertion/ExpectedPopover.vue';

const { t } = useI18n();

/**
 * Component props definition
 */
interface Props {
  value: ApiAssertionFormItem;
  num?: number;
  viewType?: boolean; // Whether to display in read-only view mode
  vertical: boolean; // Whether to use vertical layout
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  num: undefined,
  viewType: false,
  vertical: true
});
/**
 * Generate default extraction object with all fields undefined
 */
const generateDefaultExtraction = (): Extraction => {
  return {
    defaultValue: undefined,
    expression: undefined,
    failureMessage: undefined,
    finalValue: undefined,
    location: undefined,
    matchItem: undefined,
    method: undefined,
    name: undefined,
    parameterName: undefined,
    value: undefined
  };
};

/**
 * Generate default assertion form data
 */
const generateDefaultData = (): ApiAssertionFormItem => {
  return {
    assertionCondition: undefined,
    condition: undefined,
    description: undefined,
    expression: undefined,
    matchItem: undefined,
    expected: undefined,
    extraction: generateDefaultExtraction(),
    parameterName: undefined,
    name: undefined,
    type: undefined,
    enabled: true
  };
};

// Template refs
const nameRefs = ref();
const extractionExpressionRefs = ref();

// State management
const idList = ref<string[]>([]);
const assertionFormData = ref<ApiAssertionFormItem>(generateDefaultData());

const isFocused = ref(false);
const shouldExtractValue = ref(false); // Whether to extract value for expected field
const isExtractionDisabled = ref(false); // Whether extraction toggle is disabled

// Validation error states
const hasTypeError = ref(false);
const hasHeaderNameError = ref(false);
const hasAssertionConditionError = ref(false);
const hasExpectedError = ref(false);

const hasExtractionExpressionError = ref(false);
const extractionExpressionErrorMsg = ref();

const hasLocationError = ref(false);
const hasMethodError = ref(false);
const hasParameterNameError = ref(false);

// Locations that don't require parameter name input
const LOCATIONS_WITHOUT_PARAMETER_NAME: readonly ['REQUEST_RAW_BODY', 'RESPONSE_BODY'] = ['REQUEST_RAW_BODY', 'RESPONSE_BODY'];

// Display expected value input only when assertion condition is not in this list
const shouldShowExpectedInput = ref(false);
const CONDITIONS_WITHOUT_EXPECTED = ['IS_NULL', 'IS_EMPTY', 'NOT_EMPTY', 'NOT_NULL'] as const;

// Show expression input only for regex, xpath, and jsonpath match conditions
const shouldShowExpressionInput = ref();
const hasExpressionError = ref(false);
const expressionErrorMsg = ref();

// Assertion condition options
const assertionConditionOptions = ref<EnumMessage<AssertionCondition>[]>([]);
const loadAssertionConditionOptions = async () => {
  assertionConditionOptions.value = enumUtils.enumToMessages(AssertionCondition);
};

// Conditions that only support numeric comparisons
const NUMERIC_CONDITIONS = ['EQUAL', 'NOT_EQUAL', 'GREATER_THAN', 'GREATER_THAN_EQUAL', 'LESS_THAN', 'LESS_THAN_EQUAL'];

/**
 * Filtered assertion condition options based on assertion type
 * Numeric types (like STATUS, SIZE, etc.) only show numeric comparison conditions
 */
const filteredAssertionConditions = computed(() => {
  const formData = assertionFormData.value;
  const type = formData.type;
  const options = assertionConditionOptions.value;
  
  if (type) {
    // BODY and HEADER support all conditions
    if (type === 'BODY' || type === 'HEADER') {
      return options;
    } else {
      // Other types only support numeric conditions
      return options.filter((item) => NUMERIC_CONDITIONS.includes(item.value));
    }
  } else {
    return options;
  }
});

// Extraction location options
const locationOptions = ref<EnumMessage<HttpExtractionLocation>[]>([]);
const loadLocationOptions = async () => {
  locationOptions.value = enumUtils.enumToMessages(HttpExtractionLocation);
};

/**
 * Filtered location options based on extraction method
 * EXACT_VALUE method excludes certain locations like RESPONSE_BODY, RESPONSE_HEADER, etc.
 */
const filteredLocationOptions = computed(() => {
  const formData = assertionFormData.value;
  const options = locationOptions.value;
  const method = formData.extraction.method;
  
  if (method && method === 'EXACT_VALUE') {
    return options.filter((item) => !['RESPONSE_BODY', 'RESPONSE_HEADER', 'REQUEST_RAW_BODY'].includes(item.value));
  } else {
    return options;
  }
});

/**
 * Handle extraction toggle switch change
 * When toggling between expected value and extraction mode
 */
const handleExtractionToggle = async (checked: boolean | number | string) => {
  hasExpectedError.value = false;
  hasParameterNameError.value = false;
  hasLocationError.value = false;
  hasMethodError.value = false;
  assertionFormData.value.expected = '';

  shouldExtractValue.value = Boolean(checked);
};

/**
 * Handle input focus event
 */
const handleInputFocus = () => {
  isFocused.value = true;
};

/**
 * Handle input blur event
 */
const handleInputBlur = () => {
  isFocused.value = false;
};

/**
 * Handle assertion type change
 * Updates form state and validation based on selected type
 */
const handleTypeChange = (value: AssertionType) => {
  hasTypeError.value = false;

  // Clear parameter name if not HEADER type
  if (value !== 'HEADER') {
    assertionFormData.value.parameterName = '';
    hasHeaderNameError.value = false;
  }

  const currentCondition = assertionFormData.value.assertionCondition || '';
  
  // STATUS type has special handling
  if (['STATUS'].includes(value)) {
    if (shouldShowExpressionInput.value) {
      shouldShowExpressionInput.value = false;
      assertionFormData.value.expression = undefined;
      assertionFormData.value.matchItem = undefined;
      assertionFormData.value.expected = undefined;
    }

    // Force numeric condition for STATUS type
    if (!NUMERIC_CONDITIONS.includes(currentCondition)) {
      assertionFormData.value.assertionCondition = 'EQUAL' as AssertionCondition;
      shouldShowExpectedInput.value = true;
    }

    // Disable extraction for STATUS type
    shouldExtractValue.value = false;
    isExtractionDisabled.value = true;
    return;
  }

  // Enable extraction if condition allows
  if (!['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(currentCondition)) {
    isExtractionDisabled.value = false;
  }
};

/**
 * Handle header name input change
 */
const handleHeaderNameChange = () => {
  hasHeaderNameError.value = false;
};

/**
 * Handle assertion condition change
 * Updates UI state based on selected condition
 */
const handleAssertionConditionChange = (value: typeof CONDITIONS_WITHOUT_EXPECTED[number]) => {
  hasAssertionConditionError.value = false;
  shouldShowExpressionInput.value = false;
  shouldShowExpectedInput.value = true;

  // Expression match conditions require special handling
  if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(value)) {
    shouldExtractValue.value = false;
    isExtractionDisabled.value = true;
    shouldShowExpressionInput.value = true;
    return;
  }

  // Conditions without expected value
  if (CONDITIONS_WITHOUT_EXPECTED.includes(value)) {
    isExtractionDisabled.value = true;
    shouldShowExpectedInput.value = false;
    return;
  }

  isExtractionDisabled.value = false;
};

/**
 * Handle expression input change
 */
const handleExpressionChange = () => {
  hasExpressionError.value = false;
  expressionErrorMsg.value = undefined;
};

/**
 * Handle extraction method change
 * Clears expression and validates location when method changes
 */
const handleMethodChange = () => {
  hasMethodError.value = false;
  
  // EXACT_VALUE method doesn't need expression
  if (assertionFormData.value.extraction.method === 'EXACT_VALUE') {
    assertionFormData.value.extraction.expression = '';
    hasExtractionExpressionError.value = false;
    extractionExpressionErrorMsg.value = undefined;

    // Clear incompatible locations
    const location = assertionFormData.value.extraction.location;
    if (location && ['RESPONSE_BODY', 'RESPONSE_HEADER'].includes(location)) {
      assertionFormData.value.extraction.location = undefined;
    }

    return;
  }

  // Validate existing expression if present
  const expression = assertionFormData.value.extraction.expression;
  if (expression) {
    validateExtractionExpression(expression);
  }
};

/**
 * Handle extraction location change
 */
const handleLocationChange = () => {
  hasLocationError.value = false;

  const location = assertionFormData.value.extraction.location || '';
  if (!location) {
    hasParameterNameError.value = false;
  }

  // Some locations don't require parameter name
  if (['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(location)) {
    assertionFormData.value.extraction.parameterName = undefined;
    hasParameterNameError.value = false;
  }
};

/**
 * Handle parameter name input change
 */
const handleParameterNameChange = () => {
  hasParameterNameError.value = false;
};

/**
 * Handle extraction expression input change
 */
const handleExtractionExpressionChange = () => {
  hasExtractionExpressionError.value = false;
  extractionExpressionErrorMsg.value = undefined;
};

/**
 * Handle extraction expression blur event
 * Validates the expression when user leaves the input
 */
const handleExtressionBlur = (event: { target: { value: string } }) => {
  handleInputBlur();

  const value = event.target.value?.trim();
  if (!value) {
    return;
  }

  validateExtractionExpression(value);
};

/**
 * Handle extraction expression enter key press
 */
const handleExtressionEnter = () => {
  if (!extractionExpressionRefs.value) {
    return;
  }

  extractionExpressionRefs.value?.blur?.();
};

/**
 * Handle expected value input change
 */
const handleExpectedChange = () => {
  hasExpectedError.value = false;
};

/**
 * Reset all error states
 */
const resetErrorStates = () => {
  hasTypeError.value = false;
  hasHeaderNameError.value = false;
  hasAssertionConditionError.value = false;
  hasExpectedError.value = false;

  hasExtractionExpressionError.value = false;
  extractionExpressionErrorMsg.value = undefined;

  hasExpressionError.value = false;
  expressionErrorMsg.value = undefined;

  hasParameterNameError.value = false;
  hasLocationError.value = false;
  hasMethodError.value = false;
};

/**
 * Reset all form data and states to initial values
 */
const resetForm = () => {
  idList.value = [];
  nameRefs.value = [];
  isFocused.value = false;
  shouldExtractValue.value = false;
  isExtractionDisabled.value = false;
  shouldShowExpectedInput.value = false;
  shouldShowExpressionInput.value = false;
  assertionFormData.value = { ...generateDefaultData() };

  resetErrorStates();
};

/**
 * Validate extraction expression based on extraction method
 * @param value - The expression string to validate
 * @returns true if valid, false otherwise
 */
const validateExtractionExpression = (value: string): boolean => {
  const method = assertionFormData.value.extraction.method;
  let validResult: { valid: boolean; message: string } = { valid: false, message: '' };
  
  // Validate based on extraction method
  if (method === 'JSON_PATH') {
    validResult = jsonpath.isValid(value);
  } else if (method === 'X_PATH') {
    validResult = xpath.isValid(value);
  } else {
    validResult = regexp.isValid(value);
  }

  if (validResult.valid) {
    hasExtractionExpressionError.value = false;
    extractionExpressionErrorMsg.value = undefined;
    return true;
  }

  hasExtractionExpressionError.value = true;
  extractionExpressionErrorMsg.value = validResult.message;
  return false;
};

/**
 * Validate assertion expression based on assertion condition
 * @param value - The expression string to validate
 * @returns true if valid, false otherwise
 */
const validateExpression = (value: string): boolean => {
  const condition = assertionFormData.value.assertionCondition;
  let validResult: { valid: boolean; message: string } = { valid: false, message: '' };
  
  // Validate based on assertion condition type
  if (condition === 'JSON_PATH_MATCH') {
    validResult = jsonpath.isValid(value);
  } else if (condition === 'XPATH_MATCH') {
    validResult = xpath.isValid(value);
  } else {
    validResult = regexp.isValid(value);
  }

  if (validResult.valid) {
    hasExpressionError.value = false;
    expressionErrorMsg.value = undefined;
    return true;
  }

  hasExpressionError.value = true;
  expressionErrorMsg.value = validResult.message;
  return false;
};

/**
 * Validate the entire assertion form
 * @returns true if validation passes, false otherwise
 */
const validateForm = (): boolean => {
  resetErrorStates();
  const formData = assertionFormData.value;
  
  // Validate assertion type
  if (!formData.type) {
    hasTypeError.value = true;
  } else {
    // Header type requires parameter name (header name)
    if (formData.type === 'HEADER') {
      if (!formData.parameterName) {
        hasHeaderNameError.value = true;
      }
    }
  }

  // Validate assertion condition
  if (!formData.assertionCondition) {
    hasAssertionConditionError.value = true;
  }

  // Validate expression for match conditions
  if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(formData.assertionCondition!)) {
    if (!formData.expression) {
      hasExpressionError.value = true;
    } else {
      validateExpression(formData.expression!);
    }
  } else if (shouldShowExpectedInput.value) {
    // Validate expected value or extraction
    if (!shouldExtractValue.value) {
      // Direct expected value
      if (!formData.expected) {
        hasExpectedError.value = true;
      }
    } else {
      // Extraction mode validation
      if (formData.extraction.method !== 'EXACT_VALUE') {
        if (!formData.extraction.expression) {
          hasExtractionExpressionError.value = true;
        } else {
          validateExtractionExpression(formData.extraction.expression!);
        }
      }

      // Validate parameter name (except for raw body locations)
      if (!['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(formData.extraction.location!)) {
        if (!formData.extraction.parameterName) {
          hasParameterNameError.value = true;
        }
      }

      // Validate location and method
      if (!formData.extraction.location) {
        hasLocationError.value = true;
      }

      if (!formData.extraction.method) {
        hasMethodError.value = true;
      }
    }
  }

  // Return true only if no errors exist
  return !(hasTypeError.value ||
    hasHeaderNameError.value ||
    hasAssertionConditionError.value ||
    hasExpressionError.value ||
    hasExpectedError.value ||
    hasExtractionExpressionError.value ||
    hasParameterNameError.value ||
    hasLocationError.value ||
    hasMethodError.value
  );
};

/**
 * Get form data for submission
 * Cleans up empty values and converts them to null
 * @returns Object containing the cleaned form data
 */
const getFormData = (): { data: ApiAssertionFormItem } => {
  const data = assertionFormData.value;
  const isExtracting = shouldExtractValue.value;

  // Deep clone to avoid mutating original data
  const cleanedData = JSON.parse(JSON.stringify(data));

  // Set expected to null for conditions that don't use it
  if (['IS_EMPTY', 'NOT_EMPTY', 'IS_NULL', 'NOT_NULL'].includes(cleanedData.assertionCondition) || 
      cleanedData.expected === '' || 
      cleanedData.expected === undefined) {
    cleanedData.expected = null;
  }

  // Handle extraction data
  if (!isExtracting) {
    cleanedData.extraction = null;
  } else {
    // Convert empty extraction fields to null
    for (const key in cleanedData.extraction) {
      if (cleanedData.extraction[key] === '' || cleanedData.extraction[key] === undefined) {
        cleanedData.extraction[key] = null;
      }
    }
  }

  // Convert empty parameter name to null
  if (cleanedData.parameterName === '' || cleanedData.parameterName === undefined) {
    cleanedData.parameterName = null;
  }

  return { data: cleanedData };
};

/**
 * Expose methods to parent component
 */
defineExpose({
  getData: getFormData,
  validate: validateForm
});

/**
 * Component lifecycle - mounted
 * Load enum options and set up watchers
 */
onMounted(() => {
  // Load assertion condition enum options
  loadAssertionConditionOptions();

  // Load extraction location enum options
  loadLocationOptions();

  /**
   * Watch for props.value changes and update form data accordingly
   */
  watch(() => props.value, async (newValue) => {
    resetForm();
    const data = newValue || {} as ApiAssertionFormItem;
    const extraction = data.extraction;
    
    // Extract assertion condition value (handle both enum and object formats)
    const assertionCondition = typeof data.assertionCondition === 'object' && data.assertionCondition !== null
      ? (data.assertionCondition as any).value
      : data.assertionCondition;

    // Extract type value (handle both enum and object formats)
    // Note: We store the raw enum value internally for easier comparisons
    const typeValue: any = typeof data.type === 'object' && data.type !== null
      ? (data.type as any).value
      : data.type;

    // Update form data with incoming props
    assertionFormData.value = {
      assertionCondition,
      condition: data.condition,
      description: data.description,
      expression: data.expression,
      matchItem: data.matchItem,
      expected: data.expected,
      extraction: data.extraction || generateDefaultExtraction(),
      parameterName: data.parameterName,
      name: data.name,
      type: typeValue,
      enabled: data.enabled !== undefined ? data.enabled : true
    };

    // Enable extraction mode if extraction data exists
    if (extraction) {
      shouldExtractValue.value = true;
    }
    
    // Show expected input unless condition is null/empty check
    if (!CONDITIONS_WITHOUT_EXPECTED.includes(assertionCondition)) {
      shouldShowExpectedInput.value = true;
      
      // Expression match conditions require special handling
      if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(assertionCondition)) {
        shouldExtractValue.value = false;
        shouldShowExpressionInput.value = true;
      }
    }

    // Disable extraction for certain conditions
    if (['IS_EMPTY', 'NOT_EMPTY', 'IS_NULL', 'NOT_NULL', 'REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(assertionCondition)) {
      isExtractionDisabled.value = true;
    }
  });
});

/**
 * Computed placeholder text for expression input based on assertion condition
 */
const expressionPlaceholder = computed(() => {
  const formData = assertionFormData.value;
  let placeholder = '';
  
  switch (formData.assertionCondition) {
    case 'REG_MATCH':
      placeholder = t('xcan_indicatorAssert.expressionPlaceholder.regexMatch');
      break;
    case 'XPATH_MATCH':
      placeholder = t('xcan_indicatorAssert.expressionPlaceholder.xpathMatch');
      break;
    case 'JSON_PATH_MATCH':
      placeholder = t('xcan_indicatorAssert.expressionPlaceholder.jsonPathMatch');
      break;
    default:
      placeholder = t('xcan_indicatorAssert.expressionPlaceholder.regexMatch');
  }
  
  return placeholder;
});

// UI configuration
const textAreaAutoSize = { minRows: 1, maxRows: 5 };
const enumFieldNames = { label: 'message', value: 'value' };

/**
 * Filter out status-type assertions from the list
 * These are excluded as they have special handling
 */
const filterStatus = (value) => {
  return ['BODY_SIZE', 'SIZE', 'DURATION'].includes(value.value);
};
</script>
<template>
  <div ref="assertWrapperRef" class="space-y-3 pb-3">
    <div class="flex-1 mr-3">
      <div class="" :class="{'space-x-2 flex items-start': !props.vertical, 'space-y-2': props.vertical}">
        <div class="flex-1">
          <SelectEnum
            v-model:value="(assertionFormData.type as any)"
            :enumKey="AssertionType"
            class="w-full"
            :placeholder="t('xcan_indicatorAssert.assertionType')"
            :title="t('xcan_indicatorAssert.assertionType')"
            :excludes="filterStatus"
            :readonly="props.viewType"
            :error="hasTypeError"
            @change="handleTypeChange($event)" />
        </div>
        <div class="flex-1">
          <Input
            v-model:value="assertionFormData.parameterName"
            :disabled="assertionFormData.type !== 'HEADER'"
            :readonly="props.viewType"
            :error="hasHeaderNameError"
            :maxlength="400"
            :placeholder="t('xcan_indicatorAssert.headerName')"
            :title="t('xcan_indicatorAssert.headerName')"
            @focus="handleInputFocus"
            @blur="handleInputBlur"
            @change="handleHeaderNameChange" />
        </div>
        <div class="flex items-center flex-nowrap space-x-2" style="flex: 1;min-width: 100px;">
          <Select
            v-model:value="assertionFormData.assertionCondition"
            :placeholder="t('xcan_indicatorAssert.assertionCondition')"
            :title="t('xcan_indicatorAssert.assertionCondition')"
            class="flex-1"
            :disabled="!assertionFormData.type"
            :readonly="props.viewType"
            :options="filteredAssertionConditions"
            :fieldNames="enumFieldNames"
            :error="hasAssertionConditionError"
            @change="handleAssertionConditionChange($event)" />
          <Switch
            class="flex-shrink-0 flex-grow-0"
            :disabled="isExtractionDisabled || props.viewType"
            :checked="shouldExtractValue"
            :checkedChildren="t('xcan_indicatorAssert.extractExpectedValue')"
            :unCheckedChildren="t('xcan_indicatorAssert.expectedValue')"
            @change="handleExtractionToggle($event)" />
        </div>
      </div>
      <div class="transition-all space-y-2 overflow-hidden mt-2">
        <template v-if="shouldShowExpectedInput">
          <template v-if="shouldExtractValue">
            <div class="" :class="{'flex items-start space-x-2': !props.vertical, 'space-y-2': props.vertical}">
              <SelectEnum
                v-model:value="assertionFormData.extraction.method"
                style="flex: 0 0 calc((100% - 40px)/6);"
                class="block"
                :enumKey="ExtractionMethod"
                :placeholder="t('xcan_indicatorAssert.extractionMethod')"
                :title="t('xcan_indicatorAssert.extractionMethod')"
                :readonly="props.viewType"
                :error="hasMethodError"
                @change="handleMethodChange()" />
              <Select
                v-model:value="assertionFormData.extraction.location"
                style="flex: 0 0 calc((100% - 40px)/6);"
                :placeholder="t('xcan_indicatorAssert.extractionLocation')"
                :title="t('xcan_indicatorAssert.extractionLocation')"
                class="block"
                :readonly="props.viewType"
                :fieldNames="enumFieldNames"
                :options="filteredLocationOptions"
                :error="hasLocationError"
                @change="handleLocationChange" />
              <Input
                v-model:value="assertionFormData.extraction.parameterName"
                style="flex: 0 0 calc((100% - 40px)/6);"
                :placeholder="t('xcan_indicatorAssert.extractionParameterName')"
                :title="t('xcan_indicatorAssert.extractionParameterName')"
                :maxlength="400"
                :disabled="LOCATIONS_WITHOUT_PARAMETER_NAME.includes(assertionFormData.extraction.location as any)"
                :readonly="props.viewType"
                :error="hasParameterNameError"
                @focus="handleInputFocus"
                @blur="handleInputBlur"
                @change="handleParameterNameChange" />
              <Input
                v-model:value="assertionFormData.extraction.defaultValue"
                style="flex: 0 0 calc((100% - 40px)/6);"
                :placeholder="t('xcan_indicatorAssert.extractionDefaultValue')"
                :title="t('xcan_indicatorAssert.extractionDefaultValue')"
                :readonly="props.viewType"
                @focus="handleInputFocus"
                @blur="handleInputBlur" />
              <Validate style="flex: 0 0 calc((100% - 40px)/6);" :text="extractionExpressionErrorMsg">
                <Input
                  :ref="el => { extractionExpressionRefs = el }"
                  v-model:value="assertionFormData.extraction.expression"
                  :placeholder="t('xcan_indicatorAssert.extractionExpression')"
                  :title="t('xcan_indicatorAssert.extractionExpression')"
                  :disabled="assertionFormData.extraction.method === 'EXACT_VALUE'"
                  :readonly="props.viewType"
                  :error="hasExtractionExpressionError"
                  @focus="handleInputFocus"
                  @blur="handleExtressionBlur($event)"
                  @change="handleExtractionExpressionChange"
                  @pressEnter="handleExtressionEnter" />
              </Validate>
              <div class="flex items-center space-x-2" style="flex: 0 0 calc((100% - 40px)/6);">
                <Input
                  v-model:value="assertionFormData.extraction.matchItem"
                  :placeholder="t('xcan_indicatorAssert.matchItem')"
                  :title="t('xcan_indicatorAssert.matchItem')"
                  :disabled="assertionFormData.extraction.method === 'EXACT_VALUE'"
                  :readonly="props.viewType"
                  :max="2000"
                  :maxlength="4"
                  dataType="number"
                  @focus="handleInputFocus"
                  @blur="handleInputBlur" />
                <MatchItemPopover />
              </div>
            </div>
          </template>
          <template v-else>
            <div :class="{'space-x-2 flex items-start': !props.vertical, 'space-y-2': props.vertical}">
              <template v-if="shouldShowExpressionInput">
                <Validate style="flex: 0 0 calc((100% - 40px)/6*2 + 8px);" :text="expressionErrorMsg">
                  <Input
                    v-model:value="assertionFormData.expression"
                    :placeholder="expressionPlaceholder"
                    :title="expressionPlaceholder"
                    :error="hasExpressionError"
                    :maxlength="400"
                    :readonly="props.viewType"
                    @focus="handleInputFocus"
                    @blur="handleInputBlur"
                    @change="handleExpressionChange" />
                </Validate>

                <Input
                  v-model:value="assertionFormData.matchItem"
                  :placeholder="t('xcan_indicatorAssert.matchItem')"
                  :title="t('xcan_indicatorAssert.matchItem')"
                  style="flex: 0 0 calc((100% - 40px)/6);"
                  :max="2000"
                  :maxlength="4"
                  :readonly="props.viewType"
                  dataType="number"
                  @focus="handleInputFocus"
                  @blur="handleInputBlur" />

                <div class="flex items-center space-x-2" style="flex: 1;">
                  <Input
                    v-model:value="assertionFormData.expected"
                    :placeholder="t('xcan_indicatorAssert.expectedValueOptional')"
                    :title="t('xcan_indicatorAssert.expectedValue')"
                    :readonly="props.viewType"
                    :error="hasExpectedError"
                    @focus="handleInputFocus"
                    @blur="handleInputBlur"
                    @change="handleExpectedChange" />
                  <ExpectedPopover class="flex-shrink-0" />
                </div>
              </template>
              <template v-else>
                <Input
                  v-model:value="assertionFormData.expected"
                  type="textarea"
                  :placeholder="t('xcan_indicatorAssert.expectedValue')"
                  :title="t('xcan_indicatorAssert.expectedValue')"
                  :readonly="props.viewType"
                  :autoSize="textAreaAutoSize"
                  :error="hasExpectedError"
                  @focus="handleInputFocus"
                  @blur="handleInputBlur"
                  @change="handleExpectedChange" />
              </template>
            </div>
          </template>
        </template>
      </div>
    </div>
  </div>
</template>
<style scoped>
.not-enabled {
  opacity: 0.5;
}
</style>
