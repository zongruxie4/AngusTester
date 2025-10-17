<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { notification } from '@xcan-angus/vue-ui';
import YAML from 'yaml';

export interface Props {
  visible:boolean;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
});


const emit = defineEmits<{
  (e:'update:visible', value:boolean):void;
  (e:'ok', value:{[key:string]:any}):void;
}>();

const inputRef = ref();

const selectFile = () => {
  if (typeof inputRef.value?.click !== 'function') {
    return;
  }

  inputRef.value.click();
  setTimeout(() => {
    emit('update:visible', false);
  }, 300);
};

const change = (event) => {
  const files = event.target?.files as File[];
  if (!files?.length) {
    return;
  }

  const isJsonFile = /(.json)$/.test(files[0].name);

  // 使用文件路径打开文件
  const fileReader = new FileReader();
  fileReader.readAsText(files[0]);

  // 监听文件读取完成事件
  fileReader.onload = () => {
    // 重置选择文件内容
    inputRef.value.value = '';

    // 获取文件内容
    let fileContent = fileReader.result as string;
    if (isJsonFile) {
      try {
        fileContent = JSON.parse(fileContent);
        emit('ok', fileContent);
      } catch (error) {
        notification.error(error.message);
      }

      return;
    }

    try {
      fileContent = fileContent.replace(/\\\n/g, '');
      fileContent = YAML.parse(fileContent);
      emit('ok', fileContent);
    } catch (error) {
      notification.error(error.message);
    }
  };
};

onMounted(() => {
  watch(() => props.visible, (newValue) => {
    if (!newValue) {
      return;
    }

    selectFile();
  }, { immediate: true });
});
</script>
<template>
  <input
    ref="inputRef"
    type="file"
    accept=".yml,.yaml,.json"
    @change="change" />
</template>
