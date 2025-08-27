<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Hints, notification } from '@xcan-angus/vue-ui';
import { Checkbox, Modal, Switch } from 'ant-design-vue';
import { services } from '@/api/tester';

const { t } = useI18n();

interface Props {
  visible: boolean;
  id?: string; // serviceId
}

const props = withDefaults(defineProps<Props>(), {
  id: ''
});

const emits = defineEmits<{(e: 'update:visible', value: boolean)}>();

const checked = ref(['PERFORMANCE', 'STABILITY', 'FUNCTIONAL']);
const showOpt = ref(['PERFORMANCE', 'STABILITY', 'FUNCTIONAL']);
const validated = ref(false);
const testTypes = [
  {
    label: t('commonComp.apis.enabledTestModal.functionalTest'),
    value: 'FUNCTIONAL'
  },
  {
    label: t('commonComp.apis.enabledTestModal.performanceTest'),
    value: 'PERFORMANCE'
  },
  {
    label: t('commonComp.apis.enabledTestModal.stabilityTest'),
    value: 'STABILITY'
  }
];

const changeChecked = (value, checkedKey) => {
  if (value) {
    checked.value.push(checkedKey);
  } else {
    checked.value = checked.value.filter(i => i !== checkedKey);
  }
};

const changeShow = (event, checkedKey) => {
  if (event.target.checked) {
    showOpt.value.push(checkedKey);
  } else {
    showOpt.value = showOpt.value.filter(i => i !== checkedKey);
  }
};

const loading = ref(false);
const handleOk = async () => {
  loading.value = true;
  if (!showOpt.value.length) {
    handleClose();
    loading.value = false;
    return;
  }
  const enabled = showOpt.value.filter(i => checked.value.includes(i));
  if (enabled.length) {
    const [error] = await services.toggleTestEnabled(props.id, true, { testTypes: enabled }, {
      paramsType: true
    });
    if (error) {
      return;
    }
  }

  const disabled = showOpt.value.filter(i => !enabled.includes(i));
  if (disabled.length) {
    const [error] = await services.toggleTestEnabled(props.id, false, { testTypes: disabled }, {
      paramsType: true
    });
    if (error) {
      return;
    }
  }
  // let successMsg = '成功';
  // if (enabled.length) {
  //   const enabeldName = testTypes.filter(i => enabled.includes(i.value)).map(i => i.label).join('、');
  //   successMsg += `启用${enabeldName}`;
  // }

  // if (disabled.length) {
  //   const disabledName = testTypes.filter(i => disabled.includes(i.value)).map(i => i.label).join('、');
  //   successMsg += `${enabled.length ? '，' : ''}禁用${disabledName}`;
  // }

  loading.value = false;
  notification.success(t('commonComp.apis.enabledTestModal.successMessage'));
  emits('update:visible', false);
};

const handleClose = () => {
  emits('update:visible', false);
};

watch(() => props.visible, () => {
  checked.value = ['PERFORMANCE', 'STABILITY', 'FUNCTIONAL'];
  showOpt.value = ['PERFORMANCE', 'STABILITY', 'FUNCTIONAL'];
  validated.value = false;
}, {
  immediate: true
});

</script>
<template>
  <Modal
    :title="t('commonComp.apis.enabledTestModal.title')"
    :confirmLoading="loading"
    :visible="props.visible"
    :width="350"
    @cancel="handleClose"
    @ok="handleOk">
    <!-- <CheckboxGroup
      v-model:value="showOpt"
      :options="testTypes"
      class="ml-2">
    </CheckboxGroup> -->
    <Hints :text="t('commonComp.apis.enabledTestModal.hint')" />
    <div class="mt-2">
      <div
        v-for="opt in testTypes"
        :key="opt.value"
        class="flex items-center mb-2 flex-1">
        <Checkbox :checked="showOpt.includes(opt.value)" @change="changeShow($event, opt.value)" />
        <span class="w-20 ml-2">{{ opt.label }}</span>
        <div>
          <Switch
            v-show="showOpt.includes(opt.value)"
            :checked="checked.includes(opt.value)"
            :checkedChildren="t('commonComp.apis.enabledTestModal.enabled')"
            :unCheckedChildren="t('commonComp.apis.enabledTestModal.disabled')"
            size="small"
            @click="changeChecked($event, opt.value)" />
        </div>
      </div>
    </div>
  </Modal>
</template>
