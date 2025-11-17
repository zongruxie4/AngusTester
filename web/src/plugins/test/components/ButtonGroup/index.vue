
<script setup lang="ts">
import { computed } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { ButtonGroupMenuItem } from './PropsType';

/**
 * Component props interface
 */
export interface Props {
  hideKeys: Set<string>; // Set of button keys to hide from the button group
  debugLoading?: boolean;
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  hideKeys: () => new Set<string>(),
  debugLoading: false
});

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Event emitter for button click events
 */
const emit = defineEmits<{
  (e: 'click', value: ButtonGroupMenuItem): void;
}>();

/**
 * Handle button click event
 * @param value - The clicked button menu item
 */
const click = (value: ButtonGroupMenuItem) => {
  emit('click', value);
};

/**
 * All available menu items with their icons and i18n keys
 * Computed property to support dynamic i18n updates
 */
const menuItems = computed<readonly ButtonGroupMenuItem[]>(() => [
  { name: t('actions.export'), icon: 'icon-daochu', key: 'export' },
  { name: t('actions.select'), icon: 'icon-shengchengceshijiaoben', key: 'select' },
  { name: t('actions.import'), icon: 'icon-daoru', key: 'import' },
  { name: t('views.codeView'), icon: 'icon-daimashitu', key: 'codeView' },
  { name: t('views.pageView'), icon: 'icon-yemianshitu', key: 'pageView' },
  { name: t('actions.permission'), icon: 'icon-quanxian1', key: 'permission' },
  { name: t('actions.addFollow'), icon: 'icon-yiguanzhu', key: 'follow' },
  { name: t('actions.cancelFollow'), icon: 'icon-quxiaoguanzhu', key: 'cancelFollow' },
  { name: t('actions.addFavourite'), icon: 'icon-yishoucang', key: 'favourite' },
  { name: t('actions.cancelFavourite'), icon: 'icon-quxiaoshoucang', key: 'cancelFavourite' },
  { name: t('actions.refresh'), icon: 'icon-shuaxin', key: 'refresh' },
  { name: t('httpPlugin.actions.createExecution'), icon: 'icon-tiaoshi', key: 'test' },
  { name: t('actions.debug'), icon: 'icon-tiaoshi', key: 'debug' }
]);

/**
 * Filtered button items based on hideKeys prop
 * Returns only buttons that are not in the hideKeys set
 */
const buttonItems = computed(() => {
  const hideKeys = props.hideKeys;
  return menuItems.value.filter(item => !hideKeys.has(item.key));
});


const getLoading = (key: string) => {
  if (key === 'debug') {
    return props.debugLoading;
  }
  return false;
};

/**
 * Save button configuration
 * Computed property to support dynamic i18n updates
 */
const saveItem = computed<ButtonGroupMenuItem>(() => ({
  name: t('actions.save'),
  icon: 'icon-baocun',
  key: 'save'
}));
</script>

<template>
  <!-- Button group container with flexbox layout -->
  <div class="flex items-center justify-end flex-nowrap space-x-2">
    <!-- Render filtered button items with dividers -->
    <template v-for="(item, index) in buttonItems" :key="item.key">
      <!-- Vertical divider between buttons (skip for first button) -->
      <div v-if="index > 0" class="h-3 w-0 border-r border-solid border-theme-divider"></div>
      
      <!-- Action button (link style) -->
      <Button
        type="link"
        size="small"
        :loading="getLoading(item.key)"
        @click="click(item)">
        <div class="flex items-center space-x-1">
          <Icon :icon="item.icon" class="text-3.5" />
          <span>{{ item.name }}</span>
        </div>
      </Button>
    </template>
    
    <!-- Primary save button (always visible) -->
    <Button
      type="primary"
      size="small"
      @click="click(saveItem)">
      <div class="flex items-center space-x-1">
        <Icon :icon="saveItem.icon" class="text-3.5" />
        <span>{{ saveItem.name }}</span>
      </div>
    </Button>
  </div>
</template>

<style scoped>

.ant-btn-link {
  padding: 0;
  color: var(--content-text-content);
}

.ant-btn-link:hover {
  color: var(--content-special-text-hover);
}
</style>
