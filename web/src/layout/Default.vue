<script setup lang="ts">
import { defineAsyncComponent, onMounted, provide, watch } from 'vue';
import { Dropdown } from 'ant-design-vue';
import {
  Breadcrumb,
  HeaderLanguagePreference,
  HeaderLogo,
  HeaderMessage,
  HeaderNavigator,
  HeaderPersonalCenter,
  HeaderSearch,
  Icon,
  IconRefresh,
  Image,
  Input,
  Notice,
  Spin
} from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { ProjectType } from '@/enums/enums';

// Composables
import { useProjectData } from './composables/useProjectData';
import { useDropdownState } from './composables/useDropdownState';
import { useMenuConfiguration } from './composables/useMenuConfiguration';
import { useLayoutState } from './composables/useLayoutState';

// Async components
const AddProjectModal = defineAsyncComponent(() => import('@/views/project/project/AddModal.vue'));
const HeaderMenu = defineAsyncComponent(() => import('@/layout/menus/Header.vue'));
const ProjectMenu = defineAsyncComponent(() => import('@/layout/menus/Project.vue'));

const { t } = useI18n();

// Use composables
const {
  loading,
  projectSearchState,
  currentProject,
  addProjectVisible,
  userInfo,
  loadProjectData,
  selectProject,
  changeProjectInfo,
  searchProjects,
  handleProjectTypeChange,
  projectTypeVisibility
} = useProjectData();

const {
  dropdownState,
  handleMouseEnter,
  handleMouseLeave,
  toggleDropdown,
  handleProjectSelection
} = useDropdownState();

const {
  headerMenus,
  projectMenus,
  appFunctionCodeMap
} = useMenuConfiguration(projectTypeVisibility);

const {
  appInfo,
  logoDefaultImg,
  mainContentStyle
} = useLayoutState();

/**
 * Handle project dropdown input change
 */
const handleDropdownInputChange = (event: any): void => {
  searchProjects(event.target.value);
};

/**
 * Handle project click with dropdown state management
 */
const handleProjectClick = (project: any): void => {
  selectProject(project);
  handleProjectSelection();
};

/**
 * Provide global application info
 */
const provideAppContext = (): void => {
  provide('proTypeShowMap', projectTypeVisibility.value);
  provide('projectInfo', currentProject);
  provide('changeProjectInfo', changeProjectInfo);
  provide('getNewCurrentProject', loadProjectData);
};

// Watch for project type changes
watch(() => currentProject.value?.type?.value, (newValue, oldValue) => {
  if ((newValue === ProjectType.TESTING) && (oldValue !== ProjectType.TESTING)) {
    handleProjectTypeChange();
  }
}, {
  immediate: true,
  deep: true
});

// Watch for user info changes and load project data
onMounted(async () => {
  watch(() => userInfo.value, async () => {
    if (!userInfo.value?.id) {
      return;
    }
    await loadProjectData();
  }, { immediate: true });
});

provideAppContext();
</script>
<template>
  <div class="shadow relative z-99 flex items-center h-13.5 pr-3 header-nav-bg">
    <HeaderNavigator class="flex-shrink-0 !w-11" />
    <HeaderLogo
      :appId="appInfo?.id"
      :defaultImg="logoDefaultImg"
      class="mr-3.5" />
    <div class="w-0.5 h-3.5 mr-3.5 bg-slate-200 rounded"></div>
    <div v-if="!!projectMenus.length" class="flex items-center space-x-3.5 mr-3.5">
      <ProjectMenu :menus="projectMenus" />
      <div class="w-0.5 h-3.5 bg-slate-200 rounded"></div>
    </div>
    <Dropdown :visible="dropdownState.visible">
      <template #overlay>
        <Spin
          :spinning="loading"
          class="header-menu-dropdown-content text-theme-content"
          @mouseenter="handleMouseEnter"
          @mouseleave="handleMouseLeave">
          <div class="px-3.5 mb-1">
            <div class="flex items-center mb-2.5">
              <Input
                :placeholder="t('project.searchProject')"
                trim
                allowClear
                @change="handleDropdownInputChange" />
              <IconRefresh
                :loading="loading"
                class="ml-2 text-3.5"
                @click="loadProjectData" />
            </div>
            <span class="text-theme-sub-content">{{ t('project.myJoinedProjects') }}</span>
          </div>

          <div style="height:248px;margin: 0 4px;overflow: auto;">
            <div
              v-for="item in projectSearchState.filteredList"
              :key="item.id"
              :class="{ checked: item.id === currentProject?.id }"
              class="header-menu-dropdown-content-item flex items-center overflow-hidden px-3.5 py-1.5 cursor-pointer rounded mb-1 last:mb-0"
              @click="handleProjectClick(item)">
              <div class="flex items-center flex-shrink-0 w-5 h-5 mr-2 rounded-sm overflow-hidden">
                <Image
                  :src="item.avatar"
                  :defaultImg="logoDefaultImg"
                  class="w-full" />
              </div>
              <div class="flex-1 truncate">{{ item.name }}</div>
              <Icon
                v-show="item.id === currentProject?.id"
                icon="icon-duihaolv"
                class="text-3.5" />
            </div>
          </div>
        </Spin>
      </template>
      <div
        class="flex items-center text-3.5 max-w-50 mr-3.5">
        <div :title="currentProject?.name" class="truncate">{{ currentProject?.name }}</div>
        <div
          class="flex-shrink-0 flex items-center justify-center w-6 h-6 text-theme-text-hover cursor-pointer"
          @click.stop.prevent="toggleDropdown"
          @mouseenter="handleMouseEnter"
          @mouseleave="handleMouseLeave">
          <Icon icon="icon-xiala" />
        </div>
      </div>
    </Dropdown>
    <HeaderMenu :menus="headerMenus" class="mr-3.5 flex-shrink flex-grow basis-0 layout-header-menu" />
    <HeaderSearch
      v-if="appFunctionCodeMap.get('GlobalSearch')?.hasAuth"
      style="width:170px;"
      class="mr-5 flex-shrink-0 flex-grow-0 basis-auto" />
    <template v-if="appFunctionCodeMap.get('Expense')?.hasAuth">
      <a
        :href="appFunctionCodeMap.get('Expense')?.url"
        target="_blank"
        class="flex items-center mr-5 header-item-text-normal header-item-text-active">
        {{ appFunctionCodeMap.get('Expense')?.showName }}
      </a>
    </template>
    <template v-if="appFunctionCodeMap.get('Ticket')?.hasAuth">
      <a
        :href="appFunctionCodeMap.get('Ticket')?.url"
        target="_blank"
        class="flex items-center mr-5 header-item-text-normal header-item-text-active">
        {{ appFunctionCodeMap.get('Ticket')?.showName }}
      </a>
    </template>
    <template v-if="appFunctionCodeMap.get('OfficialWebsite')?.hasAuth">
      <a
        :href="appFunctionCodeMap.get('OfficialWebsite')?.url"
        target="_blank"
        class="flex items-center mr-5 header-item-text-normal header-item-text-active">
        {{ appFunctionCodeMap.get('OfficialWebsite')?.showName }}
      </a>
    </template>
    <HeaderLanguagePreference class="px-3" />
    <HeaderMessage class="mr-5 flex-shrink-0 flex-grow-0 basis-auto" />
    <HeaderPersonalCenter class="flex-shrink-0 flex-grow-0 basis-auto" />
  </div>
  <Notice :appId="appInfo?.id?.toString()" />
  <div style="height: calc(100% - 54px);" class="overflow-hidden">
    <Breadcrumb :route="$route as any" />
    <div :style="mainContentStyle">
      <RouterView />
    </div>
  </div>
  <AddProjectModal
    v-model:visible="addProjectVisible"
    :closable="false"
    @ok="loadProjectData" />
</template>
<style scoped>
.shadow {
  box-shadow: 1px 0 4px 0 rgba(0, 0, 0, 10%);
}
</style>
<style>
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
