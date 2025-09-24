<script setup lang="ts">
import { computed, ref, defineAsyncComponent, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { utils } from '@xcan-angus/infra';
import { Arrow, NoData, Icon, Spin } from '@xcan-angus/vue-ui';

import { ExecContent } from '../../../PropsType';

const { t } = useI18n();

export interface Props {
  value: ExecContent;
  ignoreAssertions: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  ignoreAssertions: undefined
});

const Result = defineAsyncComponent(() => import('./Result/index.vue'));

const activeKeys = ref<string[]>([]);
const loadingMap = ref<{[key:string]:boolean}>({});

const arrowChange = (id: string) => {
  if (activeKeys.value.includes(id)) {
    activeKeys.value = activeKeys.value.filter(item => item !== id);
  } else {
    activeKeys.value.push(id);
  }
};

const assertions = computed(() => {
  return props.value?.content?.assertions?.map(item => {
    return {
      id: utils.uuid(),
      ...item
    };
  }) || [];
});

const statusMap = computed(():{[key:string]:'Disabled'|'Ignored'|'Success'|'Failed'} => {
  return assertions.value.reduce((prev, cur) => {
    if (props.ignoreAssertions) {
      prev[cur.id] = 'Ignored';
    } else if (cur.enabled === false) {
      prev[cur.id] = 'Disabled';
    } else if (cur.ignore === true) {
      prev[cur.id] = 'Ignored';
    } else if (cur.result?.failure === true) {
      prev[cur.id] = 'Failed';
    } else {
      prev[cur.id] = 'Success';
    }

    return prev;
  }, {} as {[key:string]:'Disabled'|'Ignored'|'Success'|'Failed'});
});

watch(() => assertions.value, (newValue) => {
  if (!newValue?.length) {
    return;
  }

  loadingMap.value = newValue.reduce((prev, cur) => {
    prev[cur.id] = true;
    return prev;
  }, {});
}, { immediate: true });
</script>
<template>
  <template v-if="!assertions.length">
    <NoData size="small" class="my-10" />
  </template>
  <template v-else>
    <Collapse
      :activeKey="activeKeys"
      :bordered="false"
      style="background-color: #fff;font-size: 12px;"
      class="tabs-collapse">
      <CollapsePanel
        v-for="item in assertions"
        :key="item.id"
        :showArrow="false"
        collapsible="disabled">
        <template #header>
          <div class="w-full flex items-center">
            <Arrow :open="activeKeys.includes(item.id)" @change="arrowChange(item.id)" />
            <div class="ml-1 w-104 truncate" :title="item.name">{{ item.name }}</div>
            <div
              v-if="statusMap[item.id]==='Failed'"
              class="flex items-center"
              style="color:rgba(255, 129, 0, 100%);">
              <Icon icon="icon-chahao" class="mr-1.5 text-3.5" />
              <span>{{ t('status.failed') }}</span>
            </div>
            <div
              v-else-if="statusMap[item.id]==='Success'"
              class="flex items-center"
              style="color:#52c41a;">
              <Icon icon="icon-duihao" class="mr-1.5 text-3.5" />
              <span>{{ t('status.passed') }}</span>
            </div>
            <div
              v-else-if="statusMap[item.id]==='Ignored'"
              class="flex items-center"
              style="color:rgba(217, 217, 217, 100%);">
              <span class="inline-block w-3 h-3 mr-1.5 rounded-md" style="background-color:rgba(217, 217, 217, 100%);"></span>
              <span>{{ t('status.ignored') }}</span>
            </div>
            <div
              v-else-if="statusMap[item.id]==='Disabled'"
              class="flex items-center"
              style="color:rgba(217, 217, 217, 100%);">
              <span class="inline-block w-3 h-3 mr-1.5 rounded-md" style="background-color:rgba(217, 217, 217, 100%);"></span>
              <span>{{ t('status.disabled') }}</span>
            </div>
          </div>
        </template>
        <Spin :spinning="loadingMap[item.id]" class="min-h-31.5">
          <Result
            v-model:loading="loadingMap[item.id]"
            :value="item"
            :status="statusMap[item.id]"
            :execContent="props.value?.content"
            :ignoreAssertions="props.ignoreAssertions" />
        </Spin>
      </CollapsePanel>
    </Collapse>
  </template>
</template>

<style scoped>
.ant-collapse.tabs-collapse > :deep(.ant-collapse-item) {
  border: none;
}

.ant-collapse.tabs-collapse > :deep(.ant-collapse-item) .ant-collapse-header{
  padding: 8px 0;
}
</style>
