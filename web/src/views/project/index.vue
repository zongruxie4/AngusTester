<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, Ref, ref } from 'vue';
import { appContext } from '@xcan-angus/infra';

import LeftMenu from '@/components/layout/leftMenu/index.vue';

const Projects = defineAsyncComponent(() => import('@/views/project/project/index.vue'));
const Trash = defineAsyncComponent(() => import('@/views/project/trash/index.vue'));
const Activity = defineAsyncComponent(() => import('@/views/project/activity/index.vue'));

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

const menuItems = [
  { icon: 'icon-xiangmu', name: '项目', key: 'project' },
  { icon: 'icon-AIzhushou', name: '助手', key: 'AI' },
  { icon: 'icon-fabu', name: '活动', key: 'activity' },
  { icon: 'icon-qingchu', name: '回收站', key: 'trash' }
];

const menuItems2 = [
  { icon: 'icon-xiangmu', name: '项目', key: 'project' },
  { icon: 'icon-fabu', name: '活动', key: 'activity' },
  { icon: 'icon-qingchu', name: '回收站', key: 'trash' }
];
</script>
<template>
  <template v-if="aiEnabled">
    <LeftMenu :menuItems="menuItems">
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
    </LeftMenu>
  </template>

  <template v-else>
    <LeftMenu :menuItems="menuItems2">
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
    </LeftMenu>
  </template>
</template>
