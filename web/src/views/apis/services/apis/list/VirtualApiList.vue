<script setup lang="ts">
import { Button, ListItem, Popover, Tooltip } from 'ant-design-vue';
import { computed, inject, onMounted, ref, watch, Ref } from 'vue';
import { Dropdown, HttpMethodTag, Icon, Image } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { ServicesPermission, ApiStatus } from '@/enums/enums';

import { API_STATUS_COLOR_CONFIG } from '@/utils/apis';
import { bgColor } from '@/utils/common';
import { ButtonGroup, CollapseButtonGroup } from './types';

import VirtualList from './BaseVirtualList.vue';
import {ApisListInfo} from "@/views/apis/services/protocol/types";

interface Props {
  dataSource: Array<any>; // Mixed array of ApisListInfo and group headers
  updateData: (value: { id: string; auth: boolean; }) => void;
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

// Project type visibility configuration (injected from parent)
const proTypeShowMap = inject<Ref<{ [key: string]: boolean }>>('proTypeShowMap', ref({
  showTask: true,
  showBackLog: true,
  showMeeting: true,
  showSprint: true,
  showTasStatistics: true
}));

const emits = defineEmits<{
  (e: 'loadApis'): void,
  (e: 'openMock'),
  (e: 'edit', value): void;
  (e: 'showInfo', id: string, value): void;
  (e: 'handleClick', value: string, item, index: number): void;
}>();

// Open API editor tab for this row
const openApiEditorTab = (value: ApisListInfo): void => {
  emits('edit', value);
};

// Permission arrays injected from parent
const apiAuths = inject('apiAuths', ref());
const serviceAuths = inject('serviceAuths', ref());

// Build dropdown menu items based on item state and permissions
const buildDropdownMenuItems = (item) => {
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
  }).filter(Boolean) as any[];
};

// Collapsible button group for inline actions
const collapsibleButtonGroup = computed(() => {
  return CollapseButtonGroup;
});

// Check if button should be disabled based on permissions and item state
const isButtonDisabled = (btn, item) => {
  const releaseBtn = ['del'];
  if (releaseBtn.includes(btn.value)) {
    return item.status?.value === ApiStatus.RELEASED || !apiAuths.value.includes(btn.auth);
  }
  if (btn.value === 'patchClone') {
    return !serviceAuths.value.includes(btn.auth);
  }
  return !apiAuths.value.includes(btn.auth);
};

// Toggle expand/collapse details for this row
const toggleApiDetails = (id: string, api) => {
  emits('showInfo', id, api);
};

// Currently visible data (filtered by group expand/collapse state)
const visibleData = ref<any[]>([]);
// Track which groups are expanded
const groupExpandState = ref<{ [key: string]: boolean }>({});

// Text color class based on test result
const getTestResultTextColor = (testFlag, testPassd = undefined) => {
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
const getTestResultIconColor = (item) => {
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

// Emit toolbar/menu action from this row
const emitRowAction = (event: string, data: ApisListInfo, index: number) => {
  emits('handleClick', event, data, index);
};

// Mock permission based on service permissions
const mockPermissions = computed(() => {
  if (serviceAuths.value.includes(ServicesPermission.ADD)) {
    return ['MOCK'];
  }
  return [];
});

// Expand or collapse group items in visible data
const toggleGroupItems = (group, index) => {
  if (groupExpandState.value[group.key]) {
    const idx = props.dataSource.findIndex(i => i.key === group.key);
    const arr = props.dataSource.slice(idx + 1, idx + 1 + group.childrenNum);
    visibleData.value.splice(index + 1, 0, ...arr);
  } else {
    visibleData.value.splice(index + 1, group.childrenNum);
  }
};

// Handle group header click to toggle expand/collapse
const handleGroupHeaderClick = (group, index) => {
  groupExpandState.value[group.key] = !groupExpandState.value[group.key];
  toggleGroupItems(group, index);
};

onMounted(() => {
  // Reset group expand state when grouping changes
  watch(() => props.groupedBy, () => {
    groupExpandState.value = {};
  }, {
    immediate: true
  });

  // Handle data source changes and manage group expand/collapse
  watch(() => props.dataSource, (newValue, oldValue) => {
    visibleData.value = JSON.parse(JSON.stringify(newValue));
    if (newValue.length && !oldValue?.length) {
      const groups = props.dataSource.filter(item => item.type === 'group');
      groups.forEach(group => {
        groupExpandState.value[group.key] = false;
      });
      // Expand first group by default
      if (groups?.[0]) {
        groupExpandState.value[groups[0].key] = true;
      }
      // Show only first group items initially
      if (groups?.[1]) {
        const secondGroupIdx = props.dataSource.findIndex(item => item.key === groups[1].key);
        visibleData.value = props.dataSource.filter((item, idx) => {
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
      // Handle existing groups based on current expand state
      const groups = newValue.filter(item => item.type === 'group');
      groups.forEach(group => {
        if (!groupExpandState.value[group.key]) {
          const idx = visibleData.value.findIndex(item => item.type === 'group' && item.key === group.key);
          toggleGroupItems(group, idx);
        }
      });
    }
  }, {
    immediate: true
  });
});
</script>

<template>
  <VirtualList
    class="pr-4"
    :data="visibleData"
    :height="props.height"
    :showNum="props.showNum"
    :cache="20">
    <template #default="{item, index}">
      <ListItem
        v-if="item.id"
        :key="item.id"
        class="mb-3 w-full p-0 cursor-pointer"
        :class="{
          'deprecated line-through': item.deprecated,
          'has-border relative': props.groupedBy !== 'none',
          '!border-b last-data': visibleData[index + 1]?.type === 'group' || (props.groupedBy !== 'none' && !visibleData[index + 1])
        }"
        @dblclick="openApiEditorTab(item)"
        @click="toggleApiDetails(item.id, item)">
        <div
          class="flex w-full h-11.5  rounded justify-between px-5 text-3 leading-3.5"
          :class="{
            'bg-gray-300': props.activeApiId === item.id,
            'bg-gray-light': props.activeApiId !== item.id, 'mx-4': props.groupedBy !== 'none'
          }">
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
                  :class="getTestResultIconColor(item)" />
              </span>
              <template #content>
                <div class="flex">
                  <span class="w-20">{{ t('service.apiList.test.functionalTest') }}：</span>
                  <div class="flex-1 min-w-0" :class="getTestResultTextColor(item.testFunc, item.testFuncPassed)">
                    {{
                      !item.testFunc ? t('status.disabled') : item.testFuncPassed
                        ? t('status.passed') : item.testFuncPassed === false ? t('status.notPassed') : t('status.notTested')
                    }} <span class="text-status-error">{{ item.testFuncFailureMessage }}</span>
                  </div>
                </div>
                <div class="flex">
                  <span class="w-20">{{ t('service.apiList.test.performanceTest') }}：</span>
                  <div class="flex-1 min-w-0" :class="getTestResultTextColor(item.testPerf, item.testPerfPassed)">
                    {{
                      !item.testPerf ? t('status.disabled') : item.testPerfPassed
                        ? t('status.passed') : item.testPerfPassed === false ? t('status.notPassed') : t('status.notTested')
                    }} <span class="text-status-error">{{ item.testPerfFailureMessage }}</span>
                  </div>
                </div>
                <div class="flex">
                  <span class="w-20">{{ t('service.apiList.test.stabilityTest') }}：</span>
                  <div class="flex-1 min-w-0" :class="getTestResultTextColor(item.testStability, item.testStabilityPassed)">
                    {{
                      !item.testStability ? t('status.disabled') : item.testStabilityPassed
                        ? t('status.passed') : item.testStabilityPassed === false ? t('status.notPassed') : t('status.notTested')
                    }} <span class="text-status-error">{{ item.testStabilityFailureMessage }}</span>
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
          <div
            class="justify-end flex items-center btns-wrapper"
            :class="[props.activeApiId === item.id ? 'visible' : 'invisible']">
            <div class="whitespace-nowrap edit-btn-wrapper">
              <template v-for="record in collapsibleButtonGroup" :key="record.value">
                <Button
                  type="text"
                  class="!bg-transparent"
                  :disabled="isButtonDisabled(record, item)"
                  @click.stop="emitRowAction(record.value, item, index)">
                  <Icon :icon="record.icon" />
                  {{ record.label }}
                </Button>
              </template>
            </div>
            <div>
              <Dropdown
                :menuItems="buildDropdownMenuItems(item)"
                :permissions="[...apiAuths, ...mockPermissions]"
                :destroyPopupOnHide="true"
                @click="$event =>emitRowAction($event.key, item, index)">
                <Icon icon="icon-gengduo" />
              </Dropdown>
            </div>
          </div>
        </div>
      </ListItem>
      <template v-else>
        <div
          class="h-11.5 border mb-3 w-full flex items-center"
          @click="handleGroupHeaderClick(item, index)">
          <div v-if="groupedBy === 'createdBy'" class="flex items-center flex-1 px-2">
            <Image
              type="avatar"
              :src="item.avatar"
              class="w-7.5 h-7.5 rounded-full" />
            <span class="flex-1 ml-7.5">{{ item.createdByName }}</span>
          </div>
          <div v-else-if="groupedBy === 'ownerId'" class="flex items-center flex-1 px-2">
            <Image
              type="avatar"
              :src="item.avatar"
              class="w-7.5 h-7.5 rounded-full" />
            <span class="flex-1 ml-7.5">{{ item.ownerName }}</span>
          </div>
          <div v-else-if="groupedBy === 'tag'" class="flex items-center flex-1 px-2">
            <span class="flex-1 ml-7.5">{{ item.name }}</span>
          </div>
          <div v-else class="flex flex-1 px-2">
            <span
              class="w-20 block h-7 text-center rounded text-white leading-7"
              :class="bgColor[item.method]">{{ item.method }}</span>
          </div>
          <p class="px-2"><span class="rounded-full bg-gray-light px-2">{{ item.childrenNum }}</span></p>
          <Icon
            icon="icon-xiangshang-copy"
            class="mr-3 transition-all"
            :class="{'rotate-90': groupExpandState[item.key]}" />
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

@media (max-width: 1300px) {
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
