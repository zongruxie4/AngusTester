<script setup lang="ts">
import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Modal, Tooltip, notification, SelectApisByService } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { apis, paramTarget } from '@/api/tester';

import { ApiInfo } from '@/plugins/test/types';

const { t } = useI18n();

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

const linking = ref(false);
const coping = ref(false);
const serviceId = ref<string>();
const checkedApiIds = ref<string[]>([]);

const inputValue = ref<string>();
const apiList = ref<ApiItem[]>([]);

const handleChangeApis = (ids: string[]) => {
  checkedApiIds.value = ids;
};

const ok = async (key:'link'|'copy') => {
  if (key === 'copy') {
    coping.value = true;
  } else {
    const hasLinkIdFlag = checkedApiIds.value.some(item => props.linkIds.has(item));
    if (hasLinkIdFlag) {
      notification.info(t('httpPlugin.uiConfig.selectApiModal.alreadyReferenced'));
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

</script>
<template>
  <Modal
    :title="t('httpPlugin.uiConfig.selectApiModal.title')"
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
          <span>{{ t('actions.cancel') }}</span>
        </Button>
        <Button
          :loading="coping"
          :disabled="copyButtonDisabled"
          size="small"
          type="primary"
          @click="ok('copy')">
          <span>{{ t('actions.copy') }}</span>
        </Button>
        <Button
          :loading="linking"
          :disabled="linkButtonDisabled"
          size="small"
          type="primary"
          @click="ok('link')">
          <div v-if="hasLinkId" class="flex items-center justify-center w-2.5 h-2.5 rounded-lg bg-white mr-1">
            <Tooltip :title="t('httpPlugin.uiConfig.selectApiModal.alreadyReferenced')">
              <Icon icon="icon-tishi1" class="flex-shrink-0 text-3.5 text-status-warn" />
            </Tooltip>
          </div>
          <span>{{ t('httpPlugin.uiConfig.selectApiModal.reference') }}</span>
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
