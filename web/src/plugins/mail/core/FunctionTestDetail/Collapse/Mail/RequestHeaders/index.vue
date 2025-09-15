<script setup lang="ts">
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { utils } from '@xcan-angus/infra';
import { Arrow, NoData } from '@xcan-angus/vue-ui';

import { ExecContent } from '../../../PropsType';

const { t } = useI18n();

export interface Props {
  value: ExecContent
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const panels:{id:string;name:string;key:'headers'|'request'}[] = [
  {
    id: utils.uuid(),
    name: t('mailPlugin.functionTestDetailMail.requestContent'),
    key: 'request'
  }
];

const activeKeys = ref<string[]>([panels[0].id]);

const arrowChange = (id: string) => {
  if (activeKeys.value.includes(id)) {
    activeKeys.value = activeKeys.value.filter(item => item !== id);
  } else {
    activeKeys.value.push(id);
  }
};

</script>
<template>
  <Collapse
    :activeKey="activeKeys"
    :bordered="false"
    style="background-color: #fff;font-size: 12px;"
    class="tabs-collapse">
    <CollapsePanel
      v-for="item in panels"
      :key="item.id"
      :showArrow="false"
      collapsible="disabled">
      <template #header>
        <div class="w-full flex items-center">
          <Arrow :open="activeKeys.includes(item.id)" @change="arrowChange(item.id)" />
          <div class="ml-1 font-bold">{{ item.name }}</div>
        </div>
      </template>
      <template v-if="item.key==='request'">
        <div v-if="props.value?.content?.request0?.data" class="pb-2 pl-2 pt-1 space-y-1 whitespace-pre">
          {{ props.value.content.request0.data }}
        </div>
        <NoData v-else size="small" />
      </template>
    </CollapsePanel>
  </Collapse>
</template>

<style scoped>
.ant-collapse.tabs-collapse > :deep(.ant-collapse-item) {
  border: none;
}

.ant-collapse.tabs-collapse > :deep(.ant-collapse-item) .ant-collapse-header{
  padding: 6px 0;
}
</style>
