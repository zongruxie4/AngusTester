<script setup lang="ts">
import { Spin, Input, Icon, IconRefresh, Image } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { Dropdown } from 'ant-design-vue';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';
import type { ProjectInfo, ProjectDisplayInfo, DropdownState, ProjectSearchState } from '../types';

const { t } = useI18n();

interface Props {
  dropdownState: DropdownState;
  loading: boolean;
  projectSearchState: ProjectSearchState;
  currentProject?: ProjectDisplayInfo;
  defaultProjectImg: string;
}

interface Emits {
  (e: 'mouseenter'): void;
  (e: 'mouseleave'): void;
  (e: 'toggle'): void;
  (e: 'search', value: string): void;
  (e: 'refresh'): void;
  (e: 'select', project: ProjectInfo): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

/**
 * Handle dropdown input change with debounce
 */
const handleDropdownInputChange = debounce(duration.search, (event: any): void => {
  emit('search', event);
});

/**
 * Handle project click
 */
const handleProjectClick = (project: ProjectInfo): void => {
  emit('select', project);
};

/**
 * Handle mouse enter event
 */
const handleMouseEnter = (): void => {
  emit('mouseenter');
};

/**
 * Handle mouse leave event
 */
const handleMouseLeave = (): void => {
  emit('mouseleave');
};

/**
 * Toggle dropdown visibility
 */
const toggleDropdown = (): void => {
  emit('toggle');
};

/**
 * Handle dropdown visible change
 */
const handleVisibleChange = (): void => {
  // We control visibility through our own state management
  // This is just to prevent default behavior if needed
};
</script>

<template>
  <Dropdown
    :visible="props.dropdownState.visible"
    :trigger="['click']"
    @visibleChange="handleVisibleChange">
    <div
      class="flex items-center text-3.5 max-w-50 mr-3.5 cursor-pointer project-dropdown-trigger"
      @click.stop.prevent="toggleDropdown"
      @mouseenter="handleMouseEnter"
      @mouseleave="handleMouseLeave">
      <div :title="props.currentProject?.name" class="truncate project-name">{{ props.currentProject?.name }}</div>
      <div
        class="flex-shrink-0 flex items-center justify-center w-6 h-6 text-theme-text-hover project-dropdown-icon">
        <Icon icon="icon-xiala" />
      </div>
    </div>
    <template #overlay>
      <Spin
        :spinning="props.loading"
        class="header-menu-dropdown-content text-theme-content"
        @mouseenter="handleMouseEnter"
        @mouseleave="handleMouseLeave">
        <div class="px-4 py-3">
          <div class="flex items-center mb-3 search-container">
            <Input
              :placeholder="t('project.searchProject')"
              trim
              allowClear
              class="flex-1 search-input"
              @input="handleDropdownInputChange" />
            <IconRefresh
              :loading="props.loading"
              class="ml-2 text-3.5 refresh-icon"
              @click="emit('refresh')" />
          </div>
          <div class="section-title text-theme-sub-content mb-2">{{ t('project.myJoinedProjects') }}</div>
        </div>

        <div class="project-list-container">
          <div
            v-for="item in props.projectSearchState.filteredList"
            :key="item.id"
            :class="{ 'checked': item.id === props.currentProject?.id }"
            class="project-item flex items-center overflow-hidden px-4 py-2 cursor-pointer rounded mb-1 last:mb-0"
            @click="handleProjectClick(item)">
            <div class="flex items-center flex-shrink-0 w-6 h-6 mr-3 rounded-sm overflow-hidden project-avatar">
              <Image
                :src="item.avatar"
                :defaultImg="props.defaultProjectImg"
                class="w-full h-full object-cover" />
            </div>
            <div class="flex-1 truncate project-name">{{ item.name }}</div>
            <Icon
              v-show="item.id === props.currentProject?.id"
              icon="icon-duihaolv"
              class="text-3.5 text-success-icon" />
          </div>
        </div>
      </Spin>
    </template>
  </Dropdown>
</template>

<style scoped>
.project-dropdown-trigger {
  transition: all 0.2s ease;
  padding: 4px 8px;
  border-radius: 4px;
  background-color: rgba(245, 245, 245, 0.8);
  backdrop-filter: blur(8px);
}

.project-dropdown-trigger:hover {
  background-color: rgba(232, 232, 232, 0.9);
}

.header-menu-dropdown-content {
  width: 360px;
  border-radius: 6px;
  background-color: #fff;
  box-shadow: 0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 3px 6px -4px rgba(0, 0, 0, 0.12), 0 9px 28px 8px rgba(0, 0, 0, 0.05);
  line-height: 1.5;
  overflow: hidden;
}

.search-container {
  padding: 0 4px;
}

.section-title {
  font-size: 12px;
  padding: 0 8px;
  font-weight: 500;
}

.project-list-container {
  max-height: 280px;
  overflow-y: auto;
  padding: 0 4px 4px;
  margin: 0 4px;
}

.project-item {
  transition: all 0.2s ease;
  border-radius: 6px;
}

.project-item:hover {
  background-color: #f5f5f5;
}

.project-item.checked {
  background-color: var(--content-tabs-bg-hover);
}

.project-name {
  color: var(--content-special-text-hover);
  font-weight: 500;
}

.project-avatar {
  border-radius: 4px;
}

.refresh-icon {
  cursor: pointer;
  transition: all 0.2s ease;
  padding: 4px;
  border-radius: 4px;
}

.refresh-icon:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

.text-success-icon {
  color: #52c41a;
}
</style>
