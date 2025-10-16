<script setup lang="ts">
import { computed, inject, ref, Ref } from 'vue';
import { Button, ListItem, Popover, Tooltip } from 'ant-design-vue';
import { Dropdown, HttpMethodTag, Icon } from '@xcan-angus/vue-ui';
import { ButtonGroup, CollapseButtonGroup } from './types';
import { API_STATUS_COLOR_CONFIG } from '@/utils/apis';
import { ServicesPermission, ApiStatus } from '@/enums/enums';
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

// Open API editor tab for this row
const openApiEditorTab = (item) => {
  emits('edit', item);
};

// Toggle expand/collapse details for this row
const toggleApiDetails = (item) => {
  emits('showInfo', item.id, item);
};

// Emit toolbar/menu action from this row
const emitRowAction = (value, item) => {
  emits('handleClick', value, item, props.index);
};

const apiAuths = inject('apiAuths', ref());
const serviceAuths = inject('serviceAuths', ref());
const mockAuth = computed(() => {
  if (serviceAuths.value.includes(ServicesPermission.ADD)) {
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
    if (btn.key === 'addFollow' && props.item.follow === true) {
      return null;
    }
    if (btn.key === 'cancelFollow' && !props.item.follow) {
      return null;
    }
    if (btn.key === 'testTask' && !proTypeShowMap.value.showTask) {
      return null;
    }
    return {
      ...btn
    };
  }).filter(Boolean) as any[];
});

const getBtnDisabled = (btn, item) => {
  const releaseBtn = ['del'];
  if (releaseBtn.includes(btn.value)) {
    return item.status?.value === ApiStatus.RELEASED || !apiAuths.value.includes(btn.auth);
  }
  if (btn.value === 'patchClone') {
    return !serviceAuths.value.includes(btn.auth);
  }
  return !apiAuths.value.includes(btn.auth);
};

// Text color class based on test result
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

// Icon color class based on aggregated test results
const getResultIconColor = (item) => {
  if (item.testFunc && item.testFuncPassed === false) {
    return 'text-status-error';
  }
  if (item.testPerf && item.testPerfPassed === false) {
    return 'text-status-error';
  }
  if (item.testStability && item.testStabilityPassed === false) {
    return 'text-status-error';
  }
  if (!item.testFunc && !item.testPerf && !item.testStability) {
    return '';
  }
  if (item.testFuncPassed === true || item.testPerfPassed === true || item.testStabilityPassed === true) {
    return 'text-status-success';
  }
  return '';
};
</script>
<template>
  <ListItem
    :key="item.id"
    class="mb-3 w-full p-0 cursor-pointer"
    :class="{'deprecated line-through': item.deprecated}"
    @dblclick="openApiEditorTab(item)"
    @click="toggleApiDetails(item)">
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
              <div class="flex-1 min-w-0" :class="getResultColor(item.testFunc, item.testFuncPassed)">
                {{ !item.testFunc
                  ? t('status.disabled')
                  : item.testFuncPassed
                    ? t('status.passed')
                    : item.testFuncPassed === false
                      ? t('status.notPassed')
                      : t('status.notTested') }} <span class="text-status-error">{{ item.testFuncFailureMessage }}</span>
              </div>
            </div>
            <div class="flex">
              <span class="w-20">{{ t('service.apiList.template.testTask.perfTest') }}:</span>
              <div class="flex-1 min-w-0" :class="getResultColor(item.testPerf, item.testPerfPassed)">
                {{ !item.testPerf
                  ? t('status.disabled')
                  : item.testPerfPassed
                    ? t('status.passed')
                    : item.testPerfPassed === false
                      ? t('status.notPassed')
                      : t('status.notTested') }} <span class="text-status-error">{{ item.testPerfFailureMessage }}</span>
              </div>
            </div>
            <div class="flex">
              <span class="w-20">{{ t('service.apiList.template.testTask.stabilityTest') }}:</span>
              <div class="flex-1 min-w-0" :class="getResultColor(item.testStability, item.testStabilityPassed)">
                {{ !item.testStability
                  ? t('status.disabled')
                  : item.testStabilityPassed
                    ? t('status.passed')
                    : item.testStabilityPassed === false
                      ? t('status.notPassed')
                      : t('status.notTested') }} <span class="text-status-error">{{ item.testStabilityFailureMessage }}</span>
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
              @click.stop="emitRowAction(record.value, item)">
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
            @click="$event =>emitRowAction($event.key, item)">
            <Icon icon="icon-gengduo" />
          </Dropdown>
        </div>
      </div>
    </div>
  </ListItem>
</template>
