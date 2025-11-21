<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { Dropdown, Select } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { testTemplate } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { TestTemplateType } from '@/enums/enums';

const { t } = useI18n();

const props = withDefaults(defineProps<{templateType?: TestTemplateType}>(), {
  templateType: TestTemplateType.TEST_PLAN
});
    
const emit = defineEmits<{
  (e: 'change', value: any): void;
}>();

const templateList = ref<{label: string, value: string}[]>([]);
const templateData = ref<{[key: string]: any}[]>([]);

const loadTemplateList = async () => {
  const [error, res] = await testTemplate.getTemplateList();
  if (error) {
    return;
  }
  templateData.value = (res.data || []).filter(item => item.templateType === props.templateType);
  templateList.value = templateData.value.map(item => ({ label: item.name, value: item.id }));
};

const handleTemplateClick = (value: string) => {
  if (!value) {
    return;
  }
  emit('change', templateData.value.find(item => item.id === value));
};

onMounted(() => {
  loadTemplateList();
});

</script>

<template>
  <!-- <Dropdown :menuItems="templateList" @click="handleTemplateClick">
    <Button size="small" type="primary">
      <span>选择模版</span>
    </Button>
  </Dropdown> -->
  <Select
    class="!w-40"
    :options="templateList"
    allowClear
    :placeholder="t('testCase.actions.selectTemplate')"
    @change="handleTemplateClick" />
</template>