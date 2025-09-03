import { ref, watch } from 'vue';
import type { TreeProps } from 'ant-design-vue';
import { space } from '@/api/storage';
import store from '@/store';
import type { MoveTarget } from '../../components/type';

interface Props {
  visible: boolean;
  moveIds?: string[];
}

/**
 * Composable for managing file/directory move operations
 * Handles loading space data, directory tree navigation, and move target selection
 *
 * @param props - Component props containing visibility and move IDs
 * @param projectId - Current project ID
 * @returns Object containing reactive state and methods for move operations
 */
export function useMove (props: Props, projectId: string) {
  // Tree state
  const expandedKeys = ref<string[]>([]);
  const selectedKeys = ref<string[]>([]);
  const target = ref<MoveTarget>({});
  const treeData = ref<TreeProps['treeData']>([]);

  /**
   * Load available spaces for move targets
   */
  const loadSpaces = async (): Promise<void> => {
    const [error, res = { data: {} }] = await space.getSpaceList({
      appCode: 'AngusTester',
      pageSize: store.state.maxPageSize,
      projectId
    });

    if (error) {
      return;
    }

    treeData.value = (res.data.list || []).map(data => ({ ...data, type: 'SPACE' })).slice();
  };

  /**
   * Load directory data for a tree node
   *
   * @param treeNode - Tree node to load data for
   */
  const loadNodeData: TreeProps['loadData'] = treeNode => {
    const spaceId = treeNode.type?.value === 'DIRECTORY' ? treeNode.spaceId : treeNode.id;
    const parentDirectoryId = treeNode.type?.value === 'DIRECTORY' ? treeNode.id : '-1';

    return space.getFileList({
      spaceId,
      parentDirectoryId,
      filters: [{ key: 'type', value: 'DIRECTORY', op: 'EQUAL' }],
      pageSize: store.state.maxPageSize,
      pageNo: 1
    }).then(([error, res]) => {
      if (error) {
        return;
      }

      if (treeNode.dataRef?.children) {
        return;
      }

      if (treeNode.dataRef) {
        const children = (res.data.list || [])
          .map(data => ({
            ...data,
            spaceId: spaceId,
            isLeaf: data.summary.subDirectoryNum === '0'
          }))
          .filter(data => !props.moveIds?.includes(data.id));

        treeNode.dataRef.children = children;
      }

      // Create a new array to trigger reactivity
      treeData.value = [...(treeData.value || [])];
    });
  };

  /**
   * Handle tree node selection
   *
   * @param _selectedKeys - Selected keys (not used)
   * @param selectionInfo - Selection information
   */
  const handleSelect = (
    _selectedKeys: string[],
    selectionInfo: { selected: boolean; selectedNodes: any[] }
  ): void => {
    const { selected, selectedNodes } = selectionInfo;

    if (selected) {
      const { type, id, spaceId } = selectedNodes[0];
      target.value = type === 'SPACE'
        ? { targetSpaceId: id }
        : { targetSpaceId: spaceId, targetDirectoryId: id };
    } else {
      target.value = {};
    }
  };

  /**
   * Reset tree state when visibility changes
   */
  const resetTreeState = (): void => {
    expandedKeys.value = [];
    treeData.value = [];
    selectedKeys.value = [];
  };

  /**
   * Initialize composable - watch for visibility changes
   */
  const init = (): void => {
    watch(() => props.visible, newValue => {
      if (newValue) {
        loadSpaces();
      } else {
        resetTreeState();
      }
    }, {
      immediate: true
    });
  };

  return {
    // State
    expandedKeys,
    selectedKeys,
    target,
    treeData,

    // Methods
    loadNodeData,
    handleSelect,
    resetTreeState,
    init
  };
}
