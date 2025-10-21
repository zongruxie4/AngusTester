<script setup lang="ts">
import { computed } from 'vue';
import { Button } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

import { Icon, Tooltip, Grid, Colon } from '@xcan-angus/vue-ui';

import ScriptTypeTag from '@/components/script/ScriptTypeTag.vue';

const { t } = useI18n();

interface Props {
  value: {
    id:string;
    name:string;
    plugin:string;
    priority:string;
    actualStartDate:string;
    endDate:string;
    result:string;
    scriptType:{value:string;message:string;};
    scriptId:string;
    createdByName:string;
    createdDate:string;
    lastModifiedByName:string;
    lastModifiedDate:string;
    status:{value:string;message:string;};
  };
  exception:{
    codeName:string;
    messageName:string;
    code:string;
    message:string;
  };
  hasIgnoreAssertions?:boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  exception: undefined,
  hasIgnoreAssertions: false
});

const GridColumns = computed(() => {
  if (props.value?.scriptType?.value === 'MOCK_DATA') {
    return [
      [
        { dataIndex: 'id', label: t('common.status') },
        { dataIndex: 'execNodes', label: t('xcan_testBasicInfo.execNodes') }],
      [
        { dataIndex: 'name', label: t('common.scriptType') },
        { dataIndex: 'reportInterval', label: t('xcan_testBasicInfo.reportInterval') }],
      [
        { dataIndex: 'scriptName', label: t('common.priority') },
        { dataIndex: 'batchRows', label: t('xcan_testBasicInfo.batchRows') }],
      [
        { dataIndex: 'plugin', label: t('common.createdBy') },
        { dataIndex: 'modifiedByName', label: t('common.lastModifiedBy') }],
      [
        { dataIndex: 'actualStartDate', label: t('common.endDate') },
        (props.value?.status?.value === 'COMPLETED' && props.value?.mockFileUrl) && { dataIndex: 'mockFileUrl', label: t('xcan_testBasicInfo.mockFileUrl') }].filter(Boolean)
    ];
  }

  if (props.hasIgnoreAssertions) {
    return [
      [
        { dataIndex: 'id', label: t('common.endDate') }],
      [
        { dataIndex: 'name', label: t('common.scriptType') },
        { dataIndex: 'updateTestResult', label: t('xcan_testBasicInfo.updateTestResult') },
        { dataIndex: 'modifiedByName', label: t('common.lastModifiedBy') }],
      [
        { dataIndex: 'scriptName', label: t('common.createdBy') }],
      [
        { dataIndex: 'plugin', label: t('common.plugin') },
        { dataIndex: 'execNodes', label: t('xcan_testBasicInfo.execNodes') },
        { dataIndex: 'actualStartDate', label: t('xcan_testBasicInfo.actualStartDate') }
      ]
    ];
  }

  return [
    [
      { dataIndex: 'id', label: t('common.status') },
      { dataIndex: 'execNodes', label: t('xcan_testBasicInfo.execNodes') }],
    [
      { dataIndex: 'name', label: t('common.scriptType') },
      { dataIndex: 'reportInterval', label: t('xcan_testBasicInfo.reportInterval') }],
    [
      { dataIndex: 'scriptName', label: t('common.priority') },
      { dataIndex: 'ignoreAssertions', label: t('xcan_testBasicInfo.ignoreAssertions') }],
    [
      { dataIndex: 'plugin', label: t('common.createdBy') },
      { dataIndex: 'modifiedByName', label: t('common.lastModifiedBy') }],
    [
      { dataIndex: 'actualStartDate', label: t('common.endDate') },
      { dataIndex: 'updateTestResult', label: t('xcan_testBasicInfo.updateTestResult') }
    ]
  ];
});

const styleMap = {
  CREATED: 'background-color:rgba(45, 142, 255, 1);',
  PENDING: 'background-color:rgba(45, 142, 255, 1);',
  RUNNING: 'background-color:rgba(103, 215, 255, 1);',
  STOPPED: 'background-color:rgba(245, 34, 45, 1);',
  FAILED: 'background-color:rgba(245, 34, 45, 1);',
  TIMEOUT: 'background-color:rgba(245, 34, 45, 1);',
  COMPLETED: 'background-color:rgba(82, 196, 26, 1);'
};

const splitTime = (str: string): [string, string] => {
  const number = str.replace(/\D/g, '');
  const unit = str.replace(/\d/g, '');
  return [number, letterMap[unit]];
};

const letterMap = {
  ms: t('xcan_testBasicInfo.milliseconds'),
  s: t('xcan_testBasicInfo.seconds'),
  min: t('xcan_testBasicInfo.minutes'),
  h: t('xcan_testBasicInfo.hours'),
  d: t('xcan_testBasicInfo.days')
};
</script>
<template>
  <Grid
    :columns="GridColumns"
    :dataSource="props.value"
    :colon="true"
    labelClass="text-text-content font-semibold text-3 text-right"
    valueClass="text-text-sub-content"
    labelSpacing="12px"
    :marginBottom="12"
    :spacing="8">
    <template #execNodes="{text}">
      <template v-if="text">
        <template v-if="Array.isArray(text)">
          {{ text.length }}
        </template>
        <template v-else>
          --
        </template>
      </template>
      <template v-else>
        --
      </template>
    </template>
    <template #ignoreAssertions="{text}">
      {{ typeof text ==='boolean'?text?t('status.ignored'):t('status.notIgnored'):'--' }}
    </template>
    <template #reportInterval="{text}">
      <template v-if="text">
        {{ splitTime(text)[0]+splitTime(text)[1] }}
      </template>
      <template v-else>
        --
      </template>
    </template>
    <template #updateTestResult="{text}">
      {{ typeof text ==='boolean'?text?t('xcan_testBasicInfo.update'):t('xcan_testBasicInfo.notUpdate'):'--' }}
    </template>
    <template #scriptType="{text}">
      <ScriptTypeTag :value="text" class="transform-gpu -translate-y-0.25" />
    </template>
    <template #scriptName="{text}">
      <template v-if="text">
        <RouterLink
          :to="`/script/detail/${props.value?.scriptId}?type=view`"
          target="_blank"
          class="text-text-link hover:text-text-link-hover cursor-pointer break-all">
          {{ text }}
        </RouterLink>
      </template>
      <template v-else>--</template>
    </template>
    <template #createdByName="{text}">
      <span class="mr-2">{{ text }}</span>
      <span class="whitespace-nowrap">{{ props.value?.createdDate }}</span>
    </template>
    <template #modifiedByName>
      <span class="mr-2">{{ props.value?.lastModifiedByName }}</span>
      <span class="whitespace-nowrap">{{ props.value?.lastModifiedDate }}</span>
    </template>
    <template #lastModifiedByName="{text}">
      <span class="mr-2">{{ text }}</span>
      <span>{{ props.value?.lastModifiedDate }}</span>
    </template>
    <template #status="{text}">
      <div class="flex items-center flex-none">
        <div class="w-1.5 h-1.5 rounded mr-1" :style="styleMap[text?.value]"></div>
        <span>{{ text?.message }}</span>
        <template v-if="props.exception">
          <Tooltip
            placement="topLeft"
            arrowPointAtCenter
            :overlayStyle="{'max-width': '400px'}">
            <Icon icon="icon-tishi1" class="ml-1 text-3 text-status-warn cursor-pointer" />
            <template #title>
              <div class="flex space-x-2 leading-5">
                <div class="space-y-1 text-text-content">
                  <div class="whitespace-nowrap">{{ props.exception.codeName }}<Colon /></div>
                  <div class="whitespace-nowrap">{{ props.exception.messageName }}<Colon /></div>
                </div>
                <div class="space-y-1 text-text-sub-content">
                  <div>{{ props.exception.code }}</div>
                  <div>{{ props.exception.message }}</div>
                </div>
              </div>
            </template>
          </Tooltip>
        </template>
      </div>
    </template>
    <template #mockFileUrl>
      <Button
        type="link"
        size="small"
        class="h-5"
        :href="props.value.mockFileUrl">
        {{ props.value?.mockFileName || '' }}
      </Button>
    </template>
  </Grid>
</template>
