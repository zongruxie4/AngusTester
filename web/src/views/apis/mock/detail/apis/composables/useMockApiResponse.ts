import { ref, nextTick } from 'vue';
import { utils } from '@xcan-angus/infra';
import { ResponseConfig } from '../types';

/**
 * Composable for managing Mock API response configuration
 * Handles response list, validation, and form management
 */
export function useMockApiResponse () {
  // Response list management
  const responseIdList = ref<string[]>([]);
  const responseMap = ref<{ [key: string]: ResponseConfig }>({});
  const nameMap = ref<{ [key: string]: string | undefined }>({});
  const nameErrorSet = ref<Set<string>>(new Set());
  const nameErrorMessage = ref<{ [key: string]: string | undefined }>({});
  const openKeys = ref<string[]>([]);
  const priorityMap = ref<{ [key: string]: string }>({});
  const priorityErrorSet = ref<Set<string>>(new Set());
  const enablePushbackSet = ref<Set<string>>(new Set());

  // UI state
  const responseNotify = ref(0);

  /**
   * Add a new response configuration
   */
  const addResponse = () => {
    const id = utils.uuid();
    responseIdList.value.push(id);
    nameMap.value[id] = undefined;
    openKeys.value.push(...[id + '-1', id + '-2']);
    priorityMap.value[id] = '1000';
    responseMap.value[id] = {
      name: '',
      pushback: undefined,
      match: {
        body: undefined,
        parameters: undefined,
        path: undefined,
        priority: '1000'
      },
      content: {
        content: '',
        headers: [],
        delay: { mode: 'NONE' },
        status: '200',
        contentEncoding: undefined
      }
    };
  };

  /**
   * Remove response configuration by index and ID
   * @param index - Index in the response list
   * @param id - Response ID to remove
   */
  const deleteResponse = (index: number, id: string) => {
    responseIdList.value.splice(index, 1);
    delete priorityMap.value[id];
    delete nameMap.value[id];
    delete responseMap.value[id];
    priorityErrorSet.value.delete(id);
    nameErrorSet.value.delete(id);
    delete nameErrorMessage.value[id];

    const _keys = [id + '-1', id + '-2', id + '-3'];
    openKeys.value = openKeys.value.filter(item => !_keys.includes(item));

    // Auto-add empty response if list is empty
    if (!responseIdList.value.length) {
      addResponse();
    }
  };

  /**
   * Reset response configuration to initial state
   */
  const resetMockApiResponse = () => {
    responseIdList.value = [];
    responseMap.value = {};
    nameMap.value = {};
    nameErrorSet.value.clear();
    nameErrorMessage.value = {};
    openKeys.value = [];
    priorityMap.value = {};
    priorityErrorSet.value.clear();
    enablePushbackSet.value.clear();
    responseNotify.value++;
  };

  /**
   * Validate response name for duplicates
   * @param id - Optional specific response ID to validate
   * @returns True if validation passes, false otherwise
   */
  const validateRepeatName = (id?: string): boolean => {
    const tempSet = new Set();
    const repeatNameSet = Object.values(nameMap.value).reduce((prev, cur) => {
      if (tempSet.has(cur)) {
        prev.add(cur);
      }
      tempSet.add(cur);
      return prev;
    }, new Set());

    let errorNum = 0;
    const data = nameMap.value;

    for (const key in data) {
      if (repeatNameSet.has(data[key])) {
        if (id) {
          nameErrorSet.value.add(id);
          nameErrorMessage.value[id] = 'Name is duplicate';
        } else {
          nameErrorSet.value.add(key);
          nameErrorMessage.value[key] = 'Name is duplicate';
        }
        errorNum++;
      } else {
        nameErrorMessage.value[key] = undefined;
        nameErrorSet.value.delete(key);
      }
    }

    return !errorNum;
  };

  /**
   * Validate all response configurations
   * @param formRefs - Array of form component references
   * @returns True if all validations pass, false otherwise
   */
  const validateResponse = (formRefs: {
    matchRefs: any[];
    contentRefs: any[];
    pushbackRefs: any[];
  }): boolean => {
    if (!responseIdList.value?.length) {
      return false;
    }

    priorityErrorSet.value.clear();
    let errorNum = 0;
    const { matchRefs, contentRefs, pushbackRefs } = formRefs;

    if (!validateRepeatName()) {
      errorNum++;
    }

    const ids = responseIdList.value;
    for (let i = 0, len = ids.length; i < len; i++) {
      const id = ids[i];

      if (utils.isEmpty(nameMap.value[id])) {
        nameErrorSet.value.add(id);
        errorNum++;
      }

      if (utils.isEmpty(priorityMap.value[id])) {
        priorityErrorSet.value.add(id);
        errorNum++;
      }

      if (typeof matchRefs[i]?.isValid === 'function') {
        const validFlag = matchRefs[i].isValid();
        if (!validFlag) {
          errorNum++;
        }
      }

      if (typeof contentRefs[i]?.isValid === 'function') {
        const validFlag = contentRefs[i].isValid();
        if (!validFlag) {
          errorNum++;
        }
      }

      // Validate pushback if enabled
      if (enablePushbackSet.value.has(id)) {
        if (typeof pushbackRefs[i]?.isValid === 'function') {
          const validFlag = pushbackRefs[i].isValid();
          if (!validFlag) {
            errorNum++;
          }
        }
      }
    }

    return !errorNum;
  };

  /**
   * Get response parameters for save/update operations
   * @param formRefs - Array of form component references
   * @returns Array of response configuration parameters
   */
  const getResponseParams = (formRefs: {
    matchRefs: any[];
    contentRefs: any[];
    pushbackRefs: any[];
  }): ResponseConfig[] => {
    const params: ResponseConfig[] = [];
    const ids = responseIdList.value;
    const { matchRefs, contentRefs, pushbackRefs } = formRefs;

    for (let i = 0, len = ids.length; i < len; i++) {
      const id = ids[i];
      params[i] = {
        name: nameMap.value[id],
        match: {
          priority: priorityMap.value[id],
          body: undefined,
          parameters: undefined,
          path: undefined
        },
        content: undefined,
        pushback: undefined
      };

      if (typeof matchRefs[i]?.getData === 'function') {
        params[i].match = matchRefs[i].getData();
      }

      if (typeof contentRefs[i]?.getData === 'function') {
        params[i].content = contentRefs[i].getData();
      }

      if (enablePushbackSet.value.has(id)) {
        if (typeof pushbackRefs[i]?.getData === 'function') {
          params[i].pushback = pushbackRefs[i].getData();
        }
      }
    }

    return params;
  };

  // Event handlers
  const nameChange = (event: { target: { value: string; } }, id: string) => {
    const value = event.target.value;
    nameMap.value[id] = value;
    if (!value) {
      nameErrorSet.value.add(id);
      return;
    }

    nameErrorSet.value.delete(id);
    delete nameErrorMessage.value[id];
    validateRepeatName(id);
  };

  const priorityChange = (event: { target: { value: string } }, id: string) => {
    priorityMap.value[id] = event.target.value;
    priorityErrorSet.value.delete(id);
  };

  const priorityBlur = (event: { target: { value: string } }, id: string) => {
    const value = event.target.value.trim();
    priorityMap.value[id] = value;
    if (utils.isEmpty(value)) {
      priorityErrorSet.value.add(id);
      return;
    }
    priorityErrorSet.value.delete(id);
  };

  const enableChange = (checked: boolean, id: string) => {
    const key = id + '-3';

    if (checked) {
      openKeys.value.push(key);
      enablePushbackSet.value.add(id);
      return;
    }

    openKeys.value = openKeys.value.filter(item => item !== key);
    enablePushbackSet.value.delete(id);

    // Clear pushback data when disabled
    if (responseMap.value[id]) {
      responseMap.value[id]!.pushback = undefined;
    }
  };

  const collapseChange = (keys: string[]) => {
    openKeys.value = keys;
  };

  const arrowChange = (key: string) => {
    if (!openKeys.value.includes(key)) {
      openKeys.value.push(key);
      return;
    }
    openKeys.value = openKeys.value.filter(_key => _key !== key);
  };

  return {
    // State
    responseIdList,
    responseMap,
    nameMap,
    nameErrorSet,
    nameErrorMessage,
    openKeys,
    priorityMap,
    priorityErrorSet,
    enablePushbackSet,
    responseNotify,

    // Methods
    addResponse,
    deleteResponse,
    resetMockApiResponse,
    validateRepeatName,
    validateResponse,
    getResponseParams,

    // Event handlers
    nameChange,
    priorityChange,
    priorityBlur,
    enableChange,
    collapseChange,
    arrowChange
  };
}
