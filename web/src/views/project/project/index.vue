<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, onMounted, provide, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { appContext } from '@xcan-angus/infra';

const { t } = useI18n();

const projectHome = defineAsyncComponent(() => import('@/views/project/project/list/index.vue'));
const EditProject = defineAsyncComponent(() => import('@/views/project/project/edit/index.vue'));
const ProjectDetail = defineAsyncComponent(() => import('@/views/project/project/detail/index.vue'));

const userInfo = ref(appContext.getUser());
const projectInfo = inject('projectInfo', ref({ id: '' }));
const browserTabRef = ref();
const projectHomeRef = ref();

onMounted(() => {
  watch([() => browserTabRef.value, () => userInfo.value], () => {
    if (browserTabRef.value && userInfo.value?.id) {
      if (typeof browserTabRef.value?.update === 'function') {
        const tabData = browserTabRef.value.getData().map(item => item.type);
        if (!tabData.includes('projecthome')) {
          browserTabRef.value.add(() => ({
            _id: 'project_home',
            name: t('project.project'),
            type: 'projecthome',
            closable: false,
            icon: 'icon-zhuye',
            value: 'projecthome'
          }));
        }
      }
    }
  }, {
    immediate: true
  });
});

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

const STORAGE_KEY = computed(() => {
  return `${userInfo.value?.id}_project_browser`;
});

const refreshProList = () => {
  if (projectHomeRef.value && typeof projectHomeRef.value.refresh === 'function') {
    projectHomeRef.value.refresh();
  }
};

provide('addTabPane', addTabPane);
provide('delTabPane', delTabPane);

</script>
<template>
  <BrowserTab
    v-if="userInfo?.id"
    ref="browserTabRef"
    :key="STORAGE_KEY"
    hideAdd
    class="h-full text-3"
    size="small"
    :storageKey="STORAGE_KEY">
    <template #default="record">
      <template v-if="record.type === 'projecthome'">
        <projectHome
          ref="projectHomeRef"
          :userInfo="userInfo"
          :projectId="projectInfo?.id"
          @delOk="delTabPane" />
      </template>
      <template v-if="record.type === 'project'">
        <EditProject
          :projectId="record.id"
          :data="record.data"
          @ok="refreshProList" />
      </template>
      <template v-if="record.type === 'projectDetail'">
        <ProjectDetail :projectId="record.id" :data="record.data" />
      </template>
    </template>
  </BrowserTab>
</template>

<style scoped>
/* .ant-tabs-small>:deep(.ant-tabs-nav) .ant-tabs-tab {
  font-size: 14px;
} */

.w-right {
  width: 400px;
}

@media screen and (min-width: 1280px) and (max-width: 1480px) {
  .w-right {
    width: 330px;
  }
}

@media screen and (min-width: 1481px) and (max-width: 1800px) {
  .w-right {
    width: 350px;
  }
}
</style>
