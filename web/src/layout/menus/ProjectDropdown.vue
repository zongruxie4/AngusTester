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
  logoDefaultImg: string;
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
    @visibleChange="handleVisibleChange">
    <div
      class="flex items-center text-3.5 max-w-50 mr-3.5 cursor-pointer"
      @click.stop.prevent="toggleDropdown"
      @mouseenter="handleMouseEnter"
      @mouseleave="handleMouseLeave">
      <div :title="props.currentProject?.name" class="truncate">{{ props.currentProject?.name }}</div>
      <div
        class="flex-shrink-0 flex items-center justify-center w-6 h-6 text-theme-text-hover">
        <Icon icon="icon-xiala" />
      </div>
    </div>
    <template #overlay>
      <Spin
        :spinning="props.loading"
        class="header-menu-dropdown-content text-theme-content"
        @mouseenter="handleMouseEnter"
        @mouseleave="handleMouseLeave">
        <div class="px-3.5 mb-1">
          <div class="flex items-center mb-2.5">
            <Input
              :placeholder="t('project.searchProject')"
              trim
              allowClear
              @input="handleDropdownInputChange" />
            <IconRefresh
              :loading="props.loading"
              class="ml-2 text-3.5"
              @click="emit('refresh')" />
          </div>
          <span class="text-theme-sub-content">{{ t('project.myJoinedProjects') }}</span>
        </div>

        <div style="height:248px;margin: 0 4px;overflow: auto;">
          <div
            v-for="item in props.projectSearchState.filteredList"
            :key="item.id"
            :class="{ checked: item.id === props.currentProject?.id }"
            class="header-menu-dropdown-content-item flex items-center overflow-hidden px-3.5 py-1.5 cursor-pointer rounded mb-1 last:mb-0"
            @click="handleProjectClick(item)">
            <div class="flex items-center flex-shrink-0 w-5 h-5 mr-2 rounded-sm overflow-hidden">
              <Image
                :src="item.avatar"
                :defaultImg="props.logoDefaultImg"
                class="w-full" />
            </div>
            <div class="flex-1 truncate">{{ item.name }}</div>
            <Icon
              v-show="item.id === props.currentProject?.id"
              icon="icon-duihaolv"
              class="text-3.5" />
          </div>
        </div>
      </Spin>
    </template>
  </Dropdown>
</template>

<style scoped>
.header-menu-dropdown-content {
  width: 350px;
  padding: 14px 0 8px;
  border-radius: 2px;
  background-color: #fff;
  box-shadow: 0 3px 6px -4px #0000001f, 0 6px 16px #00000014, 0 9px 28px 8px #0000000d;
  line-height: 20px;
}

.header-menu-dropdown-content .header-menu-dropdown-content-item.checked {
  background-color: var(--content-tabs-bg-hover);
  color: var(--content-special-text-hover);
}

.header-menu-dropdown-content .header-menu-dropdown-content-item:hover {
  background-color: #f5f5f5;
}
</style>
