<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, Ref, ref, onMounted, watch } from 'vue';
import { appContext } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { ProjectInfo } from '@/layout/types';
import { ProjectMenuKey, createMenuItems, ProjectMenuVisibility } from '@/views/project/menu';

const LeftMenu = defineAsyncComponent(() => import('@/components/layout/leftMenu/index.vue'));
const Projects = defineAsyncComponent(() => import('@/views/project/project/index.vue'));
const Trash = defineAsyncComponent(() => import('@/views/project/trash/index.vue'));
const Activity = defineAsyncComponent(() => import('@/views/project/activity/index.vue'));
const Module = defineAsyncComponent(() => import('@/views/project/module/index.vue'));
const Tags = defineAsyncComponent(() => import('@/views/project/tag/index.vue'));
const Version = defineAsyncComponent(() => import('@/views/project/version/index.vue'));
const Evaluation = defineAsyncComponent(() => import('@/views/project/evaluation/index.vue'));

const { t } = useI18n();

const userInfo = ref(appContext.getUser());
const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));
const appInfo = ref(appContext.getAccessApp());
const aiAgent = inject('aiAgent', ref({ chatIframe: '' }));
const aiEnabled = inject('aiEnabled', ref(false));

const projectId = computed(() => {
  return projectInfo.value?.id;
});

const iframeSrc = computed(() => {
  const match = aiAgent.value?.chatIframe.match(/src=['"]([^'"]+)['"]/);
  return match ? match[1] : '';
});

const activeKey = ref<ProjectMenuKey>();

const menuItems = computed(() => {
  const visibility: ProjectMenuVisibility = {
    aiEnabled: !!aiEnabled.value,
    hasProjectId: !!projectId.value
  };
  return createMenuItems(t, visibility);
});

const handleMenuSectionChange = (newMenuKey: ProjectMenuKey | undefined) => {
  // Currently no refresh-token logic required for project menus.
  // This handler exists for consistency with other sections and future extension.
  if (!newMenuKey) return;
};

onMounted(() => {
  watch(() => activeKey.value, handleMenuSectionChange, { immediate: true });
});
</script>
<template>
  <LeftMenu
    v-if="projectId"
    v-model:activeKey="activeKey"
    key="project"
    :menuItems="menuItems">
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
        :projectId="String(projectId)"
        :userInfo="userInfo"
        :appInfo="appInfo" />
    </template>

    <template #activity>
      <Activity class="overflow-auto h-full" />
    </template>

    <template #module>
      <Module
        :projectId="activeKey === ProjectMenuKey.MODULE ? projectId : undefined"
        :userInfo="userInfo"
        :appInfo="appInfo"
        class="p-5" />
    </template>

    <template #version>
      <Version
        :projectId="activeKey === ProjectMenuKey.VERSION ? projectId : undefined"
        :userInfo="userInfo"
        :appInfo="appInfo" />
    </template>

    <template #tags>
      <Tags
        :projectId="activeKey === ProjectMenuKey.TAGS ? projectId : undefined"
        :userInfo="userInfo"
        :appInfo="appInfo"
        class="p-5" />
    </template>

    <template #evaluation>
      <Evaluation
        :projectId="activeKey === ProjectMenuKey.EVALUATION ? projectId : undefined"
        :userInfo="userInfo"
        :appInfo="appInfo"/>
    </template>
  </LeftMenu>
</template>
