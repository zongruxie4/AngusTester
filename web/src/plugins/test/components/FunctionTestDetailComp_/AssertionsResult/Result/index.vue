<script setup lang="ts">
import { computed, ref, onMounted, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';
import { Badge, Descriptions, DescriptionsItem, TypographyParagraph } from 'ant-design-vue';
import { AsyncComponent, Icon, Tooltip, Modal, Spin, MonacoEditor } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import beautify from 'js-beautify';

import { ExecContent } from '@/plugins/test/types';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Assertion status type
 */
type AssertionStatus = 'Disabled' | 'Ignored' | 'Success' | 'Failed';

/**
 * Editor language type for Monaco Editor
 * Note: 'xml' is handled but rendered as 'html' in the editor
 */
type EditorLanguage = 'json' | 'text' | 'yaml' | 'typescript' | 'html';

/**
 * Component props interface
 */
export interface Props {
  value: ExecContent['content']['assertions'][number];  // Single assertion data
  execContent: ExecContent['content'];                  // Full execution content (response data)
  status: AssertionStatus;                              // Overall assertion status
  ignoreAssertions: boolean;                            // Whether to ignore all assertions
  loading: boolean;                                     // Loading state
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  status: undefined,
  execContent: undefined,
  ignoreAssertions: false,
  loading: false
});

/**
 * Event emitters
 */
const emit = defineEmits<{
  (e: 'update:loading', value: boolean): void;  // Update loading state
}>();

/**
 * Component state
 */
const editorRef = ref();                           // Monaco Editor reference
const editorValue = ref('');                       // Current editor content
const editorLoading = ref(true);                   // Editor loading state
const language = ref<EditorLanguage>('text');      // Editor language mode
const modalVisible = ref(false);                   // Modal visibility state

/**
 * Component mount lifecycle hook
 * Signals parent that component has finished loading
 */
onMounted(() => {
  nextTick(() => {
    emit('update:loading', false);
  });
});

/**
 * Open modal to view full content
 * Loads appropriate content and language mode based on view type
 * 
 * @param key - View type: 'real' (actual value) or 'expected' (expected value)
 */
const openModal = (key: 'real' | 'expected'): void => {
  if (key === 'real') {
    // Load actual/real value with detected language
    editorValue.value = editorRealValue.value;
    language.value = realLanguage.value;
  } else {
    // Load expected value (always JSON)
    editorValue.value = editorExpectedValue.value;
    language.value = 'json';
  }
  
  modalVisible.value = true;
  
  // Auto-format after a frame (16.67ms ≈ 1 frame at 60fps)
  setTimeout(() => {
    if (typeof editorRef.value?.format === 'function') {
      editorRef.value.format();
    }
  }, 16.67);
};

/**
 * Computed: Condition warning message
 * Shows warning if condition uses undefined variables
 * 
 * @returns Warning message or empty string
 */
const conditionMessage = computed(() => {
  if (!props.value || props.ignoreAssertions) {
    return '';
  }

  const { actualCondition, condition } = props.value;
  if (!condition) {
    return '';
  }

  // If actual condition equals original condition, variable was not replaced
  if (actualCondition === condition) {
    return t('httpPlugin.functionTestDetail.http.assertions.noVariableDefined', {
      variable: conditionName.value
    });
  }

  return '';
});

/**
 * Computed: Condition expression for display
 * Shows the execution condition or '--' if not set
 * 
 * @returns Condition expression string
 */
const conditionExpression = computed(() => {
  return props.value?.condition || '--';
});

/**
 * Extract variable name from variable expression
 * Variable format: {variableName}
 * If the string doesn't match variable pattern, returns the input as-is
 * 
 * @param str - String to extract variable from
 * @returns Extracted variable name or original string
 */
const extractVar = (str: string): string => {
  const regex = /^\{([^}][a-zA-Z0-9!@$%^&*()_\-+=./]+)\}$/;
  const match = str.match(regex);
  return match ? match[1] : str;
};

/**
 * Split condition string into [left operand, operator, right operand]
 * Supports comparison operators: =, !=, >=, <=, >, <
 * 
 * @param condition - Condition string to split (e.g., "{var} = 200")
 * @param replaceFlag - Whether to extract variable names from left operand
 * @returns [left operand, operator, right operand] or null if invalid
 */
const splitCondition = (condition: string, replaceFlag = true): [string, string, string] | null => {
  if (!condition) {
    return null;
  }

  // Supported comparison operators
  const COMPARISON_OPERATORS: Array<'=' | '!=' | '>=' | '<=' | '>' | '<'> = ['=', '!=', '>=', '<=', '>', '<'];
  
  // Build regex to match: leftOperand operator rightOperand
  const operatorRegex = new RegExp(`^([^\\s${COMPARISON_OPERATORS.join('')}]+)\\s*(${COMPARISON_OPERATORS.join('|')})\\s*([^${COMPARISON_OPERATORS.join('')}]+)$`);
  const match = condition.trim().match(operatorRegex);
  
  if (match && match.length === 4) {
    // Extract variable name if replaceFlag is true
    const varStr = replaceFlag ? extractVar(match[1].trim()) : match[1].trim();
    return [varStr, match[2].trim(), match[3].trim()];
  } else {
    // Invalid input: contains multiple operators, no operator, or operands contain operators
    return null;
  }
};

/**
 * Computed: Condition variable name
 * Extracts the variable name from the condition expression
 * 
 * @returns Variable name or 'Variables' fallback
 */
const conditionName = computed(() => {
  return splitCondition(props.value?.condition || '', true)?.[0] || t('common.variables');
});

/**
 * Computed: Condition variable value
 * Gets the left operand value (with variable syntax)
 * 
 * @returns Variable expression or '--'
 */
const conditionValue = computed(() => {
  return splitCondition(props.value?.condition || '', false)?.[0] || '--';
});

/**
 * Computed: Whether assertion should be ignored
 * True if:
 * - Global ignore assertions flag is set
 * - Execution condition failed
 * - Assertion is disabled
 * 
 * @returns true if assertion should be ignored
 */
const assertionIgnored = computed(() => {
  return props.ignoreAssertions || conditionFailure.value || !props.value?.enabled;
});

/**
 * Computed: Whether execution condition failed
 * Condition fails when the condition expression evaluates to false
 * 
 * @returns true if condition failed
 */
const conditionFailure = computed(() => {
  if (props.ignoreAssertions) {
    return false;
  }

  return props.value.ignore;
});

/**
 * Computed: Execution condition result message
 * Shows status message based on condition evaluation result
 * 
 * @returns Localized result message
 */
const conditionResultMessage = computed(() => {
  if (!props.value) {
    return '';
  }

  if (!props.value.condition) {
    return t('httpPlugin.functionTestDetail.http.assertions.expressionEmpty');
  }

  if (props.value?.ignore) {
    return t('httpPlugin.functionTestDetail.http.assertions.operationResultNotEstablished');
  }

  return t('httpPlugin.functionTestDetail.http.assertions.operationResultEstablished');
});

/**
 * Computed: Assertion label for actual value field
 * Builds label based on assertion type and condition
 * Format: [Type] + [Parameter Name] + [Condition] + "值"
 * 
 * Example outputs:
 * - "状态码"
 * - "响应头Content-Type值"
 * - "响应体正则匹配值"
 * 
 * @returns Localized assertion label
 */
const assertLabel = computed(() => {
  if (!assertionType.value) {
    return '';
  }

  // Header assertions include parameter name
  if (assertionType.value === 'HEADER') {
    const parameterName = props.value.parameterName;
    if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(assertionCondition.value)) {
      return TYPE_MAP.value[assertionType.value] + (parameterName || '') + CONDITION_MAP.value[assertionCondition.value] + '值';
    }

    return TYPE_MAP.value[assertionType.value] + (parameterName || '') + '值';
  }

  // Expression-based conditions include condition type in label
  if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(assertionCondition.value)) {
    return TYPE_MAP.value[assertionType.value] + CONDITION_MAP.value[assertionCondition.value] + '值';
  }

  // Simple assertions just show type
  return TYPE_MAP.value[assertionType.value];
});

/**
 * Computed: Assertion type
 * Returns the type value (STATUS, HEADER, BODY, etc.)
 * 
 * @returns Assertion type string
 */
const assertionType = computed(() => {
  return props.value?.type?.value;
});

/**
 * Computed: Response headers as key-value object
 * Converts headerArray (flat array) to object with lowercase keys
 * 
 * headerArray format: [key1, value1, key2, value2, ...]
 * Result: { key1: value1, key2: value2, ... }
 * 
 * @returns Headers object with lowercase keys
 */
const headers = computed(() => {
  if (!props.execContent?.response?.headerArray?.length) {
    return {};
  }

  const result: Record<string, string> = {};
  const headerArray = props.execContent.response.headerArray;
  
  // Parse flat array into key-value pairs
  for (let i = 0, len = headerArray.length; i < len; i++) {
    if (i % 2 === 0) {
      // Even index: header name
      result[headerArray[i].toLowerCase()] = '';
    } else {
      // Odd index: header value
      result[headerArray[i - 1].toLowerCase()] = headerArray[i];
    }
  }

  return result;
});

/**
 * Computed: Header parameter name (lowercase)
 * Used to lookup header value from headers object
 * 
 * @returns Lowercase header name or empty string
 */
const headerName = computed(() => {
  return props.value?.parameterName?.toLowerCase() || '';
});

/**
 * Computed: Real/actual value from response
 * Retrieves actual value based on assertion type:
 * - BODY: Response body content
 * - HEADER: Specific header value
 * - STATUS: HTTP status code
 * - BODY_SIZE: Response body size (formatted)
 * - SIZE: Total response size (formatted)
 * - DURATION: Request duration (formatted)
 * 
 * @returns Actual value string or empty string
 */
const realValue = computed(() => {
  if (!assertionType.value) {
    return '';
  }

  if (assertionType.value === 'BODY') {
    const body = props.execContent?.response;
    return body?.rawContent || '';
  }

  if (assertionType.value === 'HEADER') {
    return headers.value?.[headerName.value] || '';
  }

  if (assertionType.value === 'STATUS') {
    return props.execContent?.response?.status || '';
  }

  if (assertionType.value === 'BODY_SIZE') {
    return utils.formatBytes(+props.execContent?.response?.bodySize);
  }

  if (assertionType.value === 'SIZE') {
    return utils.formatBytes(+props.execContent?.response?.size);
  }

  if (assertionType.value === 'DURATION') {
    return utils.formatTime(+props.execContent?.response?.timeline?.total);
  }

  return '';
});

/**
 * Computed: Display value for actual value
 * Formats JSON if possible, otherwise returns raw value
 * 
 * @returns Formatted actual value for display
 */
const showRealValue = computed(() => {
  try {
    return JSON.stringify(JSON.parse(realValue.value));
  } catch (error) {
    return realValue.value;
  }
});

/**
 * Computed: Language mode for editor based on content type
 * Detects language from Content-Type header:
 * - application/json → json
 * - text/html → html
 * - text/xml → html (Monaco treats XML as HTML)
 * - application/javascript → typescript
 * - application/yaml → yaml
 * - default → text
 * 
 * @returns Editor language mode
 */
const realLanguage = computed((): EditorLanguage => {
  const contentType = headers.value?.['content-type'];
  if (contentType?.includes('application/json')) {
    return 'json';
  }

  if (contentType?.includes('text/html')) {
    return 'html';
  }

  // XML is rendered as HTML in Monaco Editor
  if (contentType?.includes('text/xml')) {
    return 'html';
  }

  if (contentType?.includes('application/javascript')) {
    return 'typescript';
  }

  if (contentType?.includes('application/yaml')) {
    return 'yaml';
  }

  return 'text';
});

/**
 * Computed: Formatted actual value for Monaco Editor
 * Auto-beautifies code based on language:
 * - JSON/JavaScript: js-beautify
 * - HTML (including XML): html-beautify
 * - Others: no formatting
 * 
 * @returns Formatted code string
 */
const editorRealValue = computed(() => {
  if (!realValue.value) {
    return '';
  }

  // Only beautify BODY assertions
  if (assertionType.value === 'BODY') {
    if (realLanguage.value === 'json' || realLanguage.value === 'typescript') {
      return beautify.js(realValue.value);
    }

    if (realLanguage.value === 'html') {
      return beautify.html(realValue.value);
    }
  }

  return realValue.value;
});

/**
 * Computed: Assertion condition type
 * Returns the condition value (CONTAIN, EQUAL, GREATER_THAN, etc.)
 * 
 * @returns Assertion condition string
 */
const assertionCondition = computed(() => {
  return props.value?.assertionCondition?.value;
});

/**
 * Computed: Label for expected value field
 * Returns "Extracted Value" for extraction assertions,
 * or "Expected Value" for regular assertions
 * 
 * @returns Localized label string
 */
const expectedLabel = computed(() => {
  return props.value?.extraction 
    ? t('httpPlugin.functionTestDetail.http.assertions.extractedValue') 
    : t('httpPlugin.functionTestDetail.http.assertions.expectedValue');
});

/**
 * Computed: Expected value
 * Formats value based on assertion type:
 * - BODY_SIZE/SIZE: Formatted bytes
 * - DURATION: Formatted time
 * - Others: Raw value
 * 
 * @returns Expected value string or '--'
 */
const expectedValue = computed(() => {
  const actualExpected = props.value?.actualExpected;
  if (!actualExpected) {
    return '--';
  }

  if (['BODY_SIZE', 'SIZE'].includes(assertionType.value)) {
    return utils.formatBytes(+actualExpected);
  }

  if (assertionType.value === 'DURATION') {
    return utils.formatTime(+actualExpected);
  }

  return actualExpected;
});

/**
 * Computed: Display value for expected value
 * Formats JSON if possible, otherwise returns raw value
 * 
 * @returns Formatted expected value for display
 */
const showExpectedValue = computed(() => {
  try {
    return JSON.stringify(JSON.parse(expectedValue.value));
  } catch (error) {
    return expectedValue.value;
  }
});

/**
 * Computed: Formatted expected value for Monaco Editor
 * Pretty-prints JSON with 2-space indentation if possible
 * 
 * @returns Formatted JSON string or raw value
 */
const editorExpectedValue = computed(() => {
  try {
    return JSON.stringify(JSON.parse(expectedValue.value), null, 2);
  } catch (error) {
    return expectedValue.value;
  }
});

/**
 * Computed: Assertion result message
 * Returns failure reason if assertion failed
 * 
 * @returns Result message string or undefined
 */
const resultMessage = computed(() => {
  return props.value?.result?.message;
});

/**
 * Computed: Whether assertion result is failure
 * 
 * @returns true if assertion failed
 */
const resultFailure = computed(() => {
  return !!props.value?.result?.failure;
});

/**
 * Empty function for ellipsis callback
 * Required by TypographyParagraph - removing it prevents ellipsis trigger
 */
const onEllipsis = (): void => { };

/**
 * Ellipsis configuration for TypographyParagraph
 * Shows single line with ellipsis, non-expandable
 */
const ellipsis = { rows: 1, expandable: false, onEllipsis };

/**
 * Computed: Localized assertion type names
 * Maps assertion type codes to display names
 */
const TYPE_MAP = computed(() => ({
  STATUS: t('common.status'),
  HEADER: t('httpPlugin.functionTestDetail.http.typeMap.header'),
  BODY: t('httpPlugin.functionTestDetail.http.typeMap.body'),
  BODY_SIZE: t('httpPlugin.functionTestDetail.http.typeMap.bodySize'),
  SIZE: t('common.size'),
  DURATION: t('common.duration')
}));

/**
 * Computed: Localized assertion condition names
 * Maps condition type codes to display names
 */
const CONDITION_MAP = computed(() => ({
  CONTAIN: t('httpPlugin.functionTestDetail.http.conditionMap.contain'),
  EQUAL: t('httpPlugin.functionTestDetail.http.conditionMap.equal'),
  GREATER_THAN: t('httpPlugin.functionTestDetail.http.conditionMap.greaterThan'),
  GREATER_THAN_EQUAL: t('httpPlugin.functionTestDetail.http.conditionMap.greaterThanEqual'),
  IS_EMPTY: t('httpPlugin.functionTestDetail.http.conditionMap.isEmpty'),
  IS_NULL: t('httpPlugin.functionTestDetail.http.conditionMap.isNull'),
  LESS_THAN: t('httpPlugin.functionTestDetail.http.conditionMap.lessThan'),
  LESS_THAN_EQUAL: t('httpPlugin.functionTestDetail.http.conditionMap.lessThanEqual'),
  NOT_CONTAIN: t('httpPlugin.functionTestDetail.http.conditionMap.notContain'),
  NOT_EMPTY: t('httpPlugin.functionTestDetail.http.conditionMap.notEmpty'),
  NOT_EQUAL: t('httpPlugin.functionTestDetail.http.conditionMap.notEqual'),
  NOT_NULL: t('httpPlugin.functionTestDetail.http.conditionMap.notNull'),
  REG_MATCH: t('httpPlugin.functionTestDetail.http.conditionMap.regMatch'),
  XPATH_MATCH: t('httpPlugin.functionTestDetail.http.conditionMap.xpathMatch'),
  JSON_PATH_MATCH: t('httpPlugin.functionTestDetail.http.conditionMap.jsonPathMatch')
}));

/**
 * Conditions that don't require an expected value
 * These conditions only check existence/emptiness
 */
const EMPTY_LIST = ['IS_EMPTY', 'IS_NULL', 'NOT_EMPTY', 'NOT_NULL'];
</script>

<template>
  <!--
    Assertion result display using Ant Design Descriptions component
    Shows all assertion details in a bordered table layout:
    - Execution condition
    - Condition variable value
    - Condition result
    - Actual value from response
    - Assertion condition
    - Expected value
    - Assertion result
  -->
  <Descriptions
    class="px-3 pt-2 pb-3"
    size="small"
    bordered>
    
    <!-- Execution condition field -->
    <DescriptionsItem>
      <template #label>
        <div class="flex items-center">
          <div class="mr-1.5">{{ t('httpPlugin.functionTestDetail.http.assertions.executionCondition') }}</div>
          <!-- Warning icon if condition uses undefined variables -->
          <template v-if="conditionMessage">
            <Tooltip>
              <template #title>{{ conditionMessage }}</template>
              <Icon class="text-status-warn cursor-pointer" icon="icon-jinggao" />
            </Tooltip>
          </template>
        </div>
      </template>
      <div :title="conditionExpression">{{ conditionExpression }}</div>
    </DescriptionsItem>

    <!-- Condition variable value field -->
    <DescriptionsItem>
      <template #label>
        <div class="flex items-center">
          <div class="mr-1.5">{{ conditionName }}值</div>
        </div>
      </template>
      <div :title="conditionValue">{{ conditionValue }}</div>
    </DescriptionsItem>

    <!-- Execution condition result field -->
    <DescriptionsItem :label="t('httpPlugin.functionTestDetail.http.assertions.executionConditionResult')">
      <!-- Ignored status (global ignore or disabled) -->
      <template v-if="props.ignoreAssertions || !props.value?.enabled">
        <Badge status="default" :text="t('status.ignored')" />
      </template>
      <!-- Error status (condition failed) -->
      <template v-else-if="conditionFailure">
        <Badge status="error" :text="conditionResultMessage" />
      </template>
      <!-- Success status (condition established) -->
      <template v-else>
        <Badge status="success" :text="conditionResultMessage" />
      </template>
    </DescriptionsItem>

    <!-- Actual value field from response -->
    <DescriptionsItem :label="assertLabel">
      <!-- BODY/HEADER assertions: show ellipsis with view link -->
      <template v-if="(assertionType === 'BODY' || assertionType === 'HEADER') && showRealValue">
        <div class="flex items-center justify-between flex-nowrap">
          <TypographyParagraph
            style="word-wrap: unset;word-break: break-all;white-space: break-spaces;"
            :ellipsis="ellipsis"
            :content="showRealValue" />
          <div class="flex-shrink-0 text-text-link cursor-pointer" @click="openModal('real')">{{ t('actions.view') }}</div>
        </div>
      </template>

      <!-- Other assertion types: show full value -->
      <template v-else>
        <div :title="showRealValue">{{ showRealValue }}</div>
      </template>
    </DescriptionsItem>

    <!-- Assertion condition field (e.g., EQUAL, CONTAIN, etc.) -->
    <DescriptionsItem :label="t('httpPlugin.functionTestDetail.http.assertions.assertionCondition')">
      {{ CONDITION_MAP[assertionCondition] }}
    </DescriptionsItem>

    <!-- Expected/extracted value field -->
    <DescriptionsItem>
      <template #label>
        <div class="flex items-center">
          <div class="mr-1.5">{{ expectedLabel }}</div>
        </div>
      </template>

      <!-- BODY/HEADER with value: show ellipsis with view link -->
      <template v-if="!EMPTY_LIST.includes(assertionCondition) && (assertionType === 'BODY' || assertionType === 'HEADER') && showExpectedValue">
        <div class="flex items-center justify-between flex-nowrap">
          <TypographyParagraph
            style="word-wrap: unset;word-break: break-all;white-space: break-spaces;"
            :ellipsis="ellipsis"
            :content="showExpectedValue" />
          <div class="flex-shrink-0 text-text-link cursor-pointer" @click="openModal('expected')">{{ t('actions.view') }}</div>
        </div>
      </template>

      <!-- Other cases or empty conditions: show full value -->
      <template v-else>
        <div :title="showExpectedValue">{{ showExpectedValue }}</div>
      </template>
    </DescriptionsItem>

    <!-- Assertion result section -->
    
    <!-- Ignored assertion: gray badge -->
    <template v-if="assertionIgnored">
      <DescriptionsItem :label="t('common.assertionResult')" :span="3">
        <Badge status="default" :text="t('status.ignored')" />
      </DescriptionsItem>
    </template>

    <!-- Active assertion: show result -->
    <template v-else>
      <!-- Failed assertion: red badge + failure reason -->
      <template v-if="resultFailure">
        <DescriptionsItem :label="t('common.assertionResult')" :span="!!resultMessage ? 1 : 3">
          <Badge
            class="flex-shrink-0"
            status="error"
            :text="t('status.failed')" />
        </DescriptionsItem>

        <DescriptionsItem
          v-if="!!resultMessage"
          :label="t('common.failureReason')"
          :span="2">
          <div :title="resultMessage">{{ resultMessage }}</div>
        </DescriptionsItem>
      </template>

      <!-- Successful assertion: green badge -->
      <template v-else>
        <DescriptionsItem :label="t('common.assertionResult')" :span="3">
          <Badge status="success" :text="t('status.success')" />
        </DescriptionsItem>
      </template>
    </template>
  </Descriptions>

  <!-- Modal for viewing full content (async loaded) -->
  <AsyncComponent :visible="modalVisible">
    <Modal
      v-model:visible="modalVisible"
      wrapClassName="assert-modal"
      width="95%"
      :footer="false">
      <Spin
        class="w-full h-full py-3"
        :spinning="editorLoading"
        :mask="false">
        <!-- Monaco Editor for code viewing with syntax highlighting -->
        <MonacoEditor
          ref="editorRef"
          v-model:loading="editorLoading"
          readOnly
          class="h-full"
          :value="editorValue"
          :language="language" />
      </Spin>
    </Modal>
  </AsyncComponent>
</template>

<style>
.assert-modal .ant-modal {
  top: 0;
  max-width: 100%;
  margin: 0;
  padding-bottom: 0;
}

.assert-modal .ant-modal-content {
  display: flex;
  flex-direction: column;
  height: calc(92vh);
}

.assert-modal .ant-modal-body {
  flex: 1;
  height: calc(100% - 44px);
  padding: 0;
}
</style>

<style scoped>
.ant-descriptions-bordered.ant-descriptions-small :deep(.ant-descriptions-item-label),
.ant-descriptions-bordered.ant-descriptions-small :deep(.ant-descriptions-item-content) {
  width: 16.67%;
}
</style>
