<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Checkbox, CheckboxGroup, Textarea } from 'ant-design-vue';

const { t } = useI18n();

// Define component props for basic information settings
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

// Configuration options for basic information settings
const options = [
  {
    value: 'name',
    label: t('reportAdd.basic.options.showName')
  },
  {
    value: 'user',
    label: t('reportAdd.basic.options.showUser')
  },
  {
    value: 'createdDate',
    label: t('reportAdd.basic.options.showCreatedDate')
  },
  {
    value: 'version',
    label: t('reportAdd.basic.options.showVersion')
  },
  {
    value: 'reportContacts',
    label: t('reportAdd.basic.options.reportContacts')
  },
  {
    value: 'description',
    label: t('reportAdd.basic.options.description')
  },
  {
    value: 'reportCopyright',
    label: t('reportAdd.basic.options.reportCopyright')
  },
  {
    value: 'otherInformation',
    label: t('reportAdd.basic.options.otherInformation')
  },
  {
    value: 'watermark',
    label: t('reportAdd.basic.options.watermark')
  }
];

// Extract values from options
const value = options.map(i => i.value);

// Reactive reference for basic information settings
const basicInfoSetting = ref({
  reportContacts: '',
  reportCopyright: '',
  otherInformation: '',
  watermark: ''
});

// Configuration for placeholder texts
const placeholderConfig = {
  reportContacts: t('reportAdd.basic.placeholders.reportContacts'),
  reportCopyright: t('reportAdd.basic.placeholders.reportCopyright'),
  otherInformation: t('reportAdd.basic.placeholders.otherInformation'),
  watermark: t('reportAdd.basic.placeholders.watermark')
};

// Configuration for maximum length of inputs
const maxlengthConfig = {
  reportContacts: 200,
  reportCopyright: 200,
  otherInformation: 200,
  watermark: 100
};

// Initialize component and watch for prop changes
onMounted(() => {
  watch(() => props.basicInfoSetting, () => {
    basicInfoSetting.value = JSON.parse(JSON.stringify(props.basicInfoSetting));
  }, {
    immediate: true
  });
});

// Expose methods for parent components
defineExpose({
  getData: () => {
    return basicInfoSetting.value;
  }
});

</script>

<template>
  <div>
    <!-- Checkbox group for basic information settings -->
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
        <!-- Render textareas for specific options -->
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
        <!-- Render simple labels for other options -->
        <template v-else>
          <span style="color: rgb(82, 90, 101);">{{ option.label }}</span>
        </template>
      </Checkbox>
    </CheckboxGroup>
  </div>
</template>
