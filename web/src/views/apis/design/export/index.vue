<script lang="ts" setup>
import { ref } from 'vue';
import { Modal } from '@xcan-angus/vue-ui';
import { RadioGroup } from 'ant-design-vue';
import { download, TESTER } from '@xcan-angus/tools';

interface Props {
  visible: boolean;
  designId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  designId: undefined
});

const emits = defineEmits<{(e: 'cancel'):void; (e: 'ok'):void; (e: 'update:visible', value: boolean):void}>();

const loading = ref(false);
const exportTypeOpt = ['json', 'yaml'].map(i => ({ value: i, label: i }));

const formState = ref({
  format: 'json'
});

const cancel = () => {
  emits('update:visible', false);
};

const ok = () => {
  download(`${TESTER}/apis/design/export?id=${props.designId}&format=${formState.value.format}`)
    .then(() => {
      cancel();
    });
};

</script>
<template>
  <Modal
    title="导出"
    :visible="props.visible"
    :width="500"
    :okButtonProps="{
      loading
    }"
    @cancel="cancel"
    @ok="ok">
    <div class="flex space-x-2 items-center">
      <span>格式：</span>
      <RadioGroup
        v-model:value="formState.format"
        :options="exportTypeOpt">
      </RadioGroup>
    </div>
  </Modal>
</template>
