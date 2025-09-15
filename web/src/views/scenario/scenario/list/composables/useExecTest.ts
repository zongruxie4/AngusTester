import { ref } from 'vue';
import { utils } from '@xcan-angus/infra';

/**
 * Server variable structure
 */
interface ServerVariable {
  id: string;
  name: string;
  description: string;
  default: string;
  enum: { id: string; value: string }[];
}

/**
 * Server structure
 */
interface Server {
  url: string;
  description: string;
  variables: ServerVariable[];
}

/**
 * Composable for managing execution test functionality
 */
export function useExecTest () {
  const checkedType = ref<'none' | 'checked'>('none');
  const confirmLoading = ref(false);
  const selectedIds = ref<string[]>([]);
  const selectedServers = ref<Server[]>([]);

  /**
   * Handle radio change event
   */
  const radioChange = (event: any) => {
    const value = event.target.value;
    if (value === 'none') {
      selectedIds.value = [];
      selectedServers.value = [];
    }
  };

  /**
   * Handle select change event
   */
  const selectChange = (value: any, option: any) => {
    selectedIds.value = value;
    selectedServers.value = option.map((item: any) => {
      const variables: ServerVariable[] = [];
      if (item.variables) {
        const names = Object.keys(item.variables);
        for (let i = 0, len = names.length; i < len; i++) {
          const _name = names[i];
          const enumList = item.variables[_name]?.enum?.map((_value: string) => {
            return {
              id: utils.uuid(),
              value: _value
            };
          });
          variables.push({
            ...item.variables[_name],
            enum: enumList,
            name: _name,
            id: utils.uuid()
          });
        }
      }

      return {
        ...item,
        variables
      };
    });
  };

  /**
   * Reset form state
   */
  const reset = () => {
    checkedType.value = 'none';
    confirmLoading.value = false;
    selectedIds.value = [];
    selectedServers.value = [];
  };

  /**
   * Get parameters for saving
   */
  const getSaveParams = (data: Server) => {
    const variables = data.variables.reduce((prev, cur) => {
      prev[cur.name] = {
        default: cur.default,
        description: cur.description,
        enum: cur.enum?.map(item => item.value) || []
      };

      return prev;
    }, {} as {
      [key: string]: {
        default: string;
        description: string;
        enum: string[];
      }
    });

    return {
      description: data.description,
      url: data.url,
      variables
    };
  };

  /**
   * Get parameters for API call
   */
  const getParams = () => {
    return selectedServers.value.map(item => getSaveParams(item));
  };

  return {
    // State
    checkedType,
    confirmLoading,
    selectedIds,
    selectedServers,

    // Methods
    radioChange,
    selectChange,
    reset,
    getSaveParams,
    getParams
  };
}
