/**
 * Composable for managing node data and loading logic
 * Handles loading available nodes for service deployment
 */
import { reactive } from 'vue';
import { node } from '@/api/tester';
import store from '@/store';
import { NodeOption } from '../types';

/**
 * Composable for managing node data and loading logic
 * @returns Node options and loading function
 */
export function useNodeData () {
  // Node state
  const nodeState = reactive<{
    saving: boolean;
    nodeOptions: NodeOption[];
  }>({
    saving: false,
    nodeOptions: []
  });

  /**
   * Load available nodes for service deployment
   * Fetches nodes with MOCK_SERVICE role
   */
  const loadNodes = async (): Promise<void> => {
    const params = {
      pageNo: 1,
      pageSize: store.state.maxPageSize,
      filters: [{ key: 'role', op: 'EQUAL', value: 'MOCK_SERVICE' }]
    };

    const [error, res] = await node.getNodeList(params);
    if (error) {
      return;
    }

    // Map API response to NodeOption type
    nodeState.nodeOptions = res.data.list.map((item: any) => ({
      id: item.id,
      name: item.name,
      ip: item.ip
    }));
  };

  return {
    nodeState,
    loadNodes
  };
}
