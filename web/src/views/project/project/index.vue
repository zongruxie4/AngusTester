<script lang="ts" setup>
// Vue composition API imports
import { computed, defineAsyncComponent, inject, onMounted, provide, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { BrowserTab } from '@xcan-angus/vue-ui';
import { appContext } from '@xcan-angus/infra';

// Initialize i18n
const { t } = useI18n();

// Type constants
const PROJECT_PAGE_TYPES = {
  PROJECT_HOME: 'projecthome',
  PROJECT: 'project',
  PROJECT_DETAIL: 'projectDetail'
} as const;

// Async component definitions
const ProjectList = defineAsyncComponent(() => import('@/views/project/project/List.vue'));
const EditProject = defineAsyncComponent(() => import('@/views/project/project/Edit.vue'));
const ProjectDetail = defineAsyncComponent(() => import('@/views/project/project/Detail.vue'));

// Reactive data setup
const userInfo = ref(appContext.getUser());
// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));
const browserTabRef = ref();
const projectHomeRef = ref();

// Computed properties
const STORAGE_KEY = computed(() => {
  return `${userInfo.value?.id}_project_browser`;
});

// Transform userInfo to expected format
const transformedUserInfo = computed(() => {
  if (!userInfo.value?.id) {
    return undefined;
  }
  return {
    ...userInfo.value,
    id: String(userInfo.value.id)
  };
});

// Project tab data
const getTabData = () => {
  return {
    _id: 'project_home',
    name: t('project.project'),
    type: PROJECT_PAGE_TYPES.PROJECT_HOME,
    closable: false,
    icon: 'icon-zhuye',
    value: PROJECT_PAGE_TYPES.PROJECT_HOME
  };
};

// Tab management functions
const addTabPane = (data) => {
  browserTabRef.value.add(() => {
    return { ...data };
  });
};

const delTabPane = (keys: string[]) => {
  if (typeof browserTabRef.value?.remove === 'function') {
    browserTabRef.value.remove(keys);
  }
};

// Project list refresh function
const refreshList = () => {
  if (projectHomeRef.value && typeof projectHomeRef.value.refresh === 'function') {
    projectHomeRef.value.refresh();
  }
};

// Lifecycle hooks
onMounted(() => {
  watch([() => browserTabRef.value, () => userInfo.value], () => {
    if (browserTabRef.value && userInfo.value?.id) {
      if (typeof browserTabRef.value?.update === 'function') {
        debugger;
        const tabData = browserTabRef.value.getData().map(item => item.type);
        // Add project home tab if not exists
        if (!tabData.includes(PROJECT_PAGE_TYPES.PROJECT_HOME)) {
          browserTabRef.value.add(getTabData());
        } else {
          browserTabRef.value.update(getTabData());
        }
      }
    }
  }, {
    immediate: true
  });
});

// Provide functions to child components
provide('addTabPane', addTabPane);
provide('delTabPane', delTabPane);
</script>
<template>
  <!-- Main browser tab container for project management -->
  <BrowserTab
    v-if="userInfo?.id"
    ref="browserTabRef"
    :key="STORAGE_KEY"
    hideAdd
    class="h-full text-3"
    size="small"
    :storageKey="STORAGE_KEY">
    <template #default="record">
      <!-- Project home page tab -->
      <template v-if="record.type === PROJECT_PAGE_TYPES.PROJECT_HOME">
        <ProjectList
          ref="projectHomeRef"
          :userInfo="transformedUserInfo"
          :projectId="projectId"
          @delOk="delTabPane" />
      </template>

      <!-- Project edit tab -->
      <template v-if="record.type === PROJECT_PAGE_TYPES.PROJECT">
        <EditProject
          :projectId="record.id"
          :data="record.data"
          @ok="refreshList" />
      </template>

      <!-- Project detail tab -->
      <template v-if="record.type === PROJECT_PAGE_TYPES.PROJECT_DETAIL">
        <ProjectDetail :projectId="record.id" :data="record.data" />
      </template>
    </template>
  </BrowserTab>
</template>

<style scoped>
/* Responsive width adjustments for right panel */
.w-right {
  width: 400px;
}

/* Medium screen adjustments */
@media screen and (min-width: 1280px) and (max-width: 1480px) {
  .w-right {
    width: 330px;
  }
}

/* Large screen adjustments */
@media screen and (min-width: 1481px) and (max-width: 1800px) {
  .w-right {
    width: 350px;
  }
}
</style>
