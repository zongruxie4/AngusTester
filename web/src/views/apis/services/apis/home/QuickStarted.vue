
<script setup lang="ts">
import { defineAsyncComponent, inject, ref } from 'vue';
import { AsyncComponent, Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { utils } from '@xcan-angus/infra';
import { Button } from 'ant-design-vue';
import { ServicesPermission } from '@/enums/enums';

const LocalImport = defineAsyncComponent(() => import('@/views/apis/services/services/components/LocalImport.vue'));

interface Props {
  serviceInfo:any;
  serviceAuths: string[];
}

const props = withDefaults(defineProps<Props>(), {
  serviceInfo: () => ({}),
  serviceAuths: () => []
});

const { t } = useI18n();

// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });

const visible = ref(false);

const debugging = () => {
  addTabPane({
    value: 'API',
    unarchived: true,
    name: t('apis.quickStarted.addApiTabName'),
    _id: utils.uuid('api') + 'API',
    serviceId: props.serviceInfo.id,
    serviceName: props.serviceInfo.name
  });
};

const openImport = () => {
  visible.value = true;
};
</script>
<template>
  <div>
    <div class="text-3.5 font-semibold mb-3">
      {{ t('apis.quickStarted.title') }}
    </div>

    <div class="flex space-x-3.75 flex-1 ">
      <div class="flex flex-col justify-between px-5 pt-4 pb-3.5 border border-theme-text-box rounded w-1/3">
        <div class="text-theme-content mb-3.5">
          <div class="flex items-center space-x-2 mb-2">
            <Icon icon="icon-jiekoutiaoshi" class="text-5" />
            <span class="title-normal">{{ t('apis.quickStarted.debug.title') }}</span>
          </div>
          <div>{{ t('apis.quickStarted.debug.description') }}</div>
        </div>

        <div class="flex items-start">
          <Button
            type="link"
            size="small"
            class="flex space-x-1"
            @click="debugging">
            <Icon icon="icon-tiaoshi" />
            {{ t('common.actions') }}
          </Button>
        </div>
      </div>

      <div class="flex flex-col justify-between px-5 pt-4 pb-3.5 border border-theme-text-box rounded w-1/3">
        <div class="text-theme-content mb-3.5">
          <div class="flex items-center space-x-2 mb-2">
            <Icon icon="icon-daoruxiangmufuwu" class="text-5" />
            <span class="title-normal">{{ t('apis.quickStarted.import.title') }}</span>
          </div>
          <div>{{ t('apis.quickStarted.import.description') }}</div>
        </div>

        <div class="flex items-start">
          <Button
            type="link"
            size="small"
            class="flex space-x-1"
            :disabled="!props.serviceAuths.includes(ServicesPermission.ADD)"
            @click="openImport">
            <Icon icon="icon-daoru" />
            {{ t('common.actions') }}
          </Button>
        </div>
      </div>
    </div>
  </div>
  <AsyncComponent :visible="visible">
    <LocalImport
      v-model:visible="visible"
      :serviceId="serviceInfo.id"
      source="services" />
  </AsyncComponent>
</template>

<style scoped>
.ant-btn-link{
  padding: 0;
}
</style>
