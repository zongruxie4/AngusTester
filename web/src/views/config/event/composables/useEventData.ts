import { computed, onMounted, reactive, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { CombinedTargetType, EnumMessage, enumUtils, EventPushStatus, NoticeType } from '@xcan-angus/infra';
import DOMPurify from 'dompurify';
import { PushRecord, PushSetting, TableColumn } from '../types';
import { event, setting } from '@/api/gm';
import { analysis } from 'src/api/gm';

interface SearchLogOption {
  valueKey?: string;
  type: string;
  placeholder: string | string[];
  allowClear?: boolean;
  enumKey?: typeof EventPushStatus;
}

/**
 * Composable for managing event data and related operations
 * @returns Event data management functions and reactive state
 */
export function useEventData () {
  const { t } = useI18n();

  // Loading states
  const configLoading = ref(false);
  const recordLoading = ref(false);

  // Visibility states
  const showRecord = ref(true);
  const showConfigure = ref(true);
  const receiverVisible = ref(false);
  const configureVisible = ref(false);

  // Pagination and search parameters
  const params: { pageNo: number; pageSize: number } = reactive({ pageNo: 1, pageSize: 10 });
  const searchParams = ref<{ key: string; value: any; op: string }[]>([]);

  // State for statistics and selected items
  const state: {
    count: {
      pushFail: string;
      pushSuccess: string;
      ignore: string;
      total: string;
    };
    selectedItem: PushSetting;
  } = reactive({
    count: {
      pushFail: '0',
      pushSuccess: '0',
      ignore: '0',
      total: '0'
    },
    selectedItem: {} as PushSetting
  });

  // Data lists
  const pushSettingList = ref<PushSetting[]>([]);
  const pushRecordList = ref<PushRecord[]>([]);
  const pageTotal = ref(0);

  // Enum data
  const targetTypeEnums = ref<EnumMessage<CombinedTargetType>[]>([]);
  const noticeType = ref<EnumMessage<NoticeType>[]>([]);
  const eventDataNoticeType = ref<{ eventCode: string; noticeTypes: EnumMessage<CombinedTargetType>[] }[]>([]);

  /**
   * Initialize all data
   */
  const init = async () => {
    loadEnums();
    await loadEventNoticeTypeByEventCode();
    await loadPushConfigList();
    await loadStatistics();
    await loadPushRecordList();
  };

  /**
   * Load enum data for target types and notice types
   */
  const loadEnums = () => {
    const combinedTargetType = enumUtils.enumToMessages(CombinedTargetType);
    const noticeTypeData = enumUtils.enumToMessages(NoticeType);
    targetTypeEnums.value = (combinedTargetType || []) as EnumMessage<CombinedTargetType>[];
    noticeType.value = (noticeTypeData || []).map(i => {
      return {
        ...i,
        label: i.message
      };
    }) as EnumMessage<NoticeType>[];
  };

  /**
   * Load event notice types by event code
   */
  const loadEventNoticeTypeByEventCode = async () => {
    const [error, { data }] = await setting.getEventNoticeType();
    if (error) {
      return;
    }
    eventDataNoticeType.value = data || [];
  };

  /**
   * Get target type name by value
   * @param value - The target type value
   * @returns The display message for the target type
   */
  const getTargetTypeName = (value: string) => {
    const target = targetTypeEnums.value.find(i => i.value === value);
    return target?.message || '  ';
  };

  /**
   * Load push statistics data
   */
  const loadStatistics = async () => {
    const [error, { data = { push_status: {} } }] = await analysis.loadCustomizationSummary({
      aggregates: [
        {
          column: 'id',
          function: 'COUNT'
        }
      ],
      groupBy: 'STATUS',
      groupByColumns: ['type', 'push_status'],
      name: 'Event',
      appCode: 'AngusTester'
    });
    if (error) {
      return;
    }
    const {
      PUSH_SUCCESS = { COUNT_id: 0, TOTAL_COUNT_id: 0 },
      PUSH_FAIL = { COUNT_id: 0, TOTAL_COUNT_id: 0 },
      IGNORED = { COUNT_id: 0, TOTAL_COUNT_id: 0 }
    } = data.push_status;

    state.count.pushSuccess = PUSH_SUCCESS.COUNT_id;
    state.count.ignore = IGNORED.COUNT_id;
    state.count.pushFail = PUSH_FAIL.COUNT_id;
    state.count.total = (+PUSH_SUCCESS.TOTAL_COUNT_id || +PUSH_FAIL.TOTAL_COUNT_id || +IGNORED.TOTAL_COUNT_id || 0) + '';
  };

  /**
   * Load push configuration list
   */
  const loadPushConfigList = async () => {
    configLoading.value = true;
    const [error, res] = await event.getCurrentTemplateList({
      appCode: 'AngusTester',
      pageSize: 2000
    });
    configLoading.value = false;
    if (error) {
      return;
    }

    pushSettingList.value = res.data.list?.map(item => {
      const noticeTypesObj = eventDataNoticeType.value.find(i => i.eventCode === item.eventCode)?.noticeTypes || [];
      return {
        ...item,
        pushMsg: DOMPurify.sanitize(item.pushMsg || ''),
        noticeTypes: noticeTypesObj.map(i => i.value)
      };
    }) || [];
  };

  /**
   * Load push record list with pagination
   */
  const loadPushRecordList = async () => {
    if (recordLoading.value) {
      return;
    }
    recordLoading.value = true;
    const [error, { data = { list: [], total: 0 } }] = await event.getRecordList({
      ...params,
      filters: searchParams.value,
      appCode: 'AngusTester'
    });
    recordLoading.value = false;
    if (error) {
      return;
    }

    pushRecordList.value = data.list;
    pageTotal.value = +data.total;
  };

  /**
   * Open receive channel configuration modal
   * @param record - The push setting record
   */
  const openReceiveChannel = (record: PushSetting) => {
    state.selectedItem = record;
    configureVisible.value = true;
  };

  /**
   * Open receiver configuration modal
   * @param record - The push setting record
   */
  const openReceiver = (record: PushSetting) => {
    state.selectedItem = record;
    receiverVisible.value = true;
  };

  /**
   * Handle search parameter changes
   * @param value - New search parameters
   */
  const changeLogParams = (value: any) => {
    searchParams.value = value;
    params.pageNo = 1;
    loadPushRecordList();
  };

  /**
   * Handle pagination changes
   * @param _pagination - Pagination object
   */
  const changePagination = (_pagination: any) => {
    const { current, pageSize } = _pagination;
    params.pageNo = current;
    params.pageSize = pageSize;
    loadPushRecordList();
  };

  /**
   * Change notice types for an event
   * @param typesValue - Selected notice types
   * @param eventCode - Event code
   */
  const handleChangeNoticeType = async (typesValue: string[], eventCode: string) => {
    const paramsData = pushSettingList.value.map(i => {
      return {
        eventCode: i.eventCode,
        noticeTypes: i.eventCode === eventCode ? typesValue : i.noticeTypes
      };
    });
    const [error] = await setting.putEventNotice(paramsData);

    if (error) {
      return;
    }

    pushSettingList.value.forEach(item => {
      if (item.eventCode === eventCode) {
        // 修复类型不匹配问题，将string[]转换为正确的格式
        item.noticeTypes = typesValue.map(value => ({
          value,
          message: noticeType.value.find(nt => nt.value === value)?.message || value
        }));
      }
    });
  };

  /**
   * Get receiver information from receive setting
   * @param receiveSetting - Receive setting object
   * @returns Formatted receiver string
   */
  const getReceiver = (receiveSetting: any) => {
    const receiverTypes: string[] = receiveSetting?.receivers?.receiverTypes?.map((m: any) => m.message) || [];
    const otherUser: string[] = receiveSetting?.receivers?.receivers?.map((m: any) => m.fullName) || [];
    return [...otherUser, ...receiverTypes].join('、');
  };

  /**
   * Get status for push record
   * @param key - Status key
   * @returns Status value
   */
  const getStatus = (key: string): string => {
    const statusMap: { [key: string]: string } = {
      UN_PUSH: 'default',
      PUSHING: 'processing',
      PUSH_SUCCESS: 'success',
      PUSH_FAIL: 'error'
    };
    return statusMap[key];
  };

  /**
   * Computed property for push configuration columns
   */
  const configColumns = computed<TableColumn[]>(() => {
    return [
      {
        title: t('event.columns.eventName'),
        dataIndex: 'eventName',
        key: 'eventName'
      },
      {
        title: t('common.category'),
        dataIndex: 'targetType',
        key: 'targetType'
      },
      {
        title: t('event.columns.noticeType'),
        dataIndex: 'noticeType',
        key: 'noticeType'
      }
    ];
  });

  /**
   * Computed property for push record columns
   */
  const recordColumns = computed<TableColumn[]>(() => {
    return [
      {
        title: t('event.columns.eventId'),
        dataIndex: 'id',
        key: 'id',
        width: '12%'
      },
      {
        title: t('event.columns.eventName'),
        dataIndex: 'name',
        width: '12%',
        ellipsis: true
      },
      {
        title: t('event.columns.content'),
        dataIndex: 'description',
        ellipsis: true
      },
      {
        title: t('event.columns.receiver'),
        dataIndex: 'fullName',
        width: '12%'
      },
      {
        title: t('event.columns.createdDate'),
        key: 'createdDate',
        dataIndex: 'createdDate',
        width: '12%'
      },
      {
        title: t('event.columns.pushStatus'),
        dataIndex: 'pushStatus',
        key: 'pushStatus',
        width: '12%'
      }
    ];
  });

  /**
   * Computed property for pagination configuration
   */
  const pagination = computed(() => {
    return {
      current: params.pageNo,
      pageSize: params.pageSize,
      total: pageTotal.value
    };
  });

  /**
   * Search log options configuration
   */
  const searchLogOpt = computed<SearchLogOption[]>(() => [
    {
      valueKey: 'code',
      type: 'input',
      placeholder: t('event.searchLogPlaceholder.code'),
      allowClear: true
    },
    {
      valueKey: 'pushStatus',
      type: 'select-enum',
      enumKey: EventPushStatus,
      placeholder: t('event.searchLogPlaceholder.pushStatus'),
      allowClear: true
    },
    {
      type: 'date-range',
      valueKey: 'createdDate',
      placeholder: [t('event.searchLogPlaceholder.startDate'), t('event.searchLogPlaceholder.endDate')]
    }
  ]);

  // Initialize on component mount
  const onMountedInit = () => {
    onMounted(() => {
      init();
    });
  };

  return {
    // Reactive data
    configLoading,
    recordLoading,
    showRecord,
    showConfigure,
    receiverVisible,
    configureVisible,
    params,
    searchParams,
    state,
    pushSettingList,
    pushRecordList,
    pageTotal,
    targetTypeEnums,
    noticeType,
    eventDataNoticeType,

    // Computed properties
    configColumns,
    recordColumns,
    pagination,
    searchLogOpt,

    // Methods
    init,
    loadEnums,
    loadEventNoticeTypeByEventCode,
    getTargetTypeName,
    loadStatistics,
    loadPushConfigList,
    loadPushRecordList,
    openReceiveChannel,
    openReceiver,
    changeLogParams,
    changePagination,
    handleChangeNoticeType,
    getReceiver,
    getStatus,
    onMountedInit,

    // i18n
    t
  };
}
