import { reactive, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { EnumMessage, enumUtils, ReceiveChannelType } from '@xcan-angus/infra';
import { notification } from '@xcan-angus/vue-ui';
import { event } from '@/api/gm';

/**
 * Composable for managing receive configuration logic
 * @returns Receive configuration functions and reactive state
 */
export function useReceiveConfig () {
  const { t } = useI18n();

  // Type definitions
  interface Options {
    address: string;
    id: string;
    name: string;
    pkey: { value: string; message: string };
    secret: string;
  }

  interface EventTypeOption extends EnumMessage<ReceiveChannelType> {
    label: string;
    description: string;
  }

  // Reactive state
  const eventTypes = ref<EventTypeOption[]>([]);
  const selectedType = ref<string[]>([]);
  const channelValues = reactive({
    WEBHOOK: [],
    EMAIL: [],
    DINGTALK: [],
    WECHAT: []
  });

  const webOptions = ref<Options[]>([]);
  const emailOptions = ref<Options[]>([]);
  const dingTalkOptions = ref<Options[]>([]);
  const wechatOptions = ref<Options[]>([]);

  /**
   * Initialize receive configuration data
   */
  const initReceiveConfig = async (id: string) => {
    await loadCurrentChannels(id);
    await loadEventTypes();
  };

  /**
   * Load event types for receive configuration
   */
  const loadEventTypes = async () => {
    const data = enumUtils.enumToMessages(ReceiveChannelType);
    eventTypes.value = (data || [])?.map(m => {
      (selectedType.value.includes(m.value) || channelValues[m.value].length) && loadConfigOptions(m.value);
      return {
        ...m,
        label: m.message,
        description: m.message
      } as EventTypeOption;
    });
  };

  /**
   * Load current channels for the event
   * @param id - Event ID
   */
  const loadCurrentChannels = async (id: string) => {
    const [error, res] = await event.getCurrentChannels(id);
    if (error) {
      return;
    }
    selectedType.value = (res.data.allowedChannelTypes || []).map(type => type.value);
    channelValues.WEBHOOK = res.data.receiveSetting?.channels?.filter(channel => channel.channelType.value === 'WEBHOOK').map(channel => channel.id) || [];
    channelValues.EMAIL = res.data.receiveSetting?.channels?.filter(channel => channel.channelType.value === 'EMAIL').map(channel => channel.id) || [];
    channelValues.DINGTALK = res.data.receiveSetting?.channels?.filter(channel => channel.channelType.value === 'DINGTALK').map(channel => channel.id) || [];
    channelValues.WECHAT = res.data.receiveSetting?.channels?.filter(channel => channel.channelType.value === 'WECHAT').map(channel => channel.id) || [];
  };

  /**
   * Load receive setting details for a channel type
   * @param key - Channel type key
   */
  const loadReceiveSettingDetail = async (key: string) => {
    const [error, res] = await event.getChannelDetail(key);
    if (error || !res.data) {
      return [];
    }
    return res.data;
  };

  /**
   * Load configuration options for a channel type
   * @param key - Channel type key
   */
  const loadConfigOptions = async (key: string) => {
    if (key === 'WEBHOOK') {
      loadReceiveSettingDetail(key).then(res => { webOptions.value = res; });
    }
    if (key === 'EMAIL') {
      loadReceiveSettingDetail(key).then(res => { emailOptions.value = res; });
    }
    if (key === 'DINGTALK') {
      loadReceiveSettingDetail(key).then(res => { dingTalkOptions.value = res; });
    }
    if (key === 'WECHAT') {
      loadReceiveSettingDetail(key).then(res => { wechatOptions.value = res; });
    }
  };

  /**
   * Get options for a specific channel type
   * @param key - Channel type key
   * @returns Options for the channel type
   */
  const getOptions = (key: string) => {
    if (key === 'WEBHOOK') {
      return webOptions.value;
    }
    if (key === 'EMAIL') {
      return emailOptions.value;
    }
    if (key === 'DINGTALK') {
      return dingTalkOptions.value;
    }
    if (key === 'WECHAT') {
      return wechatOptions.value;
    }
  };

  /**
   * Get placeholder text for a channel type
   * @param key - Channel type key
   * @returns Placeholder text
   */
  const getPlaceholder = (key: string) => {
    if (key === 'WEBHOOK') {
      return t('notification.receiveConfig.selectWebhookAddress');
    }
    if (key === 'EMAIL') {
      return t('notification.receiveConfig.selectEmail');
    }
    if (key === 'DINGTALK') {
      return t('notification.receiveConfig.selectDingTalkBot');
    }
    if (key === 'WECHAT') {
      return t('notification.receiveConfig.selectWeChatBot');
    }
  };

  /**
   * Save channel setting configuration
   * @param id - Event ID
   */
  const saveChannelSetting = async (id: string) => {
    let ids: string[] = [];
    for (const eventType of selectedType.value) {
      ids = ids.concat(channelValues[eventType]);
    }
    const [error] = await event.saveChannelSetting({ id, channelIds: ids });
    if (error) {
      return false;
    }
    notification.success(t('notification.receiveConfig.configSuccess'));
    return true;
  };

  return {
    // Reactive data
    eventTypes,
    selectedType,
    channelValues,
    webOptions,
    emailOptions,
    dingTalkOptions: dingTalkOptions,
    wechatOptions,

    // Methods
    initReceiveConfig,
    loadEventTypes,
    loadCurrentChannels,
    loadReceiveSettingDetail,
    loadConfigOptions,
    getOptions,
    getPlaceholder,
    saveChannelSetting
  };
}
