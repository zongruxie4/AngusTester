import { ref, computed } from 'vue';
import { AssertionCondition, AssertionType } from '@xcan-angus/infra';
import assertUtils from '@/utils/assertutils';
import { AssertResult, ConditionResult } from '@/views/apis/services/protocol/http/types';

/**
 * 断言处理composable
 * <p>
 * 处理API断言的创建、验证和执行
 * </p>
 */
export function useAssertionHandler () {
  // 断言数量
  const assertNum = ref(0);

  // 断言结果
  const assertResult = ref<AssertResult[]>();

  // 断言变量额外信息
  const assertionVariableExtra = ref<any>({});

  /**
   * 验证参数
   * <p>
   * 验证断言表单是否有效
   * </p>
   * @param assertFormRef - 断言表单引用
   * @returns 验证是否通过
   */
  const validateParam = (assertFormRef: any) => {
    return !assertFormRef.value || assertFormRef.value?.validate();
  };

  /**
   * 获取断言数据
   * <p>
   * 从断言表单中获取断言数据
   * </p>
   * @param assertFormRef - 断言表单引用
   * @returns 断言数据和变量
   */
  const getAssertionData = (assertFormRef: any) => {
    if (typeof assertFormRef.value?.getData === 'function') {
      return assertFormRef.value.getData();
    }
    return { data: [], variables: [] };
  };

  /**
   * 执行断言
   * <p>
   * 执行断言并返回结果
   * </p>
   * @param requestInfo - 请求信息
   * @param assertions - 断言列表
   * @param variableValues - 变量值
   * @returns 断言执行结果
   */
  const executeAssertions = async (requestInfo: any, assertions: any[], variableValues: any[]) => {
    if (!assertions?.length) {
      return [];
    }

    try {
      const result = await assertUtils.assert.execute(requestInfo, assertions, variableValues);
      return result || [];
    } catch (error) {
      console.error('断言执行失败:', error);
      return [];
    }
  };

  /**
   * 执行代理断言
   * <p>
   * 在代理模式下执行断言
   * </p>
   * @param conditions - 条件列表
   * @param variableValues - 变量值
   * @returns 断言执行结果
   */
  const executeProxyAssertions = (conditions: any[], variableValues: any[]) => {
    if (!conditions?.length) {
      return { extra: {} };
    }

    try {
      const { extra } = assertUtils.proxy.execute(conditions, variableValues);
      return { extra };
    } catch (error) {
      console.error('代理断言执行失败:', error);
      return { extra: {} };
    }
  };

  /**
   * 根据类型获取断言值
   * <p>
   * 根据断言类型和条件从响应数据中提取适当的值
   * </p>
   * @param assertionCondition - 断言条件类型
   * @param type - 断言类型
   * @param data - 响应数据
   * @param parameterName - 参数名称
   * @returns 提取的值和相关信息
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
   * 获取header参数
   * <p>
   * 从header数组中提取指定名称的参数值
   * </p>
   * @param data - header数组
   * @param name - 参数名称
   * @returns 参数值
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
   * 处理断言结果
   * <p>
   * 处理断言结果并格式化显示
   * </p>
   * @param assertions - 断言列表
   * @param responseData - 响应数据
   * @param assertionVariableExtra - 断言变量额外信息
   * @returns 处理后的断言结果
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
        message: '条件消息为空'
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
            message: '条件消息为空'
          };
        } else {
          const matchsMap = assertionVariableExtra?.matchs || {};
          const varMap = assertionVariableExtra?.vars || {};
          const matchs = matchsMap[condition];

          if (!matchs) {
            _condition = {
              failure: false,
              name: '',
              conditionMessage: '条件消息格式错误',
              failureMessage: '条件消息格式失败',
              value: '',
              ignored: true,
              message: '条件消息格式错误'
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
              failureMessage = '条件消息定义失败';
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
                message: '条件消息忽略'
              };
            } else {
              _condition = {
                failure: false,
                name: leftOperand,
                conditionMessage: '',
                failureMessage,
                value,
                ignored: false,
                message: '条件消息执行'
              };
            }
          }
        }
      }

      // 期望值处理
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
   * 更新断言数量
   * <p>
   * 更新断言数量显示
   * </p>
   * @param count - 断言数量
   */
  const updateAssertNum = (count: number) => {
    assertNum.value = count;
  };

  /**
   * 重置断言状态
   * <p>
   * 重置所有断言相关状态
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
