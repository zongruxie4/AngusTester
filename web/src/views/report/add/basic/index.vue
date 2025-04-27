<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { Checkbox, CheckboxGroup, Textarea } from 'ant-design-vue';

interface Props {
  basicInfoSetting: {
    reportContacts: string,
    reportCopyright: string,
    otherInformation: string,
    watermark: string,
  };
  viewType: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  basicInfoSetting: () => ({
    reportContacts: '',
    reportCopyright: '',
    otherInformation: '',
    watermark: ''
  }),
  viewType: false
});

const options = [
  {
    value: 'name',
    label: '展示报告名称'
  },
  {
    value: 'user',
    label: '展示报告人'
  },
  {
    value: 'createdDate',
    label: '展示报告生成时间'
  },
  {
    value: 'version',
    label: '展示报告版本'
  },
  {
    value: 'reportContacts',
    label: '展示联系人'
  },
  {
    value: 'description',
    label: '展示报告描述'
  },
  {
    value: 'reportCopyright',
    label: '展示版权说明'
  },
  {
    value: 'otherInformation',
    label: '其他信息'
  },
  {
    value: 'watermark',
    label: '为报告内容添加水印'
  }
];

const value = options.map(i => i.value);
const basicInfoSetting = ref({
  reportContacts: '',
  reportCopyright: '',
  otherInformation: '',
  watermark: ''
});

const placeholderConfig = {
  reportContacts: '联系人信息，默认为当前用户名称和邮箱，最长200个字符',
  reportCopyright: '报告版权说明，申明版权所有、使用范围限制等，最长200个字符',
  otherInformation: '其他信扩展息，用于对报告补充说明，最长200个字符',
  watermark: '水印文案，默认为当前租户名，最长100个字符'
};

const maxlengthConfig = {
  reportContacts: 200,
  reportCopyright: 200,
  otherInformation: 200,
  watermark: 100
};

onMounted(() => {
  watch(() => props.basicInfoSetting, () => {
    basicInfoSetting.value = JSON.parse(JSON.stringify(props.basicInfoSetting));
  }, {
    immediate: true
  });
});

defineExpose({
  getData: () => {
    return basicInfoSetting.value;
  }
});

</script>
<template>
  <div>
    <CheckboxGroup
      slot="label"
      :value="value"
      disabled
      class="flex flex-col space-y-2"
      slotScope="option">
      <Checkbox
        v-for="option in options"
        :key="option.value"
        class="!ml-0 items-start"
        :value="option.value">
        <div v-if="['reportContacts', 'reportCopyright', 'otherInformation', 'watermark'].includes(option.value)" class="w-100 space-y-1">
          <span style="color: rgb(82, 90, 101);">{{ option.label }}</span>
          <Textarea
            v-if="!viewType"
            v-model:value="basicInfoSetting[option.value]"
            :placeholder="placeholderConfig[option.value]"
            :maxlength="maxlengthConfig[option.value]"
            showCount />
          <div v-else-if="basicInfoSetting[option.value]" class="text-blue-1 p-2">
            {{ basicInfoSetting[option.value] }}
          </div>
        </div>
        <template v-else>
          <span style="color: rgb(82, 90, 101);">{{ option.label }}</span>
        </template>
      </Checkbox>
    </CheckboxGroup>
  </div>
</template>
