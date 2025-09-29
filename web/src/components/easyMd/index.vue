<script lang="ts" setup>
// Vue core imports
import { onMounted, watch, ref } from 'vue';

// Third-party imports
import EasyMDE from '@xcan-angus/easymde-enhanced';
import '@xcan-angus/easymde-enhanced/dist/easymde.min.css';

/**
 * Component props interface for EasyMD editor
 */
interface Props {
  value?: string;
  preview?: boolean;
}
// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  value: '',
  preview: false // Preview mode flag
});

// Component refs and state
const textareaElementRef = ref();
const easyMDEInstance = ref();
const editorValue = ref();

// Component initialization and watchers
onMounted(() => {
  /**
   * Watch for value changes and initialize or update editor
   */
  watch(() => props.value, () => {
    editorValue.value = props.value;
    if (!easyMDEInstance.value) {
      easyMDEInstance.value = new EasyMDE({
        initialValue: editorValue.value,
        element: textareaElementRef.value,
        autoDownloadFontAwesome: true,
        toolbar: props.preview ? false : undefined,
        spellChecker: false,
        status: props.preview ? false : ['autosave', 'lines', 'words', 'cursor'],
        minHeight: props.preview ? 'auto' : undefined
      });
    } else {
      easyMDEInstance.value.value(editorValue.value);
    }

    if (props.preview && !easyMDEInstance.value.isPreviewActive()) {
      easyMDEInstance.value.togglePreview();
    }
  }, {
    immediate: true
  });

  /**
   * Watch for preview mode changes and toggle preview accordingly
   */
  watch(() => props.preview, (isPreviewMode) => {
    if (isPreviewMode && !easyMDEInstance.value.isPreviewActive()) {
      easyMDEInstance.value.togglePreview();
    }

    if (!isPreviewMode && easyMDEInstance.value.isPreviewActive()) {
      easyMDEInstance.value.togglePreview();
    }
  });
});

// Component exposure
defineExpose({
  /**
   * Get the current editor value
   */
  getEditorValue: () => {
    return easyMDEInstance.value.value();
  }
});
</script>
<template>
  <div class="revert-style text-4" :class="{'preview-wrapper': !!props.preview }">
    <textarea ref="textareaElementRef"></textarea>
  </div>
</template>
<style scoped>
.preview-wrapper > .EasyMDEContainer > .editor-toolbar[role="toolbar"]{
  display: none;
}
.revert-style h1,
.revert-style h2,
.revert-style h3,
.revert-style h4,
.revert-style h5,
.revert-style h6,
.revert-style p,
.revert-style table,
.revert-style ul,
.revert-style ol,
.revert-style pre,
.revert-style code,
.revert-style li {
  all:revert !important;
}
</style>
