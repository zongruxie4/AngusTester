
<script setup lang="ts">
import { computed, defineAsyncComponent, inject, ref } from 'vue';
import { AsyncComponent, Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { utils } from '@xcan-angus/infra';
import { Button } from 'ant-design-vue';

const LocalImport = defineAsyncComponent(() => import('../../sidebar/components/localImport/index.vue'));

interface Props {
  info:any;
  projectAuths: string[];
}

const props = withDefaults(defineProps<Props>(), {
  info: () => ({}),
  projectAuths: () => []
});

const { t } = useI18n();
// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });

const visible = ref(false);

const debugging = () => {
  addTabPane({
    value: 'API',
    unarchived: true,
    name: '添加API',
    _id: utils.uuid('api') + 'API',
    serviceId: props.info.id,
    serviceName: props.info.name
  });
};

const openImport = () => {
  visible.value = true;
};

const entries = computed(():{titleIcon: string; title: string; content: string; icon: string; action: string; click:() => void, disabled?: boolean }[] => {
  return [
    {
      titleIcon: 'icon-jiekoutiaoshi',
      title: t('apis.quickAccess.debug.title'),
      content: t('apis.quickAccess.debug.description'),
      icon: 'icon-tiaoshi',
      action: t('apis.quickAccess.debug.action'),
      click: debugging
    },
    {
      titleIcon: 'icon-daoruxiangmufuwu',
      title: t('apis.quickAccess.import.title'),
      content: t('apis.quickAccess.import.description'),
      icon: 'icon-daoru',
      action: t('apis.quickAccess.import.action'),
      click: openImport,
      disabled: !props.projectAuths.includes('ADD')
    }
  ].filter(Boolean);
});




</script>
<template>
  <div class="space-y-2">
    <div class="title-normal">{{ t('apis.quickAccess.title') }}</div>
    <div class="flex flex-nowrap space-x-5 px-4">
      <div
        v-for="(entery,index) in entries"
        :key="entery.titleIcon"
        style="width:calc((100% - 40px)/3);"
        class="p-5 flex flex-col justify-between min-h-37.5 min-w-50 border border-theme-text-box rounded">
        <div>
          <div class="flex items-center space-x-2">
            <Icon :icon="entery.titleIcon" class="text-white -mt-0.5 text-5" />
            <span class="title-normal">{{ entery.title }}</span>
          </div>
          <div class="mt-3 text-content">{{ entery.content }}</div>
        </div>
        <Button
            type="link"
            size="small"
            class="w-fit h-5 space-x-1 mt-2"
            :class="{'text-theme-special text-theme-text-hover': !entery.disabled}"
            :disabled="entery.disabled"
            @click="entery.click">
            <Icon :icon="entery.icon" />
            <span> {{ entery.action }}</span>
          </Button>
      </div>
    </div>
  </div>
  <AsyncComponent :visible="visible">
    <LocalImport
      v-model:visible="visible"
      :serviceId="info.id"
      source="projectOrService" />
  </AsyncComponent>
</template>

<style scoped>
.ant-btn-link{
  padding: 0;
}
</style>
