<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { PushSetting } from './interface';
import { EnumMessage, ReceiverType, NoticeType, enumUtils } from '@xcan-angus/infra';
import { event } from '@/api/gm';
import { Modal, notification, SelectUser } from '@xcan-angus/vue-ui';
import { Checkbox, CheckboxGroup, Form, FormItem } from 'ant-design-vue';

interface Props {
  visible: boolean,
  selectedItem: PushSetting
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  selectedItem: () => ({} as PushSetting)
});

const emit = defineEmits<{(e: 'update:visible', value: boolean): void, (e: 'refresh'): void }>();

const { t } = useI18n();
const formRef = ref();

const receiversType = ref<EnumMessage<string>[]>([]);
const otherReceiversType = ref<EnumMessage<string>>({ value: '', message: '' });
const defaultUsers = ref<{
  value: string,
  label: string
}[]>((props.selectedItem?.receiveSetting?.receivers?.receivers || []).map(i => ({ label: i.fullName, value: i.id })));
const users = ref<string[]>(defaultUsers.value.map(user => user.value));
const noticeTypeOpt = ref<{ value: string, message: string }[]>([]);
const noticeType = ref<string[]>((props.selectedItem?.receiveSetting?.receivers?.noticeTypes || []).map(i => i.value));

const init = () => {
  loadReceiverEnum();
  loadNoticeType();
};

const loadReceiverEnum = () => {
  const data = enumUtils.enumToMessages(ReceiverType);
  receiversType.value = (data || []).filter(type => type.value !== 'OTHER');
  otherReceiversType.value = (data || []).find(type => type.value === 'OTHER') || { value: '', message: '' };
};

const loadNoticeType = () => {
  const data = enumUtils.enumToMessages(NoticeType);
  noticeTypeOpt.value = (data || []).map(val => ({ ...val, label: val.message }));
};

const receivingMethods = ref<string[]>(props.selectedItem?.receiveSetting?.receivers?.receiverTypes.map(m => m.value) || []);

const handleOk = async () => {
  const otherTypeValue = users.value.length ? [otherReceiversType.value.value] : [];
  const [error] = await event.putReceiver({
    id: props.selectedItem.id,
    receiverIds: users.value,
    receiverTypes: [...receivingMethods.value, ...otherTypeValue],
    noticeTypes: noticeType.value
  });

  if (error) {
    return;
  }
  notification.success('配置成功');
  emit('update:visible', false);
  emit('refresh');
};
const handleCancel = () => {
  emit('update:visible', false);
};
onMounted(() => {
  init();
});
</script>

<template>
  <Modal
    :visible="props.visible"
    :title="t('settingNotification.title.t10')"
    :centered="true"
    width="600px"
    @ok="handleOk"
    @cancel="handleCancel">
    <Form
      ref="formRef"
      size="small"
      layout="vertical">
      <FormItem :label="t('settingNotification.title.t9')">
        <CheckboxGroup v-model:value="receivingMethods" class="my-2">
          <Checkbox
            v-for="r in receiversType"
            :key="r.value"
            :value="r.value">
            {{ r.message }}
          </Checkbox>
        </CheckboxGroup>
        <div class="flex text-3">
          <span class="mr-2 leading-7">{{ otherReceiversType.message }}</span>
          <SelectUser
            v-model:value="users"
            :allowClear="false"
            :placeholder="t('settingNotification.title.t11')"
            class="flex-1"
            mode="multiple"
            size="small"
            style="width: 100%;"
            internal
            showSearch />
        </div>
      </FormItem>
      <FormItem :label="t('通知方式')">
        <CheckboxGroup
          v-model:value="noticeType"
          :options="noticeTypeOpt"
          class="my-2" />
      </FormItem>
    </Form>
  </Modal>
</template>
