<script setup lang="ts">
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { AssociateSelect, Modal, IconText } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { apis } from '@/api/tester';

import { ApiInfo } from './PropsType';

const { t } = useI18n();

export interface Props {
  visible:boolean;
  linkIds:Set<string>;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  linkIds: () => new Set()
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e:'update:visible', value:boolean):void;
  (e:'ok', value:ApiInfo):void;
  (e:'cancel'):void;
}>();

const checkedId = ref<string>();
const linking = ref(false);
const coping = ref(false);

const ok = async (key:'link'|'copy') => {
  if (key === 'copy') {
    coping.value = true;
  } else {
    linking.value = true;
  }
  const [error, { data }]:[Error|null, { data: ApiInfo }] = await apis.getApiDetail(checkedId.value, true);
  coping.value = false;
  linking.value = false;
  if (error) {
    return;
  }

  if (key === 'link') {
    data.apisId = data.id;
  }

  emit('ok', data);
  emit('update:visible', false);
  checkedId.value = undefined;
};

const cancel = () => {
  emit('cancel');
  emit('update:visible', false);
  checkedId.value = undefined;
};

const treeSelectProps = {
  action: `${TESTER}/project/tree`,
  fieldNames: { label: 'name', value: 'id', children: 'children' },
  label: t('websocketPlugin.uiConfig.selectApiModal.treeSelect.label'),
  placeholder: t('websocketPlugin.uiConfig.selectApiModal.treeSelect.placeholder')
};

const inputProps = {
  filterKey: 'summary',
  allowClear: true,
  placeholder: t('websocketPlugin.uiConfig.selectApiModal.input.placeholder')
};

const scrollProps = {
  action: (id: string) => {
    return `${TESTER}/project/${id}/apis`;
  },
  params: {
    filters: [{ key: 'protocol', op: 'IN', value: ['ws', 'wss'] }]
  }
};

const fields = [
  {
    key: 'uri',
    name: t('websocketPlugin.uiConfig.selectApiModal.fields.url'),
    style: {
      flex: '1 1 45%',
      overflow: 'hidden',
      'text-overflow': 'ellipsis',
      'white-space': 'nowrap'
    }
  },
  {
    key: 'summary',
    name: t('websocketPlugin.uiConfig.selectApiModal.fields.name'),
    style: {
      flex: '1 1 55%',
      overflow: 'hidden',
      'text-overflow': 'ellipsis',
      'white-space': 'nowrap'
    }
  }
];

const change = ({ ids }) => {
  checkedId.value = ids[0];
};
</script>
<template>
  <Modal
    :title="t('websocketPlugin.uiConfig.selectApiModal.title')"
    :visible="props.visible"
    :centered="true"
    :width="700"
    class="select-api-modal-wrap truncate"
    @cancel="cancel">
    <AssociateSelect
      v-if="visible"
      :treeSelectProps="treeSelectProps"
      :inputProps="inputProps"
      :scrollProps="scrollProps"
      :fields="fields"
      mode="single"
      @change="change">
      <template #method="record">
        <HttpMethodText :value="record.method" />
      </template>

      <template #summary="{summary,id}">
        <div class="flex items-center">
          <div class="flex-1 truncate">{{ summary }}</div>
          <div v-if="props.linkIds.has(id)" class="flex-shrink-0 text-theme-placeholder">{{ t('websocketPlugin.uiConfig.selectApiModal.referenced') }}</div>
        </div>
      </template>

      <template #treeSelectTitle="{ targetType, name }">
        <div class="flex items-center leading-6.5 h-6.5 space-x-1.5">
          <IconText
            style="width: 16px;height: 16px;"
            :style="'background-color:' + (targetType?.value === 'SERVICE' ? 'rgb(162 222 236)' : 'rgb(166 206 255)') + ';'"
            :text="targetType?.value === 'PROJECT' ? 'P' : 'S'"
            class="flex-shrink-0" />
          <div :title="name" class="flex-1 truncate">{{ name }}</div>
        </div>
      </template>
    </AssociateSelect>
    <template #footer>
      <Button
        :disabled="linking||coping"
        type="default"
        size="small"
        @click="cancel">
        {{ t('websocketPlugin.uiConfig.selectApiModal.buttons.cancel') }}
      </Button>
      <Button
        :loading="coping"
        :disabled="!checkedId||linking"
        size="small"
        type="primary"
        @click="ok('copy')">
        {{ t('websocketPlugin.uiConfig.selectApiModal.buttons.copy') }}
      </Button>
      <Button
        :loading="linking"
        :disabled="(!checkedId||props.linkIds.has(checkedId))||coping"
        size="small"
        type="primary"
        @click="ok('link')">
        {{ t('websocketPlugin.uiConfig.selectApiModal.buttons.link') }}
      </Button>
    </template>
  </Modal>
</template>

<style>
.select-api-modal-wrap {
  height: calc(70%);
  min-height: 480px;
}

.select-api-modal-wrap .ant-modal-content {
  height: 100%;
}

.select-api-modal-wrap .ant-modal-body {
  height: calc(100% - 84px);
}
</style>
