<script setup lang="ts">
import { Button, ListItem, Popover, Tooltip } from 'ant-design-vue';
import { computed, inject, onMounted, ref, watch, Ref } from 'vue';
import { Arrow, Dropdown, HttpMethodTag, Icon, Image } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { API_STATUS_COLOR_CONFIG } from '@/utils/apis';
import { bgColor } from '@/utils/common';
import type { DataSourceType } from '../PropsType';
import { ButtonGroup, CollapseButtonGroup } from './interface';

import VirtualList from './BaseVirtualList.vue';

interface Props{
  dataSource:Array<DataSourceType>;
  updateData:(value:{id:string;auth:boolean;})=>void;
  height: number;
  showNum?: number;
  groupedBy?: string;
  activeApiId?: string;
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => [],
  updateData: undefined,
  height: 500,
  showNum: 20,
  groupedBy: 'none'
});

const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));
// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
    (e:'loadApis'):void,
    (e:'openMock'),
    (e: 'edit', value):void;
    (e: 'showInfo', id: string, value):void;
    (e: 'handleClick', value: string, item, index: number): void;
  }>();
// 编辑api
const edit = (value:DataSourceType):void => {
  emits('edit', value);
};

const apiAuths = inject('apiAuths', ref());
const serviceAuths = inject('serviceAuths', ref());

// popover 显示的 button
const myButtonGroup = (item) => {
  return ButtonGroup.map(btn => {
    if (item.protocol?.value?.includes('ws') && ['mock'].includes(btn.key)) {
      return null;
    }
    if (btn.key === 'addFavourite' && item.favourite === true) {
      return null;
    }
    if (btn.key === 'cancelFavourite' && !item.favourite) {
      return null;
    }
    if (btn.key === 'addFollow' && item.follow === true) {
      return null;
    }
    if (btn.key === 'cancelFollow' && !item.follow) {
      return null;
    }
    if (btn.key === 'testTask' && !proTypeShowMap.value.showTask) {
      return null;
    }
    return {
      ...btn
    };
  }).filter(Boolean);
};

const myCollapseButtonGroup = computed(() => {
  return CollapseButtonGroup;
});

const getBtnDisabled = (btn, item) => {
  const publishBtn = ['del'];
  if (publishBtn.includes(btn.value)) {
    return item.status?.value === 'RELEASED' || !apiAuths.value.includes(btn.auth);
  }
  if (btn.value === 'patchClone') {
    return !serviceAuths.value.includes(btn.auth);
  }
  return !apiAuths.value.includes(btn.auth);
};
// 点击详情
const showInfo = (id:string, api) => {
  emits('showInfo', id, api);
};

const showData = ref<DataSourceType[]>([]);
const groupSpreadMap = ref<{[key: string]: boolean}>({});
// let size = 0;
// 监听 当前 open api 的变化, 更改 ap

onMounted(() => {
  watch(() => props.groupedBy, () => {
    groupSpreadMap.value = {};
  }, {
    immediate: true
  });
  watch(() => props.dataSource, (newValue, oldValue) => {
    showData.value = JSON.parse(JSON.stringify(newValue));
    if (newValue.length && !oldValue?.length) {
      const groups = props.dataSource.filter(item => item.type === 'group');
      groups.forEach(group => {
        groupSpreadMap.value[group.key] = false;
      });
      if (groups?.[0]) {
        groupSpreadMap.value[groups[0].key] = true;
      }
      if (groups?.[1]) {
        const secondGroupIdx = props.dataSource.findIndex(item => item.key === groups[1].key);
        showData.value = props.dataSource.filter((item, idx) => {
          if (item.key) {
            return true;
          }
          if (idx < secondGroupIdx) {
            return true;
          }
          return false;
        });
      }
    } else {
      const groups = newValue.filter(item => item.type === 'group');
      groups.forEach(group => {
        if (!groupSpreadMap.value[group.key]) {
          const idx = showData.value.findIndex(item => item.type === 'group' && item.key === group.key);
          handleSpread(group, idx);
        }
      });
    }
  }, {
    immediate: true
  });
});

// 测试结果图标颜色
const getResultIconColor = (item) => {
  if (item.testFunc && item.testFuncPassedFlag === false) {
    return 'text-status-error';
  }
  if (item.testPerf && item.testPerfPassedFlag === false) {
    return 'text-status-error';
  }
  if (item.testStability && item.testStabilityPassedFlag === false) {
    return 'text-status-error';
  }
  if (!item.testFunc && !item.testPerf && !item.testStability) {
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

const handleClick = (event:string, data:DataSourceType, index: number) => {
  emits('handleClick', event, data, index);
};

const mockAuth = computed(() => {
  if (serviceAuths.value.includes('ADD')) {
    return ['MOCK'];
  }
  return [];
});

const handleSpread = (group, index) => {
  // groupSpreadMap.value[group.key] = !groupSpreadMap.value[group.key];
  if (groupSpreadMap.value[group.key]) {
    const idx = props.dataSource.findIndex(i => i.key === group.key);
    const arr = props.dataSource.slice(idx + 1, idx + 1 + group.childrenNum);
    showData.value.splice(index + 1, 0, ...arr);
  } else {
    showData.value.splice(index + 1, group.childrenNum);
  }
};

const handleClickSpread = (group, index) => {
  groupSpreadMap.value[group.key] = !groupSpreadMap.value[group.key];
  handleSpread(group, index);
};

</script>

<template>
  <VirtualList
    class="pr-4"
    :data="showData"
    :height="props.height"
    :showNum="props.showNum"
    :cache="20">
    <template #default="{item, index}">
      <ListItem
        v-if="item.id"
        :key="item.id"
        class="mb-3 w-full p-0 cursor-pointer"
        :class="{'deprecated line-through': item.deprecated, 'has-border relative': props.groupedBy !== 'none', '!border-b last-data': showData[index + 1]?.type === 'group' || (props.groupedBy !== 'none' && !showData[index + 1]) }"
        @dblclick="edit(item)"
        @click="showInfo(item.id, item)">
        <div
          class="flex w-full h-11.5  rounded justify-between px-5 text-3 leading-3.5"
          :class="{'bg-gray-300': props.activeApiId === item.id, 'bg-gray-light': props.activeApiId !== item.id, 'mx-4': props.groupedBy !== 'none'}">
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
                <div class="flex"><span class="w-20">{{ t('service.apiList.test.functionalTest') }}：</span> <div class="flex-1 min-w-0" :class="getResultColor(item.testFunc, item.testFuncPassedFlag)">{{ !item.testFunc ? t('status.disabled') : item.testFuncPassedFlag ? t('status.passed') : item.testFuncPassedFlag === false ? t('status.notPassed') : t('status.notTested') }} <span class="text-status-error">{{ item.testFuncFailureMessage }}</span></div></div>
                <div class="flex"><span class="w-20">{{ t('service.apiList.test.performanceTest') }}：</span> <div class="flex-1 min-w-0" :class="getResultColor(item.testPerf, item.testPerfPassedFlag)">{{ !item.testPerf ? t('status.disabled') : item.testPerfPassedFlag ? t('status.passed') : item.testPerfPassedFlag === false ? t('status.notPassed') : t('status.notTested') }} <span class="text-status-error">{{ item.testPerfFailureMessage }}</span></div></div>
                <div class="flex"><span class="w-20">{{ t('service.apiList.test.stabilityTest') }}：</span> <div class="flex-1 min-w-0" :class="getResultColor(item.testStability, item.testStabilityPassedFlag)">{{ !item.testStability ? t('status.disabled') : item.testStabilityPassedFlag ? t('status.passed') : item.testStabilityPassedFlag === false ? t('status.notPassed') : t('status.notTested') }} <span class="text-status-error">{{ item.testStabilityFailureMessage }}</span></div></div>
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
              <template v-for="record in myCollapseButtonGroup" :key="record.value">
                <Button
                  type="text"
                  class="!bg-transparent"
                  :disabled="getBtnDisabled(record, item)"
                  @click.stop="handleClick(record.value, item, index)">
                  <Icon :icon="record.icon" />
                  {{ record.label }}
                </Button>
              </template>
            </div>
            <div>
              <Dropdown
                :menuItems="myButtonGroup(item)"
                :permissions="[...apiAuths, ...mockAuth]"
                :destroyPopupOnHide="true"
                @click="$event =>handleClick($event.key, item, index)">
                <Icon icon="icon-gengduo" />
              </Dropdown>
            </div>
          </div>
        </div>
      </ListItem>
      <template v-else>
        <div
          class="h-11.5 border mb-3 w-full flex items-center"
          @click="handleClickSpread(item, index)">
          <div v-if="groupedBy === 'createdBy'" class="flex items-center flex-1 px-2">
            <Image
              type="avatar"
              :src="item.avatar"
              class="w-7.5 h-7.5 rounded-full" />
            <span class="flex-1 ml-7.5">{{ item.createdByName }}</span>
            <!-- <p><span class="rounded-full bg-gray-light px-2">{{ item.childrenNum }}</span></p> -->
          </div>
          <div v-else-if="groupedBy === 'ownerId'" class="flex items-center flex-1 px-2">
            <Image
              type="avatar"
              :src="item.avatar"
              class="w-7.5 h-7.5 rounded-full" />
            <span class="flex-1 ml-7.5">{{ item.ownerName }}</span>
            <!-- <p><span class="rounded-full bg-gray-light px-2">{{ item.childrenNum }}</span></p> -->
          </div>
          <div v-else-if="groupedBy === 'tag'" class="flex items-center flex-1 px-2">
            <span class="flex-1 ml-7.5">{{ item.name }}</span>
            <!-- <p><span class="rounded-full bg-gray-light px-2">{{ item.childrenNum }}</span></p> -->
          </div>
          <div v-else class="flex flex-1 px-2">
            <span class="w-20 block h-7 text-center rounded text-white leading-7" :class="bgColor[item.method]">{{ item.method }}</span>
          </div>
          <p class="px-2"><span class="rounded-full bg-gray-light px-2">{{ item.childrenNum }}</span></p>
          <Icon
            icon="icon-xiangshang-copy"
            class="mr-3 transition-all"
            :class="{'rotate-90': groupSpreadMap[item.key]}" />
        </div>
      </template>
    </template>
  </VirtualList>
</template>

  <style scoped>
  :deep(.deprecated) {
    filter: grayscale(1);
  }

  :deep(.ant-btn-text) {
    @apply text-text-sub-content;
  }

  :deep(.ant-btn) {
    /* stylelint-disable-next-line CssSyntaxError */
    @apply text-3 leading-3 px-2.25;
  }

  :deep(.ant-list-item) {
    @apply p-0 border-b-0;
  }

  .edit-btn-wrapper button[disabled] {
    color: rgba(0, 0, 0, 25%);
  }

  .show {
    @apply block;
  }

  .show1 {
    @apply hidden;
  }

  .publish-wrapper {
    max-width: calc(100% - 1rem);
  }

  @media(max-width: 1300px) {
    .show {
      @apply hidden;
    }

    .show1 {
      @apply block;
    }
  }

  .has-border::before {
    content: "";
    position: absolute;
    bottom: 0;
    left: 0;
    width: 0;
    height: 58px;
    border-left: 1px solid rgb(229, 231, 235);
  }

  .has-border::after {
    content: "";
    position: absolute;
    right: 0;
    bottom: 0;
    width: 0;
    height: 58px;
    border-right: 1px solid rgb(229, 231, 235);
  }

  .last-data > div {
    @apply mb-3;
  }

  .last-data::before {
    height: 70px;
  }

  .last-data::after {
    height: 70px;
  }
  </style>
