<script setup lang="ts">
import { computed, defineAsyncComponent, ref } from 'vue';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { Arrow } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import StatusTag from '@/plugins/test/components/StatusTag/index.vue';
import { ExecInfo, ExecContent } from '../PropsType';

interface Props {
  iterations:string;
  pipelines:ExecInfo['task']['pipelines'];
  execContent:ExecContent[];
}

const props = withDefaults(defineProps<Props>(), {
  iterations: undefined,
  pipelines: () => [],
  execContent: () => []
});

const LDAP = defineAsyncComponent(() => import('./LDAP/index.vue'));

const UUID = utils.uuid();
const collapseActiveKey = ref<string>(UUID);
const arrowOpen = ref(collapseActiveKey.value === UUID);

const arrowChange = (open:boolean) => {
  arrowOpen.value = open;
  if (open) {
    collapseActiveKey.value = UUID;
    return;
  }

  collapseActiveKey.value = undefined;
};

const status = computed(() => {
  if (!props.execContent?.length) {
    return 'block';
  }

  const totalNum = props.execContent.length;
  const successNum = props.execContent.filter(item => item.content?.success).length;
  return totalNum === successNum ? 'success' : 'fail';
});
</script>
<template>
  <Collapse
    :activeKey="collapseActiveKey"
    :bordered="false"
    style="background-color: #fff;font-size: 12px;"
    class="timeline-collapse">
    <CollapsePanel
      :key="UUID"
      :showArrow="false"
      collapsible="disabled">
      <template #header>
        <div class="w-full flex items-center">
          <div class="min-w-20 mr-3">第{{ props.iterations }}次迭代</div>
          <StatusTag :value="status" class="mr-3" />
          <Arrow :open="arrowOpen" @change="arrowChange" />
        </div>
      </template>

      <div class="space-y-3">
        <LDAP
          v-for="item in props.pipelines"
          :key="item.id"
          :value="item"
          :content="props.execContent" />
      </div>
    </CollapsePanel>
  </Collapse>
</template>

<style scoped>
.timeline-collapse.ant-collapse>.ant-collapse-item {
  border: none;
}

.timeline-collapse.ant-collapse>.ant-collapse-item>:deep(.ant-collapse-content) > .ant-collapse-content-box {
  padding: 8px 0;
}

.ant-collapse > :deep(.ant-collapse-item) > .ant-collapse-header {
  padding-right: 0;
  padding-left: 0;
  line-height: 20px;
}
</style>
