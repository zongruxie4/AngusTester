<script setup lang="ts">
import { computed, inject, ref, Ref } from 'vue';
import { Button, ListItem, Popover, Tooltip } from 'ant-design-vue';
import { Dropdown, HttpMethodTag, Icon } from '@xcan-angus/vue-ui';
import { ButtonGroup, CollapseButtonGroup } from './interface';
import { API_STATUS_COLOR_CONFIG } from '@/views/apis/utils';
import { useI18n } from 'vue-i18n';

interface Props {
  item: Record<string, any>;
  activeApiId: string;
  index: number;
}

const props = withDefaults(defineProps<Props>(), {
  item: () => ({}),
  activeApiId: ''
});
const { t } = useI18n();

const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
 (e: 'edit', value):void;
 (e: 'showInfo', id: string, value):void;
 (e: 'handleClick', value: string, item, index: number): void;
}>();

const edit = (item) => {
  emits('edit', item);
};

const showInfo = (item) => {
  emits('showInfo', item.id, item);
};

const handleClick = (value, item) => {
  emits('handleClick', value, item, props.index);
};

const apiAuths = inject('apiAuths', ref());
const projectAuths = inject('projectAuths', ref());
const mockAuth = computed(() => {
  if (projectAuths.value.includes('ADD')) {
    return ['MOCK'];
  }
  return [];
});

// popover 显示的 button
const myButtonGroup = computed(() => {
  return ButtonGroup.map(btn => {
    if (props.item.protocol?.value?.includes('ws') && ['mock'].includes(btn.key)) {
      return null;
    }
    if (btn.key === 'addFavourite' && props.item.favourite === true) {
      return null;
    }
    if (btn.key === 'cancelFavourite' && !props.item.favourite) {
      return null;
    }
    if (btn.key === 'addWatch' && props.item.follow === true) {
      return null;
    }
    if (btn.key === 'cancelWatch' && !props.item.follow) {
      return null;
    }
    if (btn.key === 'testTask' && !proTypeShowMap.value.showTask) {
      return null;
    }
    return {
      ...btn
    };
  }).filter(Boolean);
});

const getBtnDisabled = (btn, item) => {
  const publishBtn = ['del'];
  if (publishBtn.includes(btn.value)) {
    return item.status?.value === 'RELEASED' || !apiAuths.value.includes(btn.auth);
  }
  if (btn.value === 'patchClone') {
    return !projectAuths.value.includes(btn.auth);
  }
  return !apiAuths.value.includes(btn.auth);
};

// 测试结果图标颜色
const getResultIconColor = (item) => {
  if (item.testFuncFlag && item.testFuncPassedFlag === false) {
    return 'text-status-error';
  }
  if (item.testPerfFlag && item.testPerfPassedFlag === false) {
    return 'text-status-error';
  }
  if (item.testStabilityFlag && item.testStabilityPassedFlag === false) {
    return 'text-status-error';
  }
  if (!item.testFuncFlag && !item.testPerfFlag && !item.testStabilityFlag) {
    return '';
  }
  if (item.testFuncPassedFlag === true || item.testPerfPassedFlag === true || item.testStabilityPassedFlag === true) {
    return 'text-status-success';
  }
  return '';
};

// 测试结果颜色
const getResultColor = (testFlag, testPassd = undefined) => {
  if (!testFlag || testPassd === undefined) {
    return;
  }
  if (testPassd) {
    return 'text-status-success';
  }
  if (testPassd === false) {
    return 'text-status-error';
  }
};

</script>
<template>
  <ListItem
    :key="item.id"
    class="mb-3 w-full p-0 cursor-pointer"
    :class="{'deprecated line-through': item.deprecated}"
    @dblclick="edit(item)"
    @click="showInfo(item)">
    <div
      class="flex w-full h-11.5  rounded justify-between px-5 text-3 leading-3.5"
      :class="{'bg-gray-300': props.activeApiId === item.id, 'bg-gray-light': props.activeApiId !== item.id}">
      <div class="flex items-center flex-1">
        <HttpMethodTag :value="item.method" />
        <Tooltip placement="topLeft" :title="item.endpoint">
          <div class="ml-7.5 mr-7.5 leading-5 w-0 truncate flex-1 flex items-center">
            <span
              v-if="item.protocol.value.includes('ws')"
              class="h-4 mr-2 text-http-get font-semibold bg-blue-light px-1 rounded-full leading-4">
              ws
            </span>
            <span class="flex-1 truncate">{{ item.endpoint }}</span>
          </div>
        </Tooltip>
        <Popover :destroyTooltipOnHide="true" overlayClassName="max-w-80">
          <span v-if="item.protocol.value.includes('http')" class="px-4">
            <Icon
              icon="icon-zhihangceshi"
              class="text-4"
              :class="getResultIconColor(item)" />
          </span>
          <template #content>
            <div class="flex">
              <span class="w-20">{{ t('service.apiList.template.testTask.funcTest') }}:</span>
              <div class="flex-1 min-w-0" :class="getResultColor(item.testFuncFlag, item.testFuncPassedFlag)">
                {{ !item.testFuncFlag
                  ? t('service.apiList.test.notEnabled')
                  : item.testFuncPassedFlag
                    ? t('service.apiList.test.passed')
                    : item.testFuncPassedFlag === false
                      ? t('service.apiList.test.unPassed')
                      : t('service.apiList.test.notTested') }} <span class="text-status-error">{{ item.testFuncFailureMessage }}</span>
              </div>
            </div>
            <div class="flex">
              <span class="w-20">{{ t('service.apiList.template.testTask.perfTest') }}:</span>
              <div class="flex-1 min-w-0" :class="getResultColor(item.testPerfFlag, item.testPerfPassedFlag)">
                {{ !item.testPerfFlag
                  ? t('service.apiList.test.notEnabled')
                  : item.testPerfPassedFlag
                    ? t('service.apiList.test.passed')
                    : item.testPerfPassedFlag === false
                      ? t('service.apiList.test.unPassed')
                      : t('service.apiList.test.notTested') }} <span class="text-status-error">{{ item.testPerfFailureMessage }}</span>
              </div>
            </div>
            <div class="flex">
              <span class="w-20">{{ t('service.apiList.template.testTask.stabilityTest') }}:</span>
              <div class="flex-1 min-w-0" :class="getResultColor(item.testStabilityFlag, item.testStabilityPassedFlag)">
                {{ !item.testStabilityFlag
                  ? t('service.apiList.test.notEnabled')
                  : item.testStabilityPassedFlag
                    ? t('service.apiList.test.passed')
                    : item.testStabilityPassedFlag === false
                      ? t('service.apiList.test.unPassed')
                      : t('service.apiList.test.notTested') }} <span class="text-status-error">{{ item.testStabilityFailureMessage }}</span>
              </div>
            </div>
          </template>
        </Popover>
        <span :class="['w-20', API_STATUS_COLOR_CONFIG[item.status?.value]]">{{ item.status?.message }}</span>
        <Tooltip
          placement="top"
          :title="item.summary"
          :destroyPopupOnHide="true">
          <div class="flex-1 w-0 h-4 leading-4 flex items-center">
            <div class="truncate pl-1">{{ item.summary }}</div>
          </div>
        </Tooltip>
      </div>
      <div class="justify-end flex items-center btns-wrapper" :class="[props.activeApiId === item.id ? 'visible' : 'invisible']">
        <div class="whitespace-nowrap edit-btn-wrapper">
          <template v-for="record in CollapseButtonGroup" :key="record.value">
            <Button
              type="text"
              class="!bg-transparent"
              :disabled="getBtnDisabled(record, item)"
              @click.stop="handleClick(record.value, item)">
              <Icon :icon="record.icon" />
              {{ record.label }}
            </Button>
          </template>
        </div>
        <div>
          <Dropdown
            :menuItems="myButtonGroup"
            :permissions="[...apiAuths, ...mockAuth]"
            :destroyPopupOnHide="true"
            @click="$event =>handleClick($event.key, item)">
            <Icon icon="icon-gengduo" />
          </Dropdown>
        </div>
      </div>
    </div>
  </ListItem>
</template>
