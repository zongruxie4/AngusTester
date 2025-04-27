<script setup lang="ts">
import { computed, ref } from 'vue';
import { Modal, SelectApi } from '@xcan-angus/vue-ui';

type APIInfo = {
  id: string;
  summary: string;
  endpoint: string;
  method: string;
  protocol: { message: string; value: string; };
  disabled: boolean;
  mockApisId: string|undefined;
}

interface Props {
  visible:boolean;
  title:string;
  type:'link'|'copy';
  width?:number;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  title: undefined,
  type: 'copy',
  width: 700
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e:'update:visible', value:boolean):void;
  (e:'ok', value:APIInfo):void;
  (e:'cancel'):void;
}>();

const apiInfo = ref<APIInfo>();

const ok = () => {
  if (!apiInfo.value) {
    return;
  }

  emit('ok', apiInfo.value);
};

const cancel = () => {
  apiInfo.value = undefined;
  emit('update:visible', false);
  emit('cancel');
};

const scrollProps = {
  params: {
    filters: [{ key: 'protocol', op: 'IN', value: ['http', 'https'] }]
  },
  format: (data:APIInfo) => {
    return {
      ...data,
      disabled: !!data.mockApisId
    };
  }
};

const change = ({ apiOptions }) => {
  apiInfo.value = apiOptions[0];
};

const okButtonProps = computed(() => {
  return {
    disabled: !apiInfo.value
  };
});

const bodyStyle = {
  height: 'calc(100% - 84px)',
  paddingTop: '4px'
};
</script>
<template>
  <Modal
    :title="props.title"
    :visible="props.visible"
    :centered="true"
    :width="props.width"
    :bodyStyle="bodyStyle"
    :okButtonProps="okButtonProps"
    class="select-api-modal-wrap"
    @ok="ok"
    @cancel="cancel">
    <SelectApi
      v-if="props.visible"
      mode="single"
      :showLinkText="true"
      :scrollProps="scrollProps"
      @change="change" />
  </Modal>
</template>

<style>
.select-api-modal-wrap {
  height: calc(70%);
}

.select-api-modal-wrap .ant-modal-content {
  height: 100%;
}
</style>
