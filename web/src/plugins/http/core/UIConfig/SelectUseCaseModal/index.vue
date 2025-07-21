<script setup lang="ts">
import { ref, computed, inject, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { TESTER, duration } from '@xcan-angus/tools';
import { debounce } from 'throttle-debounce';
import { Icon, Modal, Tooltip, notification, SelectApisCase } from '@xcan-angus/vue-ui';
import { paramTarget, apis } from '@/api/tester';

import { UseCaseInfo } from './PropsType';

export type UseCaseItem = {
  id: string;
  name: string;
  endpoint: string;
  method: string;
  protocol: { message: string; value: string; };
  disabled: boolean;
}

export interface Props {
  visible:boolean;
  linkIds:Set<string>;
}

const projectInfo = inject('projectInfo', ref({ id: '' }));
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  linkIds: () => new Set()
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e:'update:visible', value:boolean):void;
  (e:'ok', value:UseCaseInfo[]):void;
  (e:'cancel'):void;
}>();

const dirTreeSelectRef = ref();
const selectApisCaseRef = ref();

const linking = ref(false);
const coping = ref(false);
const serviceId = ref<string>();
const checkedApiId = ref<string>();
const checkedUseCaseIds = ref<string[]>([]);
const apisId = ref();
const checkedApisIds = ref<string[]>([]);
// const variableStrategy = ref<'ALL'|'REF'|'IGNORE'>('REF');

const inputValue = ref<string>();
const useCaseList = ref<UseCaseItem[]>([]);

const dirChange = (id: string) => {
  if (serviceId.value === id) {
    return;
  }

  serviceId.value = id;
  checkedApiId.value = undefined;
  checkedUseCaseIds.value = [];
  checkedApisIds.value = [];
};

const planChange = (id:string) => {
  if (checkedApiId.value === id) {
    return;
  }

  checkedApiId.value = id;
  checkedUseCaseIds.value = [];
  checkedApisIds.value = [];
};

const checkChange = (event:{target:{checked:boolean}}, data) => {
  const checked = event.target.checked;
  const id = data.id;
  const apisId = data.apisId;
  if (checked) {
    if (!checkedUseCaseIds.value.includes(id)) {
      checkedUseCaseIds.value.push(id);
    }

    if (!checkedApisIds.value.includes(apisId)) {
      checkedApisIds.value.push(apisId);
    }

    return;
  }

  checkedUseCaseIds.value = checkedUseCaseIds.value.filter(item => item !== id);
  checkedApisIds.value = checkedApisIds.value.filter(item => item !== apisId);
};

const inputChange = debounce(duration.search, (event: { target: { value: string } }) => {
  inputValue.value = event.target.value;
});

const scrollChange = (data: UseCaseItem[]) => {
  useCaseList.value = data;
};

const changeSelectCase = (caseIds: string[], cases: any[]) => {
  checkedUseCaseIds.value = caseIds;
  apisId.value = cases?.[0]?.apisId || undefined;
};

const ok = async (key:'link'|'copy') => {
  if (key === 'copy') {
    coping.value = true;
  } else {
    const hasLinkIdFlag = checkedUseCaseIds.value.some(item => props.linkIds.has(item));
    if (hasLinkIdFlag) {
      notification.info('已选中的用例中包含已被引用的用例，不能重复引用。');
      return;
    }

    linking.value = true;
  }

  const results:UseCaseInfo[] = [];
  const ids = checkedUseCaseIds.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const [error, { data }]:[Error|null, { data: UseCaseInfo }] = await apis.getCaseDetail(id, { silence: false });
    if (error) {
      coping.value = false;
      linking.value = false;
      return;
    }

    if (key === 'link') {
      data.caseId = id;
    }
    // 查询变量
    const [_error, { data: _data }] = await paramTarget.getVariable(apisId.value, 'API', { silence: false });
    if (_error) {
      coping.value = false;
      linking.value = false;
      return;
    }

    data._variables = (_data || []).map((item) => {
      return {
        ...item,
        ...item.extraction,
        source: 'HTTP_SAMPLING'
      };
    });

    // 查询数据集
    const [_error2, { data: _data2 }] = await paramTarget.getDataSet(apisId.value, 'API', { silence: false });
    if (_error2) {
      coping.value = false;
      linking.value = false;
      return;
    }

    data.datasets = _data2 || [];

    results.push(data);
  }

  coping.value = false;
  linking.value = false;
  emit('ok', results);
  emit('update:visible', false);
  reset();
};

const cancel = () => {
  emit('cancel');
  emit('update:visible', false);
  reset();
};

const reset = () => {
  serviceId.value = undefined;
  checkedApiId.value = undefined;
  checkedUseCaseIds.value = [];
  // checkedApisIds.value = [];
  apisId.value = undefined;
  // variableStrategy.value = 'REF';
  inputValue.value = undefined;
  useCaseList.value = [];
  if (typeof dirTreeSelectRef.value?.clearAll === 'function') {
    dirTreeSelectRef.value.clearAll();
  }
};

const useCaseAction = computed(() => {
  if (!serviceId.value || !checkedApiId.value) {
    return undefined;
  }

  return `${TESTER}/apis/case/search?apisId=${checkedApiId.value}`;
});

const useCaseParams = computed(() => {
  const filters: {
    key: 'name';
    op: 'MATCH_END',
    value: string
  }[] = [];

  if (inputValue.value) {
    filters.push({
      key: 'name',
      op: 'MATCH_END',
      value: inputValue.value
    });
  }

  return { filters };
});

const cancelButtonDisabled = computed(() => {
  return coping.value || linking.value;
});

const copyButtonDisabled = computed(() => {
  if (!checkedUseCaseIds.value.length || linking.value) {
    return true;
  }

  return false;
});

const linkButtonDisabled = computed(() => {
  if (!checkedUseCaseIds.value.length || coping.value) {
    return true;
  }

  return false;
});

const hasLinkId = computed(() => {
  return checkedUseCaseIds.value.some(item => props.linkIds.has(item));
});

watch(() => props.visible, () => {
  if (selectApisCaseRef.value) {
    selectApisCaseRef.value && selectApisCaseRef.value.reset();
  }
});
</script>
<template>
  <Modal
    title="选择用例"
    :visible="props.visible"
    :centered="true"
    :width="1000"
    class="select-api-case-modal-wrap"
    @ok="ok"
    @cancel="cancel">
    <SelectApisCase
      v-if="props.visible"
      ref="selectApisCaseRef"
      :visible="props.visible"
      :projectId="projectInfo.id"
      @change="changeSelectCase" />
    <!-- <div class="h-full text-3 leading-5 text-theme-content">
      <div class="flex items-center mb-5">
        <div class="flex items-center flex-shrink-0 w-20.5">
          <IconRequired />
          <span>所属服务</span>
        </div>
        <Select
          v-if="props.visible"
          ref="dirTreeSelectRef"
          :action="`${TESTER}/services/search?projectId=${projectInfo.id}`"
          :allowClear="true"
          :fieldNames="{ label: 'name', value: 'id'}"
          placeholder="请选择服务"
          showSearch
          class="w-1/2"
          @change="dirChange">
          <template #title="{ name }">
            <div class="flex items-center leading-6.5 h-6.5 space-x-1.5">
              <IconText
                style="width: 16px;height: 16px;background-color: rgb(162, 222, 236);"
                text="S"
                class="flex-shrink-0" />
              <div :title="name" class="flex-1 truncate">{{ name }}</div>
            </div>
          </template>
        </Select>
      </div>

      <div class="flex items-center mb-5">
        <div class="flex items-center flex-shrink-0 w-20.5">
          <IconRequired />
          <span>接口</span>
        </div>
        <Select
          :allowClear="true"
          :action="`${TESTER}/services/${serviceId}/apis/search`"
          :fieldNames="{ label: 'summary', value: 'id' }"
          :disabled="!serviceId"
          placeholder="请选择接口"
          showSearch
          class="w-1/2"
          @change="planChange">
          <template #title="{ summary }">
            <div class="flex items-center leading-6.5 h-6.5 space-x-1.5">
              <div :title="summary" class="flex-1 truncate">{{ summary }}</div>
            </div>
          </template>
        </Select>
      </div>

      <div style="height: calc(100% - 138px);" class="pt-3.5 px-3.5 space-y-3 border rounded border-theme-text-box">
        <Input
          :value="inputValue"
          :allowClear="true"
          placeholder="查询用例名称"
          trim
          @change="inputChange">
          <template #suffix>
            <Icon icon="icon-sousuo" />
          </template>
        </Input>

        <div style="height: calc(100% - 38px);">
          <div class="bg-table-header flex items-center px-2.5 h-8 leading-5 rounded space-x-3">
            <div class="w-4 h-5 flex items-center justify-center flex-shrink-0"></div>
            <div class="flex-shrink-0 w-13.75">方法</div>
            <div style="flex:1 1 45%">URL</div>
            <div style="flex:1 1 55%">名称</div>
          </div>

          <Scroll
            style="height: calc(100% - 32px);"
            :action="useCaseAction"
            :lineHeight="32"
            :params="useCaseParams"
            class="py-1"
            @change="scrollChange">
            <div
              v-for="item in useCaseList"
              :key="item.id"
              :class="{'api-item-disabled':item.disabled}"
              class="api-item flex items-center h-7 px-2.5 leading-5 mb-1 rounded space-x-3 cursor-pointer">
              <div class="w-4 h-5 flex items-center justify-center flex-shrink-0">
                <Checkbox
                  :disabled="item.disabled"
                  :checked="checkedUseCaseIds.includes(item.id)"
                  class="checkbox-box-white"
                  @change="checkChange($event,item)" />
              </div>
              <HttpMethodText class="flex-shrink-0 w-13.75" :value="item.method" />
              <div
                :title="item.endpoint"
                style="flex:1 1 45%"
                class="flex items-center truncate">
                {{ item.endpoint }}
              </div>
              <div
                :title="item.name"
                style="flex:1 1 55%"
                class="flex items-center truncate">
                <div class="flex-1 truncate">{{ item.name }}</div>
                <div v-if="props.linkIds.has(item.id)" class="flex-shrink-0 text-theme-placeholder">（已引用）</div>
              </div>
            </div>
          </Scroll>
        </div>
      </div>
    </div> -->

    <template #footer>
      <div class="flex items-center justify-end">
        <Button
          :disabled="cancelButtonDisabled"
          type="default"
          size="small"
          @click="cancel">
          <span>取消</span>
        </Button>
        <Button
          :loading="coping"
          :disabled="copyButtonDisabled"
          size="small"
          type="primary"
          @click="ok('copy')">
          <span>复制</span>
        </Button>
        <Button
          :loading="linking"
          :disabled="linkButtonDisabled"
          size="small"
          type="primary"
          @click="ok('link')">
          <div v-if="hasLinkId" class="flex items-center justify-center w-2.5 h-2.5 rounded-lg bg-white mr-1">
            <Tooltip title="已选中的用例中包含已被引用的用例，不能重复引用。">
              <Icon icon="icon-tishi1" class="flex-shrink-0 text-3.5 text-status-warn" />
            </Tooltip>
          </div>
          <span>引用</span>
        </Button>
      </div>
    </template>
  </Modal>
</template>

<style>
.select-api-case-modal-wrap {
  min-height: 480px;
}

.select-api-case-modal-wrap .ant-modal-content {
  height: 100%;
}
</style>

<style scoped>
.bg-table-header {
  background-color: rgba(247, 248, 251, 100%);
}

.checkbox-box-white :deep(.ant-checkbox) {
  background-color: #fff;
}

.api-item:hover {
  background-color: var(--content-tabs-bg-hover);
}

.api-item.api-item-disabled{
  opacity: 0.75;
  cursor: not-allowed;
}

.api-item.api-item-disabled :deep(.ant-checkbox-input) {
  cursor: not-allowed;
}

.ant-checkbox-wrapper{
  align-items:center;
  line-height: 20px;
}

:deep(.ant-checkbox) {
  top:0;
}
</style>
