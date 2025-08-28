<script lang="ts" setup>
// Vue composition API imports
import { onMounted, ref, watch, computed } from 'vue';
import { useI18n } from 'vue-i18n';

// Ant Design components
import { Tree } from 'ant-design-vue';

// Custom UI components
import { Icon, Modal } from '@xcan-angus/vue-ui';

// Utilities and API
import { travelTreeData } from '@/views/project/project/utils';
import { modules } from '@/api/tester';
import type { MoveModuleProps, ModuleItem } from './types';

/**
 * Component for moving modules to different parent locations
 * Provides a tree interface for selecting new parent module
 */

// Initialize internationalization
const { t } = useI18n();

// Props definition with proper defaults
const props = withDefaults(defineProps<MoveModuleProps>(), {
  visible: false,
  projectId: '',
  projectName: '',
  module: () => ({
    id: '',
    childLevels: 0,
    pid: ''
  })
});

// Emit events for parent component communication
const emits = defineEmits<{
  /** Updates modal visibility state */
  (e: 'update:visible', value: boolean): void;
  /** Emitted when module move is completed successfully */
  (e: 'ok'): void;
}>();

// Reactive state management
const treeData = ref<ModuleItem[]>([]);
const selectedParentIds = ref<string[]>([]);
const isLoading = ref(false);
const isTreeLoading = ref(false);

/**
 * Computed property for OK button state
 * Manages loading and validation states
 */
const okButtonConfig = computed(() => ({
  disabled: selectedParentIds.value.length === 0,
  loading: isLoading.value
}));

/**
 * Fetches and processes the module tree data for selection
 * Excludes the module being moved and its children from selection
 */
const loadModuleTree = async (): Promise<void> => {
  isTreeLoading.value = true;
  
  try {
    const [error, response] = await modules.getModuleTree({
      projectId: props.projectId
    });
    
    if (error) {
      console.error('Failed to load module tree:', error);
      return;
    }

    const rawData = response?.data || [];
    
    // Process tree data with move restrictions
    const processedChildren = travelTreeData(rawData, (item: ModuleItem) => {
      // Disable modules that cannot be selected as new parent
      const processedItem = { ...item };
      
      // Disable if this is the module being moved or its descendant
      if (item.ids?.includes(props.module.id)) {
        processedItem.disabled = true;
      }
      
      // Disable if selecting this parent would exceed max depth
      if ((item.level || 0) + props.module.childLevels > 4) {
        processedItem.disabled = true;
      }
      
      return processedItem;
    });

    // Build complete tree with project root
    treeData.value = [{
      name: props.projectName,
      level: -1,
      id: '-1',
      children: processedChildren
    }];
    
  } catch (error) {
    console.error('Unexpected error while loading module tree:', error);
  } finally {
    isTreeLoading.value = false;
  }
};

/**
 * Handles modal cancellation
 * Resets selection and closes the modal
 */
const handleCancel = (): void => {
  resetSelection();
  closeModal();
};

/**
 * Handles module move operation
 * Updates module parent and triggers success callback
 */
const handleMoveModule = async (): Promise<void> => {
  const newParentId = selectedParentIds.value[0];
  
  // Skip if moving to same parent
  if (props.module.pid === newParentId) {
    closeModal();
    return;
  }

  isLoading.value = true;
  
  try {
    // Update module with new parent
    const [error] = await modules.updateModule([{
      id: props.module.id,
      pid: newParentId
    }]);
    
    if (error) {
      console.error('Failed to move module:', error);
      return;
    }

    // Success handling
    closeModal();
    emits('ok');
    
  } catch (error) {
    console.error('Unexpected error during module move:', error);
  } finally {
    isLoading.value = false;
  }
};

/**
 * Resets the selection state
 */
const resetSelection = (): void => {
  selectedParentIds.value = [];
};

/**
 * Closes the modal by emitting visibility update
 */
const closeModal = (): void => {
  emits('update:visible', false);
};

/**
 * Checks if a module can be selected as new parent
 * Prevents circular references and depth limit violations
 */
const canSelectAsParent = (moduleItem: ModuleItem): boolean => {
  // Cannot select the module being moved or its descendants
  if (moduleItem.ids?.includes(props.module.id)) {
    return false;
  }
  
  // Cannot select if it would exceed maximum depth
  if ((moduleItem.level || 0) + props.module.childLevels > 4) {
    return false;
  }
  
  return true;
};

// Lifecycle hooks and watchers

/**
 * Watches for modal visibility changes
 * Loads tree data when modal opens and resets state when closes
 */
onMounted(() => {
  watch(() => props.visible, (isVisible) => {
    resetSelection();
    
    if (isVisible) {
      loadModuleTree();
    } else {
      // Clear tree data when modal closes to free memory
      treeData.value = [];
    }
  }, {
    immediate: true
  });
});
</script>
<template>
  <!-- Modal for moving module to different parent -->
  <Modal
    :title="t('project.projectEdit.module.moveModule')"
    :okButtonProps="okButtonConfig"
    :visible="props.visible"
    @cancel="handleCancel"
    @ok="handleMoveModule">
    <!-- Tree view for selecting target parent module -->
    <div v-if="isTreeLoading" class="flex justify-center items-center h-64">
      <div class="text-gray-500 text-sm">{{ t('common.loading') }}...</div>
    </div>
    
    <Tree
      v-else-if="treeData.length"
      v-model:selectedKeys="selectedParentIds"
      :treeData="treeData"
      blockNode
      class="h-100 overflow-auto"
      defaultExpandAll
      :fieldNames="{
        children: 'children',
        title: 'name',
        key: 'id'
      }">
      <!-- Custom tree node template -->
      <template #title="{name, id, disabled}">
        <div 
          class="flex items-center space-x-2"
          :class="{ 'text-gray-400 cursor-not-allowed': disabled }">
          <Icon 
            v-if="id !== '-1'" 
            icon="icon-mokuai" 
            :class="{ 'text-gray-300': disabled, 'text-gray-500': !disabled }" />
          <span 
            class="flex-1" 
            :class="{ 'text-gray-400': disabled, 'text-gray-800': !disabled }"
            :title="disabled ? t('project.projectEdit.module.cannotSelectAsParent') : name">
            {{ name }}
          </span>
        </div>
      </template>
    </Tree>
    
    <div v-else class="text-center text-gray-500 py-8">
      {{ t('project.projectEdit.module.noModulesAvailable') }}
    </div>
  </Modal>
</template>
