<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Checkbox, CheckboxGroup, Textarea } from 'ant-design-vue';

const { t } = useI18n();

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
    label: t('reportAdd.basic.options.name')
  },
  {
    value: 'user',
    label: t('reportAdd.basic.options.user')
  },
  {
    value: 'createdDate',
    label: t('reportAdd.basic.options.createdDate')
  },
  {
    value: 'version',
    label: t('reportAdd.basic.options.version')
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

const value = options.map(i => i.value);
const basicInfoSetting = ref({
  reportContacts: '',
  reportCopyright: '',
  otherInformation: '',
  watermark: ''
});

const placeholderConfig = {
  reportContacts: t('reportAdd.basic.placeholders.reportContacts'),
  reportCopyright: t('reportAdd.basic.placeholders.reportCopyright'),
  otherInformation: t('reportAdd.basic.placeholders.otherInformation'),
  watermark: t('reportAdd.basic.placeholders.watermark')
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
