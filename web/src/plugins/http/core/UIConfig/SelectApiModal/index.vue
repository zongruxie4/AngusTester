<script setup lang="ts">
import { ref, computed, inject } from 'vue';
import { Icon, Modal, Tooltip, notification, SelectApisByService } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { TESTER, duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { apis, paramTarget } from '@/api/tester';

import { ApiInfo } from './PropsType';

export type ApiItem = {
  id: string;
  summary: string;
  endpoint: string;
  method: string;
  protocol: { message: string; value: string; };
  disabled: boolean;
}

export interface Props {
  visible:boolean;
  linkIds:Set<string>;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  linkIds: () => new Set(),
  projectId: ''
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e:'update:visible', value:boolean):void;
  (e:'ok', value:ApiInfo[]):void;
  (e:'cancel'):void;
}>();

const projectInfo = inject('projectInfo', ref({ id: '' }));

const linking = ref(false);
const coping = ref(false);
const serviceId = ref<string>();
const checkedApiIds = ref<string[]>([]);

const inputValue = ref<string>();
const apiList = ref<ApiItem[]>([]);

const groupedKey = ref('-');

const handleChangeApis = (ids: string[]) => {
  checkedApiIds.value = ids;
};

const grouped = (groupKey: string) => {
  groupedKey.value = groupKey;
};

const dirChange = (id: string) => {
  if (serviceId.value === id) {
    return;
  }

  serviceId.value = id;
  checkedApiIds.value = [];
};

const checkChange = (event:{target:{checked:boolean}}, id:string) => {
  const checked = event.target.checked;
  if (checked) {
    if (!checkedApiIds.value.includes(id)) {
      checkedApiIds.value.push(id);
    }

    return;
  }

  checkedApiIds.value = checkedApiIds.value.filter(item => item !== id);
};

const inputChange = debounce(duration.search, (event: { target: { value: string } }) => {
  inputValue.value = event.target.value;
});

const scrollChange = (data: ApiItem[]) => {
  apiList.value = data;
};

const ok = async (key:'link'|'copy') => {
  if (key === 'copy') {
    coping.value = true;
  } else {
    const hasLinkIdFlag = checkedApiIds.value.some(item => props.linkIds.has(item));
    if (hasLinkIdFlag) {
      notification.info('已选中的接口中包含已被引用的接口，不能重复引用。');
      return;
    }

    linking.value = true;
  }

  const results:ApiInfo[] = [];
  const ids = checkedApiIds.value;
  for (let i = 0, len = ids.length; i < len; i++) {
    const id = ids[i];
    const [error, { data }]:[Error|null, { data: ApiInfo }] = await apis.getApiDetail(id, true, { silence: false });
    if (error) {
      coping.value = false;
      linking.value = false;
      return;
    }

    if (key === 'link') {
      data.apisId = id;
    }

    // 查询变量
    const [_error, { data: _data }] = await paramTarget.getVariable(id, 'API', { silence: false });
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
    const [_error2, { data: _data2 }] = await paramTarget.getDataSet(id, 'API', { silence: false });
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
  checkedApiIds.value = [];
  inputValue.value = undefined;
  apiList.value = [];
};

const apiAction = computed(() => {
  if (!serviceId.value) {
    return undefined;
  }
  return `${TESTER}/services/${serviceId.value}/apis`;
});

const apiParams = computed(() => {
  const filters: {
    key: string;
    op: 'MATCH_END'|'IN',
    value: string|string[];
  }[] = [{ key: 'protocol', op: 'IN', value: ['http', 'https'] }];

  if (inputValue.value) {
    filters.push({
      key: 'summary',
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
  if (!checkedApiIds.value.length || linking.value) {
    return true;
  }

  return false;
});

const linkButtonDisabled = computed(() => {
  if (!checkedApiIds.value.length || coping.value) {
    return true;
  }

  return false;
});

const hasLinkId = computed(() => {
  return checkedApiIds.value.some(item => props.linkIds.has(item));
});

const groupList = [
  {
    key: '-',
    name: '不分组'
  },
  {
    key: 'createdBy',
    name: '按添加人分组'
  },
  {
    key: 'method',
    name: '按请求方法分组'
  },
  {
    key: 'ownerId',
    name: '按负责人分组'
  },
  {
    key: 'tag',
    name: '按标签分组'
  }
];
</script>
<template>
  <Modal
    title="选择接口"
    :visible="props.visible"
    :centered="true"
    :width="1000"
    class="select-api-modal-wrap"
    @ok="ok"
    @cancel="cancel">
    <SelectApisByService
      v-if="props.visible"
      :projectId="props.projectId"
      @change="handleChangeApis" />
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
            <Tooltip title="已选中的接口中包含已被引用的接口，不能重复引用。">
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
.select-api-modal-wrap {
  min-height: 480px;
}

.select-api-modal-wrap .ant-modal-content {
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
