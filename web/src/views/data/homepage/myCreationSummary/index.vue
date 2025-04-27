<script setup lang="ts">
import { defineAsyncComponent, ref } from 'vue';
import { TabPane, Tabs } from 'ant-design-vue';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined
});

const Table = defineAsyncComponent(() => import('./table.vue'));

const deletedNotify = ref<string>();

const variableTotal = ref(0);
const dataSetTotal = ref(0);
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-1">我的参数化</div>
    <Tabs size="small">
      <TabPane key="variable" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">添加的变量</span>
            <span>(</span>
            <span>{{ variableTotal }}</span>
            <span>)</span>
          </div>
        </template>
        <Table
          v-model:total="variableTotal"
          v-model:deletedNotify="deletedNotify"
          type="variable"
          :userId="props.userInfo?.id"
          :notify="props.notify"
          :projectId="props.projectId" />
      </TabPane>
      <TabPane key="dataSet" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">添加的数据集</span>
            <span>(</span>
            <span>{{ dataSetTotal }}</span>
            <span>)</span>
          </div>
        </template>
        <Table
          v-model:total="dataSetTotal"
          v-model:deletedNotify="deletedNotify"
          type="dataSet"
          :notify="props.notify"
          :userId="props.userInfo?.id"
          :projectId="props.projectId" />
      </TabPane>
      <!-- <TabPane key="space" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">添加的空间</span>
            <span>(</span>
            <span>{{ spaceTotal }}</span>
            <span>)</span>
          </div>
        </template>
        <Table
          v-model:total="spaceTotal"
          v-model:deletedNotify="deletedNotify"
          type="space"
          :notify="props.notify"
          :userId="props.userInfo?.id"
          :projectId="props.projectId" />
      </TabPane>
      <TabPane key="dataSource" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">添加的数据源</span>
            <span>({{ dataSourceTotal }})</span>
          </div>
        </template>
        <Table
          v-model:total="dataSourceTotal"
          v-model:deletedNotify="deletedNotify"
          type="dataSource"
          :notify="props.notify"
          :userId="props.userInfo?.id"
          :projectId="props.projectId" />
      </TabPane> -->
    </Tabs>
  </div>
</template>

<style scoped>
.ant-tabs {
  line-height: 20px;
}

:deep(.ant-tabs-content-holder) {
  min-height: 166px;
}

.ant-tabs-top>:deep(.ant-tabs-nav),
.ant-tabs-bottom>:deep(.ant-tabs-nav),
.ant-tabs-top>:deep(div)>.ant-tabs-nav,
.ant-tabs-bottom>:deep(div)>.ant-tabs-nav {
  margin-bottom: 14px;
}
</style>
