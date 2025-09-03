<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { Dropdown } from 'ant-design-vue';
import { useRoute, useRouter } from 'vue-router';
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
  notification,
  Spin
} from '@xcan-angus/vue-ui';
import { appContext, duration, localStore } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { project } from '@/api/tester';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

type ProjectInfo = {
  id: string;
  avatar: string;
  name: string;
  ownerId: string;
  description: string;
  createdBy: string;
  createdDate: string;
  lastModifiedBy: string;
  lastModifiedDate: string;
}

const AddProjectModal = defineAsyncComponent(() => import('@/views/project/project/addModal/index.vue'));
const HeaderMenu = defineAsyncComponent(() => import('@/layout/headerMenu/index.vue'));
const ProjectMenu = defineAsyncComponent(() => import('@/layout/projectMenu/index.vue'));

const appInfo = ref(appContext.getContext().accessApp);
// const appInfo = inject<Ref<{ appId: string; id: string; }>>('appInfo');
// const userInfo = inject<Ref<{ id: string; }>>('tenantInfo', ref({ id: '' }));
const userInfo = ref(appContext.getContext().user);
const codeMap = appContext.getAccessAppFuncCodeMap();
const route = useRoute();
const router = useRouter();
const logoDefaultImg = new URL('./assets/AngusTester.png', import.meta.url).href;

const PROJECT_ID_STORE_KEY = window.btoa('GPID');

const addProjectVisible = ref(false); // 添加项目弹窗visible
const loading = ref(false);

const projectList = ref<ProjectInfo[]>([]);
const showProjectList = ref<ProjectInfo[]>([]);
const projectInfo = ref<{ id: string; avatar: string; name: string; createdBy: string; ownerId: string, type: {value: 'AGILE'|'GENERAL'|'TESTING'} }>();
const dropdownVisible = ref(false);
let timer: NodeJS.Timeout;

const loadData = async () => {
  loading.value = true;
  const [error, res] = await project.getJoinedProject(userInfo.value?.id);
  loading.value = false;
  if (error) {
    return;
  }

  const list = res?.data || [];
  projectList.value = list;
  showProjectList.value = list;
  if (list.length < 1) {
    notification.warning(t('project.noJoinedProjects'));
    projectInfo.value = undefined;
    addProjectVisible.value = true;
    return;
  }

  const localId = localStore.get(PROJECT_ID_STORE_KEY);

  if (route.query?.projectId && localId !== route.query?.projectId) {
    const targetProject = list.find(item => item.id === route.query.projectId);
    if (targetProject) {
      projectClick(targetProject);
      return;
    } else {
      notification.error(t('project.projectNotExists'));
    }
  }
  const localTarget = list.find(item => item.id === localId);
  if (localTarget) {
    projectInfo.value = list.find(item => item.id === localId);
  } else {
    projectClick(list[0]);
  }
};

const dropdownInputChange = debounce(duration.search, (event: { target: { value: string } }) => {
  const value = event.target.value;
  showProjectList.value = projectList.value.filter(item => item.name.includes(value));
});

const projectClick = (data: ProjectInfo) => {
  const { id, avatar, name, createdBy, ownerId, type } = data;
  projectInfo.value = { id, avatar, name, createdBy, ownerId, type };
  localStore.set(PROJECT_ID_STORE_KEY, id);
  clearTimeout(timer);
  timer = setTimeout(() => {
    dropdownVisible.value = false;
  }, 100);
};

const changeProjectInfo = async (projectId?: string, force = false) => {
  if (!force) {
    const target = projectList.value.find(i => i.id === projectId);
    if (target) {
      projectClick(target);
      return;
    } else {
      localStore.remove(PROJECT_ID_STORE_KEY);
      loadData();
      return;
    }
  }

  const [error, { data }] = await project.getProjectDetail(projectId);
  if (error) {
    notification.warning(t('project.projectNotFound'));
    loadData();
    return;
  }

  const { avatar, id, name, createdBy, ownerId, type } = data;
  projectInfo.value = { avatar, id, name, createdBy, ownerId, type };
};

const mouseenter = () => {
  clearTimeout(timer);
  timer = setTimeout(() => {
    dropdownVisible.value = true;
  }, 300);
};

const mouseleave = () => {
  clearTimeout(timer);
  timer = setTimeout(() => {
    dropdownVisible.value = false;
  }, 300);
};

const toggle = () => {
  clearTimeout(timer);
  dropdownVisible.value = !dropdownVisible.value;
};

onMounted(async () => {
  watch(() => userInfo.value, () => {
    if (!userInfo.value?.id) {
      return;
    }

    loadData();
  }, { immediate: true });
});

const headerMenus = computed(() => {
  const menuList = appContext.getAccessAppFuncTree();

  return menuList?.filter(item => !['Projects', 'Config', !proTypeShowMap.value?.showTask ? 'Task' : ''].includes(item.code)) || [];
});

const projectMenus = computed(() => {
  const menuList = appContext.getAccessAppFuncTree();
  return menuList?.filter(item => ['Projects', 'Config'].includes(item.code)) || [];
});

const hasBreadcrumb = computed(() => {
  return !!(route.meta?.breadcrumb as Array<unknown>)?.length;
});

const style = computed(() => {
  return hasBreadcrumb.value ? 'height: calc(100% - 43px);' : 'height: 100%;';
});

const proTypeShowMap = computed(() => {
  if (projectInfo.value?.type?.value === 'TESTING') {
    return {
      showTask: false,
      showBackLog: false,
      showSprint: false,
      showMeeting: false,
      showTaskStatistics: false
    };
  }

  if (projectInfo.value?.type?.value === 'GENERAL') {
    return {
      showTask: true,
      showBackLog: false,
      showSprint: false,
      showMeeting: false,
      showTaskStatistics: false
    };
  }

  return {
    showTask: true,
    showBackLog: true,
    showSprint: true,
    showMeeting: true,
    showTaskStatistics: true
  };
});

watch(() => projectInfo.value?.type?.value, (newValue, oldValue) => {
  if ((newValue === 'TESTING') && (oldValue !== 'TESTING')) {
    if (route.path.includes('task')) {
      router.replace('/project');
    }
  }
}, {
  immediate: true,
  deep: true
});

provide('proTypeShowMap', proTypeShowMap);
provide('projectInfo', projectInfo);
provide('changeProjectInfo', changeProjectInfo);
provide('getNewCurrentProject', loadData);

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
    <Dropdown :visible="dropdownVisible">
      <template #overlay>
        <Spin
          :spinning="loading"
          class="header-menu-dropdown-content text-theme-content"
          @mouseenter="mouseenter"
          @mouseleave="mouseleave">
            <div class="px-3.5 mb-1">
              <div class="flex items-center mb-2.5">
                <Input
                  :placeholder="t('project.searchProject')"
                  trim
                  allowClear
                  @change="dropdownInputChange" />
                <IconRefresh
                  :loading="loading"
                  class="ml-2 text-3.5"
                  @click="loadData" />
              </div>
              <span class="text-theme-sub-content">{{ t('project.myJoinedProjects') }}</span>
            </div>

          <div style="height:248px;margin: 0 4px;overflow: auto;">
            <div
              v-for="item in showProjectList"
              :key="item.id"
              :class="{ checked: item.id === projectInfo?.id }"
              class="header-menu-dropdown-content-item flex items-center overflow-hidden px-3.5 py-1.5 cursor-pointer rounded mb-1 last:mb-0"
              @click="projectClick(item)">
              <div class="flex items-center flex-shrink-0 w-5 h-5 mr-2 rounded-sm overflow-hidden">
                <Image
                  :src="item.avatar"
                  :defaultImg="logoDefaultImg"
                  class="w-full" />
              </div>
              <div class="flex-1 truncate">{{ item.name }}</div>
              <Icon
                v-show="item.id === projectInfo?.id"
                icon="icon-duihaolv"
                class="text-3.5" />
            </div>
          </div>
        </Spin>
      </template>
      <div
        class="flex items-center text-3.5 max-w-50 mr-3.5">
        <div :title="projectInfo?.name" class="truncate">{{ projectInfo?.name }}</div>
        <div
          class="flex-shrink-0 flex items-center justify-center w-6 h-6 text-theme-text-hover cursor-pointer"
          @click.stop.prevent="toggle"
          @mouseenter="mouseenter"
          @mouseleave="mouseleave">
          <Icon icon="icon-xiala" />
        </div>
      </div>
    </Dropdown>
    <HeaderMenu :menus="headerMenus" class="mr-3.5 flex-shrink flex-grow basis-0 layout-header-menu" />
    <HeaderSearch
      v-if="codeMap.get('SearchBar')?.hasAuth"
      style="width:170px;"
      class="mr-5 flex-shrink-0 flex-grow-0 basis-auto" />
    <template v-if="codeMap.get('Expense')?.hasAuth">
      <a
        :href="codeMap.get('Expense')?.url"
        target="_blank"
        class="flex items-center mr-5 header-item-text-normal header-item-text-active">
        {{ codeMap.get('Expense')?.showName }}
      </a>
    </template>
    <template v-if="codeMap.get('WorkOrder')?.hasAuth">
      <a
        :href="codeMap.get('WorkOrder')?.url"
        target="_blank"
        class="flex items-center mr-5 header-item-text-normal header-item-text-active">
        {{ codeMap.get('WorkOrder')?.showName }}
      </a>
    </template>
    <template v-if="codeMap.get('OfficialWebsite')?.hasAuth">
      <a
        :href="codeMap.get('OfficialWebsite')?.url"
        target="_blank"
        class="flex items-center mr-5 header-item-text-normal header-item-text-active">
        {{ codeMap.get('OfficialWebsite')?.showName }}
      </a>
    </template>
    <HeaderLanguagePreference class="px-3" />
    <HeaderMessage class="mr-5 flex-shrink-0 flex-grow-0 basis-auto" />
    <HeaderPersonalCenter class="flex-shrink-0 flex-grow-0 basis-auto" />
  </div>
  <Notice :appId="appInfo?.id" />
  <div style="height: calc(100% - 54px);" class="overflow-hidden">
    <Breadcrumb :route="route" />
    <div :style="style">
      <RouterView />
    </div>
  </div>
  <AddProjectModal
    v-model:visible="addProjectVisible"
    :closable="false"
    @ok="loadData" />
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
