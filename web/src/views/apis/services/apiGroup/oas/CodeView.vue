<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { AsyncComponent, MonacoEditor } from '@xcan-angus/vue-ui';

interface Props {
  value: string;
  visible: boolean;
  selectStr?: string;
  startKey?: string[];
}

const props = withDefaults(defineProps<Props>(), {
  value: '',
  visible: true
});

const monacoEditorRef = ref();
const selectRange = ref({});

onMounted(() => {
  watch(() => props.selectStr, newValue => {
    if (monacoEditorRef.value) {
      monacoEditorRef.value.removeDecoration();
    }
    if (!newValue) {
      selectRange.value = {};
      return;
    }

    let lineNumber = 1;
    const targetStrs = newValue.split('\n');
    const targetFirstLineStr = targetStrs[0];
    const allStrs = props.value.split('\n');
    const startKeys = props.startKey || [];
    for (let i = 0; i < allStrs.length; i++) {
      if (startKeys.length < 1 && allStrs[i].trim().startsWith(targetFirstLineStr.trim())) {
        lineNumber = 1 + i;
        break;
      } else if (startKeys.length) {
        if (allStrs[i].trim() === (startKeys[0].trim() + ':')) {
          startKeys.splice(0, 1);
        }
      }
    }
    setTimeout(() => {
      monacoEditorRef.value.decorations([lineNumber, 1, lineNumber + targetStrs.length - 2, 1]);
    }, 500);
  }, {
    immediate: true,
    deep: true
  });
});

</script>
<template>
  <AsyncComponent :visible="props.visible">
    <MonacoEditor
      v-show="props.visible"
      ref="monacoEditorRef"
      :readOnly="true"
      :value="props.value"
      language="yaml"
      class="h-full flex-1">
    </MonacoEditor>
  </AsyncComponent>
</template>
