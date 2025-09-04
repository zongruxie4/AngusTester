import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { EnumMessage, enumUtils, NoticeType, ReceiverType } from '@xcan-angus/infra';
import { event } from '@/api/gm';
import { notification } from '@xcan-angus/vue-ui';

/**
 * Composable for managing receiver configuration logic
 * @returns Receiver configuration functions and reactive state
 */
export function useReceiver () {
  const { t } = useI18n();

  // Reactive state
  const receiversType = ref<EnumMessage<string>[]>([]);
  const otherReceiversType = ref<EnumMessage<string>>({ value: '', message: '' });
  const defaultUsers = ref<{
    value: string;
    label: string;
  }[]>([]);
  const users = ref<string[]>([]);
  const noticeTypeOpt = ref<{ value: string; message: string }[]>([]);
  const noticeType = ref<string[]>([]);
  const receivingMethods = ref<string[]>([]);

  /**
   * Initialize receiver configuration data
   * @param selectedItem - Selected push setting item
   */
  const initReceiver = (selectedItem: any) => {
    // Set default users from selected item
    defaultUsers.value = (selectedItem?.receiveSetting?.receivers?.receivers || []).map((i: any) => ({
      label: i.fullName,
      value: i.id
    }));
    users.value = defaultUsers.value.map(user => user.value);

    // Set notice types from selected item
    noticeType.value = (selectedItem?.receiveSetting?.receivers?.noticeTypes || []).map((i: any) => i.value);

    // Set receiving methods from selected item
    receivingMethods.value = selectedItem?.receiveSetting?.receivers?.receiverTypes.map((m: any) => m.value) || [];

    loadReceiverEnum();
    loadNoticeType();
  };

  /**
   * Load receiver enum data
   */
  const loadReceiverEnum = () => {
    const data = enumUtils.enumToMessages(ReceiverType);
    receiversType.value = (data || []).filter(type => type.value !== 'OTHER');
    otherReceiversType.value = (data || []).find(type => type.value === 'OTHER') || { value: '', message: '' };
  };

  /**
   * Load notice type data
   */
  const loadNoticeType = () => {
    const data = enumUtils.enumToMessages(NoticeType);
    noticeTypeOpt.value = (data || []).map(val => ({ ...val, label: val.message }));
  };

  /**
   * Save receiver configuration
   * @param id - Event ID
   */
  const saveReceiver = async (id: string) => {
    const otherTypeValue = users.value.length ? [otherReceiversType.value.value] : [];
    const [error] = await event.putReceiver({
      id,
      receiverIds: users.value,
      receiverTypes: [...receivingMethods.value, ...otherTypeValue],
      noticeTypes: noticeType.value
    });

    if (error) {
      return false;
    }
    notification.success(t('event.receiver.configSuccess'));
    return true;
  };

  return {
    // Reactive data
    receiversType,
    otherReceiversType,
    defaultUsers,
    users,
    noticeTypeOpt,
    noticeType,
    receivingMethods,

    // Methods
    initReceiver,
    loadReceiverEnum,
    loadNoticeType,
    saveReceiver
  };
}
