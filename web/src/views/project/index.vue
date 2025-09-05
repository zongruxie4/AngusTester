<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, Ref, ref } from 'vue';
import { appContext } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

import LeftMenu from '@/components/layout/leftMenu/index.vue';

const { t } = useI18n();
const Projects = defineAsyncComponent(() => import('@/views/project/project/index.vue'));
const Trash = defineAsyncComponent(() => import('@/views/project/trash/index.vue'));
const Activity = defineAsyncComponent(() => import('@/views/project/activity/index.vue'));
const Module = defineAsyncComponent(() => import('@/views/project/module/index.vue'));
const Tags = defineAsyncComponent(() => import('@/views/project/tag/index.vue'));
const Version = defineAsyncComponent(() => import('@/views/project/version/index.vue'));

const userInfo = ref(appContext.getUser());
const projectInfo = inject<Ref<{ id: string; avatar: string; name: string; }>>('projectInfo', ref({ id: '', avatar: '', name: '' }));
const appInfo = ref(appContext.getAccessApp());
const aiAgent = inject('aiAgent', ref({ chatIframe: '' }));
const aiEnabled = inject('aiEnabled', ref(false));

const projectId = computed(() => {
  return projectInfo.value?.id;
});

const iframeSrc = computed(() => {
  const match = aiAgent.value?.chatIframe.match(/src=['"]([^'"]+)['"]/);
  if (match) {
    const srcValue = match[1];
    return srcValue;
  } else {
    return '';
  }
});

const menuItems = computed(() => {
  return [
    { icon: 'icon-xiangmu', name: t('project.name'), key: 'project' },
    aiEnabled.value && { icon: 'icon-AIzhushou', name: t('AI.name'), key: 'AI' },
    projectId.value && { icon: 'icon-banben1', name: t('project.projectDetail.tabs.version'), key: 'version' },
    projectId.value && { icon: 'icon-mokuai1', name: t('project.projectDetail.tabs.module'), key: 'module' },
    projectId.value && { icon: 'icon-biaoqian3', name: t('project.projectDetail.tabs.tag'), key: 'tags' },
    { icon: 'icon-fabu', name: t('projectActivity.name'), key: 'activity' },
    { icon: 'icon-qingchu', name: t('projectTrash.name'), key: 'trash' }
  ].filter(Boolean) as { icon: string; name: string; key: string; }[];
});

</script>
<template>
  <LeftMenu v-if="projectId" :menuItems="menuItems">
    <template #project>
      <Projects
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo" />
    </template>

    <template #AI>
      <div class="w-full h-full px-2.5">
        <iframe
          width="100%"
          height="100%"
          :src="iframeSrc" />
      </div>
    </template>

    <template #trash>
      <trash
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo" />
    </template>

    <template #activity>
      <Activity class="overflow-auto h-full" />
    </template>

    <template #module>
      <Module
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        class="p-5" />
    </template>

    <template #version>
      <Version
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        class="p-5" />
    </template>

    <template #tags>
      <Tags
        :projectId="projectId"
        :userInfo="userInfo"
        :appInfo="appInfo"
        class="p-5" />
    </template>
  </LeftMenu>
</template>
