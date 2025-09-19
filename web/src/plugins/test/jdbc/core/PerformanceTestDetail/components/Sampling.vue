<script setup lang="ts">
import { ref } from 'vue';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { Icon, Colon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

interface Props {
  sampleList:Record<string, any>[]
}

const props = withDefaults(defineProps<Props>(), {
  sampleList: () => []
});

const { t } = useI18n();
const activeKey = ref([]);
</script>
<template>
  <div class="text-3 text-text-content">
    <div class="h-9.5 leading-9.5 bg-theme-form-head flex font-medium">
      <div class="w-12"></div>
      <div class="py-0.5 px-2 flex-1">{{ t('ftpPlugin.performanceTestDetail.sampling.title') }}</div>
    </div>
    <Collapse v-model:activeKey="activeKey" class="!bg-transparent">
      <template #expandIcon="item">
        <Icon
          :icon="item.isActive?'icon-xiangxia':'icon-xiangshang'"
          :class="sampleList[item.panelKey].content?'text-text-sub-content':'text-text-disabled cursor-not-allowed'"
          class="text-3 " />
      </template>
      <CollapsePanel
        v-for="item,index in props.sampleList"
        :key="index"
        :collapsible="item.content?'':'disabled'">
        <template #header>
          <div class="flex w-full text-3 text-text-content leading-5.5 space-x-2 pl-5">
            <div class="flex-none w-45"><span class="mr-2">{{ t('ftpPlugin.performanceTestDetail.sampling.time') }}<Colon /></span>{{ item.timestamp }}</div>
            <div class="flex-none w-45"><span class="mr-2">{{ t('ftpPlugin.performanceTestDetail.sampling.samplingId') }}<Colon /></span>{{ item.key }}</div>
            <div class="flex-1 truncate" :title="item.name"><span class="mr-2">{{ t('ftpPlugin.performanceTestDetail.sampling.name') }}<Colon /></span>{{ item.name }}</div>
          </div>
        </template>
        <div>
          {{ item.content }}
        </div>
      </CollapsePanel>
    </Collapse>
  </div>
</template>
<style scoped>
:deep(.ant-collapse){
  border: 0;
}

:deep(.ant-collapse > .ant-collapse-item > .ant-collapse-header){
 padding: 8px 14px;
}

:deep(.ant-collapse-content > .ant-collapse-content-box){
 padding: 8px 8px 8px 36px;
}

:deep(.ant-collapse > .ant-collapse-item > .ant-collapse-header .ant-collapse-arrow svg){
 margin-top: -5px;
}
</style>
