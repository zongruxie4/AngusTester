<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue';
import { Hints, Spin } from '@xcan-angus/vue-ui';
import { Tabs, TabPane } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

import type { TargetType, Scope } from './PropsType';
import List from './List.vue';
const { t } = useI18n();

interface Props {
  id: string;
  disabled: boolean;
  type:TargetType;
}

const props = withDefaults(defineProps<Props>(), {
  id: '',
  disabled: false,
  type: 'PROJECT'
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'rendered', value: true);
}>();

const activeKey = ref<Scope>('CURRENT');

const tabList = props.type === 'PROJECT'
  ? [
      {
        key: 'CURRENT', value: t('xcan_variable.currentVariable')
      },
      {
        key: 'GLOBAL', value: t('xcan_variable.globalShared')
      }
    ]
  : [
      {
        key: 'CURRENT', value: t('xcan_variable.currentVariable')
      },
      {
        key: 'INHERIT', value: t('xcan_variable.inheritedVariable')
      },
      {
        key: 'GLOBAL', value: t('xcan_variable.globalShared')
      }
    ];

const loading = ref(false);

onMounted(() => {
  nextTick(() => {
    emit('rendered', true);
  });
});

</script>
<template>
  <Spin class="h-full flex flex-col" :spinning="loading">
    <Hints :text="t('xcan_variable.variableDescription')" />
    <Hints
      :text="t('xcan_variable.variablePriority')"
      class="mt-2 hints-text" />
    <Tabs
      v-model:activeKey="activeKey"
      class="mt-2">
      <TabPane
        v-for="tab in tabList"
        :key="tab.key"
        :tab="tab.value" />
    </Tabs>
    <template v-if="activeKey === 'CURRENT'">
      <template v-if="props.type === 'PROJECT'">
        <Hints
          :text="t('xcan_variable.projectVariableDescription')"
          class="mt-2" />
      </template>
      <template v-if="props.type === 'SERVICE'">
        <Hints
          :text="t('xcan_variable.serviceVariableDescription')"
          class="mt-2" />
      </template>
      <template v-if="props.type === 'API'">
        <Hints
          :text="t('xcan_variable.apiVariableDescription')"
          class="mt-2" />
      </template>
      <List
        :id="props.id"
        v-model:loading="loading"
        :tabKey="activeKey"
        :disabled="props.disabled"
        :type="props.type" />
    </template>
    <template v-if="activeKey === 'INHERIT'">
      <Hints
        :text="t('xcan_variable.inheritedVariableDescription')"
        class="mt-2" />
      <List
        :id="props.id"
        v-model:loading="loading"
        :tabKey="activeKey"
        :disabled="props.disabled"
        :type="props.type" />
    </template>
    <template v-if="activeKey === 'GLOBAL'">
      <Hints
        :text="t('xcan_variable.globalVariableDescription')"
        class="mt-2" />
      <List
        :id="props.id"
        v-model:loading="loading"
        :tabKey="activeKey"
        :disabled="props.disabled"
        :type="props.type" />
    </template>
  </Spin>
</template>
<style scoped>
:deep(.hints-text > svg) {
  visibility: hidden;
}

:deep(.ant-tabs>.ant-tabs-nav, .ant-tabs>div>.ant-tabs-nav) {
  margin: 0;
}

:deep(.ant-tabs-tab) {
  padding: 4px 0;
  font-size: 12px;
  line-height: 24px;
}
</style>
