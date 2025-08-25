<script setup lang="ts">
import { inject, computed, defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Alert, Collapse, CollapsePanel, Tabs, TabPane } from 'ant-design-vue';
import { Arrow, Colon, Icon } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

const { t } = useI18n();

import StatusTag from '../StatusTag/index.vue';
import { ExecContent, ExecInfo } from '../../PropsType';

export interface Props {
  value: ExecInfo['task']['pipelines'][number];
  content: ExecContent[];
  ignoreAssertions: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  content: undefined,
  ignoreAssertions: undefined
});

const RequestParameter = defineAsyncComponent(() => import('./RequestParameter/index.vue'));
const AssertionsResult = defineAsyncComponent(() => import('./AssertionsResult/index.vue'));

const setGlobalTabActiveKey = inject<(key:'executeConfig')=>void>('setGlobalTabActiveKey');

const UUID = utils.uuid();
const collapseActiveKey = ref<string>();
const arrowOpen = ref(collapseActiveKey.value === UUID);

const arrowChange = (open: boolean) => {
  arrowOpen.value = open;
  if (open) {
    collapseActiveKey.value = UUID;
    return;
  }

  collapseActiveKey.value = undefined;
};

const httpContent = computed(() => {
  const linkName = props.value?.linkName;
  if (!linkName) {
    return undefined;
  }

  return props.content?.find(item => linkName === item.name);
});

const queryType = computed(() => {
  return props.value?.type?.value;
});

const runtime = computed(() => {
  const timeStamp = httpContent.value?.content?.response?.timeline?.total;
  if (timeStamp === undefined || timeStamp === null) {
    return '--';
  }

  return utils.formatTime(+timeStamp);
});

const status = computed(() => {
  if (!httpContent.value) {
    return 'block';
  }

  return httpContent.value.content?.success ? 'success' : 'fail';
});

const failMessage = computed(() => {
  return httpContent.value?.content?.failMessage;
});

const showBasicInfo = computed(() => {
  return !!httpContent.value;
});
</script>
<template>
  <Collapse
    :activeKey="collapseActiveKey"
    style="background-color: #fff;font-size: 12px;"
    class="timeline-collapse">
    <CollapsePanel
      :key="UUID"
      :showArrow="false"
      collapsible="disabled">
      <template #header>
        <div class="w-full flex items-center px-3 py-2.5 cursor-pointer" @click="arrowChange(!arrowOpen)">
          <Icon class="flex-shrink-0 text-4 mr-3" icon="icon-jdbc" />
          <div :title="props.value?.name" class="truncate min-w-55 max-w-100 mr-5 name">{{ props.value?.name }}</div>
          <div class="flex-1 justify-end flex items-center mr-3">
            <template v-if="showBasicInfo">
              <div class="mr-5">
                <span class="mr-0.5">{{ t('jdbcPlugin.FunctionTestDetailJdbc.duration') }}<Colon /></span>
                <span class="text-theme-sub-content"> {{ runtime }}</span>
              </div>
            </template>
            <template v-if="!props.value?.enabled">
              <StatusTag />
            </template>
            <template v-else>
              <StatusTag :value="status" />
            </template>
          </div>
          <Arrow :open="arrowOpen" @change="arrowChange" />
        </div>
      </template>
      <Alert
        v-if="!!failMessage"
        :message="failMessage"
        type="error"
        class="mt-3" />
      <Tabs
        type="card"
        size="small"
        class="mt-3 card-tabs">
                      <TabPane key="general" :tab="t('jdbcPlugin.FunctionTestDetailJdbc.requestParameters')">
          <RequestParameter
            :type="queryType"
            :value="httpContent"
            class="py-3" />
        </TabPane>
                      <TabPane key="assertions" :tab="t('jdbcPlugin.FunctionTestDetailJdbc.assertionResults')">
          <Alert
            v-if="props.ignoreAssertions === true"
            closable
            type="info"
            style="border-radius: 4px;"
            class="mt-3">
            <template #message>
              <div class="leading-5 text-3 flex items-center whitespace-pre-line flex-wrap">
                {{ t('jdbcPlugin.FunctionTestDetailJdbc.ignoreAssertionMessage') }}<Button
                  size="small"
                  class="mx-1 px-0 h-5 leading-5"
                  type="link"
                  @click="setGlobalTabActiveKey('executeConfig')">
                  {{ t('jdbcPlugin.FunctionTestDetailJdbc.executeConfig') }}
                                  </Button>{{ t('jdbcPlugin.FunctionTestDetailJdbc.closeIgnoreAssertion') }}
              </div>
            </template>
          </Alert>
          <AssertionsResult
            :value="httpContent"
            :ignoreAssertions="props.ignoreAssertions"
            class="py-3" />
        </TabPane>
      </Tabs>
    </CollapsePanel>
  </Collapse>
</template>

<style scoped>
.ant-collapse > :deep(.ant-collapse-item) > .ant-collapse-header {
  padding: 0;
  border-color: var(--border-divider);
  line-height: 20px;
}

.ant-collapse > :deep(.ant-collapse-item) .ant-collapse-content-box {
  padding: 0 12px;
  line-height: 20px;
}

.embed.ant-collapse > :deep(.ant-collapse-item) > .ant-collapse-header {
  border: none;
  border-bottom: 1px dashed  var(--border-text-box);
}

.embed.ant-collapse,
.embed :deep(.ant-collapse-content),
.embed.ant-collapse > .ant-collapse-item {
  border: none;
}

.embed.ant-collapse > .ant-collapse-item.ant-collapse-item-active {
  border-bottom: 1px dashed var(--border-text-box);
}

.embed .name {
  min-width: 208px;
}

.card-tabs.ant-tabs-top > :deep(.ant-tabs-nav) {
  margin: 0;
}

.card-tabs.ant-tabs-card.ant-tabs-small > :deep(.ant-tabs-nav) .ant-tabs-tab {
  padding: 3px 10px;
  line-height: 20px;
}
</style>
