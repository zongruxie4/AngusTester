<script setup lang="ts">
import { ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AssociateSelect, Modal, IconText } from '@xcan-angus/vue-ui';
import { http, TESTER } from '@xcan-angus/tools';

import { ApiInfo } from './PropsType';

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
  const [error, { data }]:[Error|null, { data: ApiInfo }] = await http.get(`${TESTER}/apis/${checkedId.value}`, { resolveRefFlag: true });
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
  label: '项目/服务',
  placeholder: '请选择项目/服务'
};

const inputProps = {
  filterKey: 'summary',
  allowClear: true,
  placeholder: '查询接口名称'
};

const scrollProps = {
  action: (id: string) => {
    return `${TESTER}/project/${id}/apis/search`;
  },
  params: {
    filters: [{ key: 'protocol', op: 'IN', value: ['ws', 'wss'] }]
  }
};

const fields = [
  {
    key: 'uri',
    name: 'URL',
    style: {
      flex: '1 1 45%',
      overflow: 'hidden',
      'text-overflow': 'ellipsis',
      'white-space': 'nowrap'
    }
  },
  {
    key: 'summary',
    name: '名称',
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
    title="选择接口"
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
          <div v-if="props.linkIds.has(id)" class="flex-shrink-0 text-theme-placeholder">（已引用）</div>
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
        取消
      </Button>
      <Button
        :loading="coping"
        :disabled="!checkedId||linking"
        size="small"
        type="primary"
        @click="ok('copy')">
        复制
      </Button>
      <Button
        :loading="linking"
        :disabled="(!checkedId||props.linkIds.has(checkedId))||coping"
        size="small"
        type="primary"
        @click="ok('link')">
        引用
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
