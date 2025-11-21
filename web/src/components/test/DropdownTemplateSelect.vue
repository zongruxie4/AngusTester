<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { Dropdown } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { testTemplate } from '@/api/tester';
import { TestTemplateType } from '@/enums/enums';

const props = withDefaults(defineProps<{templateType?: TestTemplateType}>(), {
  templateType: TestTemplateType.TEST_PLAN
});
    
const emit = defineEmits<{
  (e: 'change', value: any): void;
}>();

const templateList = ref<{name: string, key: string}[]>([]);
const templateData = ref<{[key: string]: any}[]>([]);

const loadTemplateList = async () => {
  const [error, res] = await testTemplate.getTemplateList();
  if (error) {
    return;
  }
  templateData.value = (res.data || []).filter(item => item.templateType === props.templateType);
  templateList.value = templateData.value.map(item => ({ name: item.name, key: item.id }));
};

const handleTemplateClick = ({key}: {key: string}) => {
  emit('change', templateData.value.find(item => item.id === key));
};

onMounted(() => {
  loadTemplateList();
});

</script>

<template>
  <Dropdown :menuItems="templateList" @click="handleTemplateClick">
    <Button size="small" type="primary">
      <span>选择模版</span>
    </Button>
  </Dropdown>
</template>