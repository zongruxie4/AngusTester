<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';

const PirChart = defineAsyncComponent(() => import('./PirChart.vue'));

interface Props {
  list:Record<string, any>[]
}

const props = withDefaults(defineProps<Props>(), {
  list: () => []
});
</script>
<template>
  <div class="text-3 text-text-content">
    <div class="h-9.5 leading-9.5 bg-theme-form-head flex font-medium w-full">
      <div class="w-12"></div>
      <div class="py-0.5 px-2 flex-1">错误统计</div>
      <div style="width:30%" class="py-0.5 px-2">错误数</div>
      <div style="width:30%" class="py-0.5 px-2">错误率</div>
    </div>
    <Collapse class="!bg-transparent">
      <template #expandIcon="record">
        <Icon
          :icon="record.isActive?'icon-xiangxia':'icon-xiangshang'"
          class="text-3 text-text-sub-content"
          :class="props.list?.[record.panelKey].list?.length?'text-text-sub-content':'text-text-disabled cursor-not-allowed'" />
      </template>
      <CollapsePanel
        v-for="(item,index) in props.list"
        :key="index"
        :style="{'background-color':item.name ==='Total'?'#FAFCFC':''}"
        :collapsible="item.list?.length?'':'disabled'">
        <template #header>
          <div class="flex text-3 text-text-content leading-5.5 w-full">
            <div class="py-0.5 px-2 flex-1 ml-2.5 truncate" :title="item.name">{{ item.name }}</div>
            <div style="width:31%" class="py-0.5 px-2">{{ item.errorNum }}</div>
            <div style="width:30%" class="py-0.5 px-2">{{ item.errorRate }}</div>
          </div>
        </template>
        <div class="flex w-full relative items-center" style="min-height: 140px">
          <div class="w-full">
            <div
              v-for="(child,childIndex) in item.list"
              :key="childIndex"
              class="flex w-full h-7 leading-7 text-3 text-text-content">
              <div class="w-12"></div>
              <div class="py-0.5 px-2 flex-1 break-words leading-4">{{ child.name }}</div>
              <div style="width:60%" class="py-0.5 px-2">{{ child.errorNum }}</div>
            </div>
          </div>
          <div class="pie-chart-class">
            <PirChart :dataSource="item.list.map(m=>({name:m.name,value:m.errorNum || 0}))" />
          </div>
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
 padding: 0;
}

:deep(.ant-collapse > .ant-collapse-item > .ant-collapse-header .ant-collapse-arrow svg){
 margin-top: -5px;
}

.pie-chart-class{
  position: absolute;
  top: 50%;
  left: 74%;
  transform: translate(-50%, -50%);
}
</style>
