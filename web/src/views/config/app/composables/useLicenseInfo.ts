import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { appContext, EditionType } from '@xcan-angus/infra';
import { edition } from '@/api/store';

/**
 * Composable for managing permit information display
 */
export function useLicenseInfo () {
  const { t } = useI18n();

  // Edition type and data source
  const editionType = ref<string>();
  const dataSource = ref<Record<string, any>>({});

  /**
   * Column configuration for private edition display
   */
  const privateColumns = [[
    {
      label: t('app.config.license.columns.editionType'),
      dataIndex: 'editionType'
    },
    {
      label: t('app.config.license.columns.goodsCode'),
      dataIndex: 'goodsCode'
    },
    {
      label: t('app.config.license.columns.goodsVersion'),
      dataIndex: 'goodsVersion'
    },
    {
      label: t('app.config.license.columns.provider'),
      dataIndex: 'provider'
    },
    {
      label: t('app.config.license.columns.issuer'),
      dataIndex: 'issuer'
    },
    {
      label: t('app.config.license.columns.holder'),
      dataIndex: 'holder'
    },
    {
      label: t('app.config.license.columns.licenseNo'),
      dataIndex: 'licenseNo'
    },
    {
      label: t('app.config.license.columns.issuedDate'),
      dataIndex: 'beginDate'
    },
    {
      label: t('common.expiredDate'),
      dataIndex: 'expiredDate'
    },
    {
      label: t('app.config.license.columns.signature'),
      dataIndex: 'signature'
    }
  ]];

  /**
   * Column configuration for cloud service edition display
   */
  const cloudServiceColumns = [[
    {
      label: t('app.config.license.columns.editionType'),
      dataIndex: 'editionType'
    },
    {
      label: t('app.config.license.columns.goodsCode'),
      dataIndex: 'goodsCode'
    },
    {
      label: t('app.config.license.columns.goodsVersion'),
      dataIndex: 'goodsVersion'
    },
    {
      label: t('app.config.license.columns.provider'),
      dataIndex: 'provider'
    },
    {
      label: t('app.config.license.columns.issuer'),
      dataIndex: 'issuer'
    },
    {
      label: t('app.config.license.columns.holder'),
      dataIndex: 'holder'
    }
  ]];

  /**
   * Load permit information from the API
   */
  const loadInfo = async () => {
    const [error, res] = await edition.getSysEdition();
    if (error) {
      return;
    }
    dataSource.value = res.data;
  };

  /**
   * Get icon for edition type
   * @param key - The edition type key
   * @returns The icon class name
   */
  const getVersionTypeIcon = (key: string) => {
    switch (key) {
      case EditionType.DATACENTER:
        return 'icon-shujuzhongxin';
      case EditionType.CLOUD_SERVICE:
        return 'icon-yunfuwu';
      case EditionType.ENTERPRISE:
        return 'icon-qiye';
      case EditionType.COMMUNITY:
        return 'icon-shequ';
      default:
        return '';
    }
  };

  /**
   * Initialize data on component mount
   */
  const init = () => {
    onMounted(async () => {
      editionType.value = appContext.getEditionType();
      await loadInfo();
    });
  };

  /**
   * Computed property for columns based on edition type
   */
  const columns = computed(() => {
    if (editionType.value === EditionType.CLOUD_SERVICE) {
      return cloudServiceColumns;
    }
    return privateColumns;
  });

  return {
    // Reactive data
    editionType,
    dataSource,

    // Computed properties
    columns,

    // Methods
    loadInfo,
    getVersionTypeIcon,
    init
  };
}
