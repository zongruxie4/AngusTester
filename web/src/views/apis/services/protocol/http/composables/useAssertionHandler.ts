import { ref } from 'vue';
import { AssertionCondition, AssertionType } from '@xcan-angus/infra';
import assertUtils from '@/utils/assertutils';
import { AssertResult, ConditionResult } from '@/views/apis/services/protocol/http/types';
import { useI18n } from 'vue-i18n'


/**
 * Assertion handling composable
 * <p>
 * Handles creation, validation and execution of API assertions.
 * </p>
 */
export function useAssertionHandler () {
  // Assertion count
  const assertNum = ref(0);

  const { t } = useI18n();

  // Assertion results
  const assertResult = ref<AssertResult[]>();

  // Extra info for assertion variables
  const assertionVariableExtra = ref<any>({});

  /**
   * Validate parameters
   * <p>
   * Validate whether the assertion form is valid
   * </p>
   * @param assertFormRef - Assertion form ref
   * @returns Whether validation passed
   */
  const validateParam = (assertFormRef: any) => {
    return !assertFormRef.value || assertFormRef.value?.validate();
  };

  /**
   * Get assertion data
   * <p>
   * Get assertion data from the assertion form
   * </p>
   * @param assertFormRef - Assertion form ref
   * @returns Assertion data and variables
   */
  const getAssertionData = (assertFormRef: any) => {
    if (typeof assertFormRef.value?.getData === 'function') {
      return assertFormRef.value.getData();
    }
    return { data: [], variables: [] };
  };

  /**
   * Execute assertions
   * <p>
   * Execute assertions and return results
   * </p>
   * @param requestInfo - Request info
   * @param assertions - Assertion list
   * @param variableValues - Variable values
   * @returns Assertion results
   */
  const executeAssertions = async (requestInfo: any, assertions: any[], variableValues: any[]) => {
    if (!assertions?.length) {
      return [];
    }

    try {
      const result = await assertUtils.assert.execute(requestInfo, assertions, variableValues);
      return result || [];
    } catch (error) {
      console.error('Assertion execution failed:', error);
      return [];
    }
  };

  /**
   * Execute proxy assertions
   * <p>
   * Execute assertions in proxy mode
   * </p>
   * @param conditions - Conditions list
   * @param variableValues - Variable values
   * @returns Assertion execution result
   */
  const executeProxyAssertions = (conditions: any[], variableValues: any[]) => {
    if (!conditions?.length) {
      return { extra: {} };
    }

    try {
      const { extra } = assertUtils.proxy.execute(conditions, variableValues);
      return { extra };
    } catch (error) {
      console.error('Proxy assertion execution failed:', error);
      return { extra: {} };
    }
  };

  /**
   * Get assertion value by type
   * <p>
   * Extract appropriate value from response data based on assertion type and condition
   * </p>
   * @param assertionCondition - Assertion condition type
   * @param type - Assertion type
   * @param data - Response data
   * @param parameterName - Parameter name
   * @returns Extracted value and related info
   */
  const getValueByType = (
    assertionCondition: AssertionCondition,
    type: AssertionType,
    data: {
      bodySize: number;
      size: number;
      duration: number;
      status: number;
      responseHeader: string[];
      rawContent: string;
      extractValue: string;
    },
    parameterName: string
  ): {
    data: string | null;
    message: string;
    errorMessage: string;
  } => {
    if (type === AssertionType.BODY) {
      if ([AssertionCondition.REG_MATCH, AssertionCondition.XPATH_MATCH, AssertionCondition.JSON_PATH_MATCH].includes(assertionCondition)) {
        return {
          data: data.extractValue,
          message: '',
          errorMessage: ''
        };
      }

      return {
        data: data.rawContent,
        message: '',
        errorMessage: ''
      };
    }

    if (type === AssertionType.BODY_SIZE) {
      return {
        data: data.bodySize + '',
        message: '',
        errorMessage: ''
      };
    }

    if (type === AssertionType.DURATION) {
      return {
        data: data.duration + '',
        message: '',
        errorMessage: ''
      };
    }

    if (type === AssertionType.HEADER) {
      if ([AssertionCondition.REG_MATCH, AssertionCondition.XPATH_MATCH, AssertionCondition.JSON_PATH_MATCH].includes(assertionCondition)) {
        return {
          data: data.extractValue,
          message: '',
          errorMessage: ''
        };
      }

      return {
        data: getHeaderParams(data.responseHeader, parameterName),
        message: '',
        errorMessage: ''
      };
    }

    if (type === AssertionType.SIZE) {
      return {
        data: data.size + '',
        message: '',
        errorMessage: ''
      };
    }

    return {
      data: data.status + '',
      message: '',
      errorMessage: ''
    };
  };

  /**
   * Get header parameter
   * <p>
   * Extract the value for the specified name from a header array
   * </p>
   * @param data - Header array
   * @param name - Parameter name
   * @returns Parameter value
   */
  const getHeaderParams = (data: string[], name: string): string => {
    if (!data?.length || !name) {
      return '';
    }

    const result: string[] = [];
    const lowerName = name.toLowerCase();
    for (let i = 0, len = data.length; i < len; i = i + 2) {
      if (lowerName === data[i].toLowerCase()) {
        result.push(data[i + 1]);
      }
    }

    return result.join(',');
  };

  /**
   * Process assertion results
   * <p>
   * Process assertion results and format for display
   * </p>
   * @param assertions - Assertions list
   * @param responseData - Response data
   * @param assertionVariableExtra - Extra info for assertion variables
   * @returns Processed assertion results
   */
  const processAssertionResults = async (
    assertions: any[],
    responseData: any,
    assertionVariableExtra: any
  ): Promise<AssertResult[]> => {
    if (!assertions?.length) {
      return [];
    }

    const result: AssertResult[] = [];

    for (let i = 0, len = assertions.length; i < len; i++) {
      const {
        name,
        assertionCondition,
        condition,
        expected,
        extraction,
        type,
        parameterName,
        extractValue,
        result: _assertResult
      } = assertions[i];

      let _condition: ConditionResult = {
        failure: false,
        name: '',
        conditionMessage: '',
        failureMessage: '',
        value: '',
        ignored: false,
        message: t('service.apis.assertion.conditionMessages.empty')
      };

      let ignored = true;
      let failure = false;

      if (_assertResult) {
        ignored = _assertResult.ignored || false;
        failure = _assertResult.failure;

        if (!condition) {
          _condition = {
            failure: false,
            name: '',
            conditionMessage: '',
            failureMessage: '',
            value: '',
            ignored: false,
            message: t('service.apis.assertion.conditionMessages.empty')
          };
        } else {
          const matchsMap = assertionVariableExtra?.matchs || {};
          const varMap = assertionVariableExtra?.vars || {};
          const matchs = matchsMap[condition];

          if (!matchs) {
            _condition = {
              failure: false,
              name: '',
              conditionMessage: t('service.apis.assertion.conditionMessages.formatError'),
              failureMessage: t('service.apis.assertion.conditionMessages.formatFailed'),
              value: '',
              ignored: true,
              message: t('service.apis.assertion.conditionMessages.formatError')
            };
          } else {
            const [leftOperand] = matchs;
            const varValue = varMap[leftOperand];
            let value = '';
            let failureMessage = '';

            if (varValue) {
              value = varValue.value;
              failureMessage = varValue.failureMessage;
            } else {
              failureMessage = t('service.apis.assertion.conditionMessages.defineFailed');
              value = leftOperand;
            }

            if (ignored) {
              _condition = {
                failure: true,
                name: leftOperand,
                conditionMessage: '',
                failureMessage,
                value,
                ignored: true,
                message: t('service.apis.assertion.conditionMessages.ignored')
              };
            } else {
              _condition = {
                failure: false,
                name: leftOperand,
                conditionMessage: '',
                failureMessage,
                value,
                ignored: false,
                message: t('service.apis.assertion.conditionMessages.executed')
              };
            }
          }
        }
      }

      // Expected value processing
      const expectedData: { data: string | null; message: string; errorMessage: string; } = {
        data: expected,
        message: '',
        errorMessage: ''
      };

      if (extraction) {
        expectedData.data = extraction.finalValue;
        if (expectedData.data === undefined) {
          expectedData.data = null;
        } else if (typeof expectedData.data === 'object') {
          expectedData.data = JSON.stringify(expectedData.data);
        } else if (typeof expectedData.data !== 'string') {
          expectedData.data = expectedData.data + '';
        }
      }

      const data: {
        bodySize: number;
        size: number;
        duration: number;
        status: number;
        responseHeader: string[];
        rawContent: string;
        extractValue: string;
      } = {
        extractValue: extractValue || '',
        bodySize: 0,
        size: 0,
        duration: 0,
        status: 0,
        responseHeader: [],
        rawContent: ''
      };

      if (responseData) {
        const _response = responseData.response;
        data.bodySize = _response.bodySize;
        data.size = _response.size;
        data.status = _response.status;
        data.duration = _response.timeline?.total;
        data.responseHeader = _response.headerArray;
        data.rawContent = _response.rawContent;
      }

      const realValueData = getValueByType(assertionCondition, type.value, data, parameterName);
      const _realData = realValueData.data;

      if (_realData === undefined) {
        realValueData.data = null;
      } else if (typeof _realData === 'object') {
        realValueData.data = JSON.stringify(_realData);
      } else if (typeof _realData !== 'string') {
        realValueData.data = _realData + '';
      }

      const _result: AssertResult = {
        name,
        parameterName,
        condition,
        assertionCondition,
        type: (type as { message: string; value: AssertionType }).value,
        extraction: !!extraction,
        _condition,
        result: {
          expectedData,
          failure,
          realValueData,
          message: _assertResult?.message
        }
      };

      result.push(_result);
    }

    return result;
  };

  /**
   * Update assertion count
   * <p>
   * Update displayed assertion count
   * </p>
   * @param count - Assertion count
   */
  const updateAssertNum = (count: number) => {
    assertNum.value = count;
  };

  /**
   * Reset assertion state
   * <p>
   * Reset all assertion-related states
   * </p>
   */
  const resetAssertionState = () => {
    assertNum.value = 0;
    assertResult.value = undefined;
    assertionVariableExtra.value = {};
  };

  return {
    // 状态
    assertNum,
    assertResult,
    assertionVariableExtra,

    // 方法
    validateParam,
    getAssertionData,
    executeAssertions,
    executeProxyAssertions,
    getValueByType,
    getHeaderParams,
    processAssertionResults,
    updateAssertNum,
    resetAssertionState
  };
}
