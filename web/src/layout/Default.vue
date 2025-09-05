<script setup lang="ts">
import { defineAsyncComponent, onMounted, provide, watch } from 'vue';
import {
  Breadcrumb,
  HeaderLanguagePreference,
  HeaderLogo,
  HeaderMessage,
  HeaderNavigator,
  HeaderPersonalCenter,
  HeaderSearch,
  Notice
} from '@xcan-angus/vue-ui';
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
const ProjectDropdown = defineAsyncComponent(() => import('@/layout/menus/ProjectDropdown.vue'));

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
  defaultAppIcon,
  defaultProjectImg,
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
  provide('proTypeShowMap', projectTypeVisibility);
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
      :defaultImg="defaultAppIcon"
      class="mr-3.5" />
    <div class="w-0.5 h-3.5 mr-3.5 bg-slate-200 rounded"></div>
    <div v-if="!!projectMenus.length" class="flex items-center space-x-3.5 mr-3.5">
      <ProjectMenu :menus="projectMenus" />
      <div class="w-0.5 h-3.5 bg-slate-200 rounded"></div>
    </div>
    <ProjectDropdown
      :dropdownState="dropdownState"
      :loading="loading"
      :projectSearchState="projectSearchState"
      :currentProject="currentProject"
      :defaultProjectImg="defaultProjectImg"
      @mouseenter="handleMouseEnter"
      @mouseleave="handleMouseLeave"
      @toggle="toggleDropdown"
      @search="handleDropdownInputChange"
      @refresh="loadProjectData"
      @select="handleProjectClick" />
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
