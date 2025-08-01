<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Switch } from 'ant-design-vue';
import { Input, Select, SelectEnum, Validate } from '@xcan-angus/vue-ui';
import { enumUtils } from '@xcan-angus/infra';

import MatchItemPopover from './matchItemPopover.vue';
import ExpectedPopover from './expectedPopover.vue';
import jsonpath from './utils/jsonpath';
import xpath from './utils/xpath';
import regexp from './utils/regexp';
import { Extraction } from './utils/extract/PropsType';
import { FormItem } from './PropsType';
import { AssertCondition, AssertType } from './utils/assert/PropsType';

interface Props {
  value: FormItem;
  num?: number;
  viewType?: boolean;
  vertical: boolean;

}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  num: undefined,
  viewType: false,
  vertical: true
});
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

const generateDefaultData = (): FormItem => {
  return {
    assertionCondition: undefined,
    description: undefined,
    expression: undefined,
    matchItem: undefined,
    enabled: false,
    expected: undefined,
    extraction: generateDefaultExtraction(),
    parameterName: undefined,
    name: undefined,
    type: undefined
  };
};
const nameRefs = ref();
const extractionExpressionRefs = ref();

const idList = ref<string[]>([]);
const dataMap = ref<FormItem>(generateDefaultData());// 数据集合

const focus = ref(false);
// const extract = ref<<string>>(new ());
const extract = ref(false);
const extractDisabled = ref(false);// 禁止选择提取值集合

// const typeError = ref<<string>>(new ());
const typeError = ref(false);
const headerNameeError = ref(false);
const assertionConditionError = ref(false);
const expectedError = ref(false);

const extractionExpressionError = ref(false);
const extractionExpressionErrorMsgMap = ref();

const locationError = ref(false);
const methodError = ref(false);
const parameterNameError = ref(false);

// 不允许输入提取参数名称
const NOT_PARAMETER_NAME: readonly ['REQUEST_RAW_BODY', 'RESPONSE_BODY'] = ['REQUEST_RAW_BODY', 'RESPONSE_BODY'];

// 是否显示期望值或提取值的输入框，只有断言条件不是['为空','不为空','为null','不为null']才显示
// const showExpected = ref<<string>>(new ());
const showExpected = ref(false);
const NOT_SHOW_CONDITION: readonly Partial<AssertCondition>[] = ['IS_NULL', 'IS_EMPTY', 'NOT_EMPTY', 'NOT_NULL'];

// 只有断言条件为正则表达式、xpath表达式、jsonpath表达式才显示
const expressionShow = ref();
const expressionError = ref(false);
const expressionErrorMsgMap = ref();

// 断言条件枚举
const assertionConditionOptions = ref<{ message: string; value: string; }[]>([]);
const loadAssertionConditionOptions = async () => {
  const [error, data = []] = await enumUtils.enumToMessages('AssertionCondition');
  if (error) {
    return;
  }

  assertionConditionOptions.value = data;
};

const NUMBER_CONDITION = ['EQUAL', 'NOT_EQUAL', 'GREATER_THAN', 'GREATER_THAN_EQUAL', 'LESS_THAN', 'LESS_THAN_EQUAL'];
const optionsMap = computed(() => {
  const map = dataMap.value;
  const type = map.type;
  const options = assertionConditionOptions.value;
  if (type) {
    if (type === 'BODY' || type === 'HEADER') {
      return options;
    } else {
      return options.filter((item) => NUMBER_CONDITION.includes(item.value));
    }
  } else {
    return options;
  }
});

// 提取位置枚举
const locationOptions = ref<{ message: string; value: string; }[]>([]);
const loadLocationOptions = async () => {
  const [error, data = []] = await enumUtils.enumToMessages('HttpExtractionLocation');
  if (error) {
    return;
  }

  locationOptions.value = data;
};

const locationOptionsMap = computed(() => {
  const map = dataMap.value;
  const options = locationOptions.value;
  const method = map.extraction.method;
  if (method && method === 'EXACT_VALUE') {
    return options.filter((item) => !['RESPONSE_BODY', 'RESPONSE_HEADER', 'REQUEST_RAW_BODY'].includes(item.value));
  } else {
    return options;
  }
});

const switchChange = async (checked: boolean | number | string) => {
  expectedError.value = false;
  parameterNameError.value = false;
  locationError.value = false;
  methodError.value = false;
  expectedError.value = false;
  dataMap.value.expected = '';

  if (checked) {
    extract.value = true;
    return;
  }

  extract.value = false;
};

const inputFocus = () => {
  focus.value = true;
};

const inputBlur = () => {
  focus.value = false;
};

const typeChange = (value: AssertType) => {
  typeError.value = false;

  if (value !== 'HEADER') {
    dataMap.value.parameterName = '';
    headerNameeError.value = false;
  }

  const _condition = dataMap.value.assertionCondition || '';
  // 如果断言类型是
  if (['STATUS'].includes(value)) {
    if (expressionShow.value) {
      expressionShow.value = false;
      dataMap.value.expression = undefined;
      dataMap.value.matchItem = undefined;
      dataMap.value.expected = undefined;
    }

    if (!NUMBER_CONDITION.includes(_condition)) {
      dataMap.value.assertionCondition = 'EQUAL';
      showExpected.value = true;
    }

    // 禁止选择提取值
    extract.value = false;
    extractDisabled.value = true;
    return;
  }

  if (!['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(_condition)) {
    extractDisabled.value = false;
  }
};

const headerNameeChange = () => {
  headerNameeError.value = false;
};

const assertionConditionChange = (value: typeof NOT_SHOW_CONDITION[number]) => {
  assertionConditionError.value = false;
  expressionShow.value = false;
  showExpected.value = true;

  if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(value)) {
    extract.value = false;
    extractDisabled.value = true;
    expressionShow.value = true;
    return;
  }

  if (NOT_SHOW_CONDITION.includes(value)) {
    extractDisabled.value = true;
    showExpected.value = false;
    return;
  }

  extractDisabled.value = false;
};

const expressionChange = () => {
  expressionError.value = false;
  expressionErrorMsgMap.value = undefined;
};

const methodChange = () => {
  methodError.value = false;
  if (dataMap.value.extraction.method === 'EXACT_VALUE') {
    dataMap.value.extraction.expression = '';
    extractionExpressionError.value = false;
    extractionExpressionErrorMsgMap.value = undefined;

    const location = dataMap.value.extraction.location;
    if (location && ['RESPONSE_BODY', 'RESPONSE_HEADER'].includes(location)) {
      dataMap.value.extraction.location = undefined;
    }

    return;
  }

  const expression = dataMap.value.extraction.expression;
  if (expression) {
    validateExtractionExpression(expression);
  }
};

const locationChange = () => {
  locationError.value = false;

  const _location = dataMap.value.extraction.location || '';
  if (!_location) {
    parameterNameError.value = false;
  }

  if (['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(_location)) {
    dataMap.value.extraction.parameterName = undefined;
    parameterNameError.value = false;
  }
};

const parameterNameChange = () => {
  parameterNameError.value = false;
};

const extractionExpressionChange = () => {
  extractionExpressionError.value = false;
  extractionExpressionErrorMsgMap.value = undefined;
};

const expressionBlur = (event: { target: { value: string } }) => {
  inputBlur();

  const value = event.target.value?.trim();
  if (!value) {
    return;
  }

  validateExtractionExpression(value);
};

const expressionEnter = () => {
  if (!extractionExpressionRefs.value) {
    return;
  }

  extractionExpressionRefs.value?.blur?.();
};

const expectedChange = () => {
  expectedError.value = false;
};

const resetError = () => {
  typeError.value = false;
  headerNameeError.value = false;
  assertionConditionError.value = false;
  expectedError.value = false;

  extractionExpressionError.value = false;
  extractionExpressionErrorMsgMap.value = undefined;

  expressionError.value = false;
  expressionErrorMsgMap.value = undefined;

  parameterNameError.value = false;
  locationError.value = false;
  methodError.value = false;
};

const reset = () => {
  idList.value = [];
  nameRefs.value = [];
  focus.value = false;
  extract.value = false;
  extractDisabled.value = false;
  showExpected.value = false;
  expressionShow.value = false;
  dataMap.value = { ...generateDefaultData() };

  resetError();
};

const validateExtractionExpression = (value: string) => {
  const method = dataMap.value.extraction.method;
  let validResult:{ valid: boolean; message: string; } = { valid: false, message: '' };
  if (method === 'JSON_PATH') {
    validResult = jsonpath.isValid(value);
  } else if (method === 'X_PATH') {
    validResult = xpath.isValid(value);
  } else {
    validResult = regexp.isValid(value);
  }

  if (validResult.valid) {
    extractionExpressionError.value = false;
    extractionExpressionErrorMsgMap.value = undefined;
    return true;
  }

  extractionExpressionError.value = true;
  extractionExpressionErrorMsgMap.value = validResult.message;
  return false;
};

const validateExpression = (value: string) => {
  const method = dataMap.value.assertionCondition;
  let validResult:{ valid: boolean; message: string; } = { valid: false, message: '' };
  if (method === 'JSON_PATH_MATCH') {
    validResult = jsonpath.isValid(value);
  } else if (method === 'XPATH_MATCH') {
    validResult = xpath.isValid(value);
  } else {
    validResult = regexp.isValid(value);
  }

  if (validResult.valid) {
    expressionError.value = false;
    expressionErrorMsgMap.value = undefined;
    return true;
  }

  expressionError.value = true;
  expressionErrorMsgMap.value = validResult.message;
  return false;
};

/**
 * @description true - 校验通过，false - 校验失败
 */
const toValidate = (): boolean => {
  resetError();
  const currentInfo = dataMap.value;
  if (!currentInfo.type) {
    typeError.value = true;
  } else {
    // 断言类型是响应头时，响应头名称必须
    if (currentInfo.type === 'HEADER') {
      if (!currentInfo.parameterName) {
        headerNameeError.value = true;
      }
    }
  }

  if (!currentInfo.assertionCondition) {
    assertionConditionError.value = true;
  }

  if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(currentInfo.assertionCondition!)) {
    if (!currentInfo.expression) {
      expressionError.value = true;
    } else {
      validateExpression(currentInfo.expression!);
    }
  } else if (showExpected.value) {
    // 期望值
    if (!extract.value) {
      if (!currentInfo.expected) {
        expectedError.value = true;
      }
    } else { // 提取期望值
      if (currentInfo.extraction.method !== 'EXACT_VALUE') {
        if (!currentInfo.extraction.expression) {
          extractionExpressionError.value = true;
        } else {
          validateExtractionExpression(currentInfo.extraction.expression!);
        }
      }

      // raw请求体和响应体不用校验
      if (!['REQUEST_RAW_BODY', 'RESPONSE_BODY'].includes(currentInfo.extraction.location!)) {
        if (!currentInfo.extraction.parameterName) {
          parameterNameError.value = true;
        }
      }

      if (!currentInfo.extraction.location) {
        locationError.value = true;
      }

      if (!currentInfo.extraction.method) {
        methodError.value = true;
      }
    }
  }

  return !(typeError.value ||
    headerNameeError.value ||
    assertionConditionError.value ||
    expressionError.value ||
    expectedError.value ||
    extractionExpressionError.value ||
    parameterNameError.value ||
    locationError.value ||
    methodError.value
  );
};

const getData = ():{data:FormItem} => {
  const data = dataMap.value;
  const _extract = extract.value;

  const temp = JSON.parse(JSON.stringify(data));

  if (['IS_EMPTY', 'NOT_EMPTY', 'IS_NULL', 'NOT_NULL'].includes(temp.assertionCondition) || temp.expected === '' || temp.expected === undefined) {
    temp.expected = null;
  }

  if (!_extract) {
    temp.extraction = null;
  } else {
    for (const key in temp.extraction) {
      if (temp.extraction[key] === '' || temp.extraction[key] === undefined) {
        temp.extraction[key] = null;
      }
    }
  }

  if (temp.parameterName === '' || temp.parameterName === undefined) {
    temp.parameterName = null;
  }

  return { data: temp };
};

defineExpose({
  getData,
  validate: toValidate
});

onMounted(() => {
  // 加载断言条件枚举选项
  loadAssertionConditionOptions();

  // 加载提取位置枚举选项
  loadLocationOptions();

  watch(() => props.value, async (newValue) => {
    reset();
    const data = newValue as FormItem;
    const extraction = data.extraction;
    const assertionCondition = data.assertionCondition?.value || data.assertionCondition;

    dataMap.value = {
      assertionCondition,
      description: data.description,
      expression: data.expression,
      matchItem: data.matchItem,
      enabled: data.enabled,
      expected: data.expected,
      extraction: data.extraction || generateDefaultExtraction(),
      parameterName: data.parameterName,
      name: data.name,
      type: data.type?.value || data.type
    };

    if (extraction) {
      extract.value = true;
    }
    // @TODO 设置showExpected，只有断言条件不为['为空','不为空','为null','不为null']才显示
    if (!NOT_SHOW_CONDITION.includes(assertionCondition)) {
      showExpected.value = true;
      if (['REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(assertionCondition)) {
        extract.value = false;
        expressionShow.value = true;
      }
    }

    if (['IS_EMPTY', 'NOT_EMPTY', 'IS_NULL', 'NOT_NULL', 'REG_MATCH', 'XPATH_MATCH', 'JSON_PATH_MATCH'].includes(assertionCondition)) {
      extractDisabled.value = true;
    }
  });
});

const expressionPlaceholder = computed(() => {
  const _dataMap = dataMap.value;
  let map = '';
  switch (_dataMap.assertionCondition) {
    case 'REG_MATCH':
      map = '正则匹配表达式';
      break;
    case 'XPATH_MATCH':
      map = 'XPath匹配表达式';
      break;
    case 'JSON_PATH_MATCH':
      map = 'JSONPath匹配表达式';
      break;
    default:
      map = '正则匹配表达式';
  }
  return map;
});

const textAreaAutoSize = { minRows: 1, maxRows: 5 };

const enumFieldNames = { label: 'message', value: 'value' };

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
            v-model:value="dataMap.type"
            enumKey="AssertionType"
            class="w-full"
            placeholder="断言类型"
            title="断言类型"
            :excludes="filterStatus"
            :readonly="props.viewType"
            :error="typeError"
            @change="typeChange($event)" />
        </div>
        <div class="flex-1">
          <Input
            v-model:value="dataMap.parameterName"
            :disabled="dataMap.type !== 'HEADER'"
            :readonly="props.viewType"
            :error="headerNameeError"
            :maxlength="400"
            placeholder="响应头名称"
            title="响应头名称"
            @focus="inputFocus"
            @blur="inputBlur"
            @change="headerNameeChange" />
        </div>
        <div class="flex items-center flex-nowrap space-x-2" style="flex: 1;min-width: 100px;">
          <Select
            v-model:value="dataMap.assertionCondition"
            placeholder="断言条件"
            title="断言条件"
            class="flex-1"
            :disabled="!dataMap.type"
            :readonly="props.viewType"
            :options="optionsMap"
            :fieldNames="enumFieldNames"
            :error="assertionConditionError"
            @change="assertionConditionChange($event)" />
          <Switch
            class="flex-shrink-0 flex-grow-0"
            :disabled="extractDisabled || props.viewType"
            :checked="extract"
            checkedChildren="提取期望值"
            unCheckedChildren="期望值"
            @change="switchChange($event)" />
        </div>
      </div>
      <div class="transition-all space-y-2 overflow-hidden mt-2">
        <template v-if="showExpected">
          <template v-if="extract">
            <div class="" :class="{'flex items-start space-x-2': !props.vertical, 'space-y-2': props.vertical}">
              <SelectEnum
                v-model:value="dataMap.extraction.method"
                style="flex: 0 0 calc((100% - 40px)/6);"
                class="block"
                enumKey="ExtractionMethod"
                placeholder="提取方式"
                title="提取方式"
                :readonly="props.viewType"
                :error="methodError"
                @change="methodChange()" />
              <Select
                v-model:value="dataMap.extraction.location"
                style="flex: 0 0 calc((100% - 40px)/6);"
                placeholder="提取位置"
                title="提取位置"
                class="block"
                :readonly="props.viewType"
                :fieldNames="enumFieldNames"
                :options="locationOptionsMap"
                :error="locationError"
                @change="locationChange" />
              <Input
                v-model:value="dataMap.extraction.parameterName"
                style="flex: 0 0 calc((100% - 40px)/6);"
                placeholder="提取参数名称"
                title="提取参数名称"
                :maxlength="400"
                :disabled="NOT_PARAMETER_NAME.includes(dataMap.extraction.location as any)"
                :readonly="props.viewType"
                :error="parameterNameError"
                @focus="inputFocus"
                @blur="inputBlur"
                @change="parameterNameChange" />
              <Input
                v-model:value="dataMap.extraction.defaultValue"
                style="flex: 0 0 calc((100% - 40px)/6);"
                placeholder="提取缺省值（可选）"
                title="提取缺省值（可选）"
                :readonly="props.viewType"
                @focus="inputFocus"
                @blur="inputBlur" />
              <Validate style="flex: 0 0 calc((100% - 40px)/6);" :text="extractionExpressionErrorMsgMap">
                <Input
                  :ref="el => { extractionExpressionRefs = el }"
                  v-model:value="dataMap.extraction.expression"
                  placeholder="提取表达式"
                  title="提取表达式"
                  :disabled="dataMap.extraction.method === 'EXACT_VALUE'"
                  :readonly="props.viewType"
                  :error="extractionExpressionError"
                  @focus="inputFocus"
                  @blur="expressionBlur($event)"
                  @change="extractionExpressionChange"
                  @pressEnter="expressionEnter" />
              </Validate>
              <div class="flex items-center space-x-2" style="flex: 0 0 calc((100% - 40px)/6);">
                <Input
                  v-model:value="dataMap.extraction.matchItem"
                  placeholder="匹配项，范围0-2000（可选）"
                  title="匹配项"
                  :disabled="dataMap.extraction.method === 'EXACT_VALUE'"
                  :readonly="props.viewType"
                  :max="2000"
                  :maxlength="4"
                  dataType="number"
                  @focus="inputFocus"
                  @blur="inputBlur" />
                <MatchItemPopover />
              </div>
            </div>
          </template>
          <template v-else>
            <div :class="{'space-x-2 flex items-start': !props.vertical, 'space-y-2': props.vertical}">
              <template v-if="expressionShow">
                <Validate style="flex: 0 0 calc((100% - 40px)/6*2 + 8px);" :text="expressionErrorMsgMap">
                  <Input
                    v-model:value="dataMap.expression"
                    :placeholder="expressionPlaceholder"
                    :title="expressionPlaceholder"
                    :error="expressionError"
                    :maxlength="400"
                    :readonly="props.viewType"
                    @focus="inputFocus"
                    @blur="inputBlur"
                    @change="expressionChange" />
                </Validate>

                <Input
                  v-model:value="dataMap.matchItem"
                  placeholder="匹配项，范围0-2000（可选）"
                  title="匹配项"
                  style="flex: 0 0 calc((100% - 40px)/6);"
                  :max="2000"
                  :maxlength="4"
                  :readonly="props.viewType"
                  dataType="number"
                  @focus="inputFocus"
                  @blur="inputBlur" />

                <div class="flex items-center space-x-2" style="flex: 1;">
                  <Input
                    v-model:value="dataMap.expected"
                    placeholder="期望值（可选）"
                    title="期望值"
                    :readonly="props.viewType"
                    :error="expectedError"
                    @focus="inputFocus"
                    @blur="inputBlur"
                    @change="expectedChange" />
                  <ExpectedPopover class="flex-shrink-0" />
                </div>
              </template>
              <template v-else>
                <Input
                  v-model:value="dataMap.expected"
                  type="textarea"
                  placeholder="期望值"
                  title="期望值"
                  :readonly="props.viewType"
                  :autoSize="textAreaAutoSize"
                  :error="expectedError"
                  @focus="inputFocus"
                  @blur="inputBlur"
                  @change="expectedChange" />
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
