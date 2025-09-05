import { useI18n } from 'vue-i18n';

/**
 * Composable for table column definitions and styling configurations.
 * <p>
 * Centralizes table structure, HTTP method colors, and icon mappings
 * for consistent UI presentation across the control panel.
 */
export function useTableColumns () {
  const { t } = useI18n();

  /**
   * Column definitions for the mock API table.
   */
  const columns = [
    {
      key: 'summary',
      title: t('mock.mockDetail.control.apiName'),
      dataIndex: 'summary',
      width: '15%',
      ellipsis: true
    },
    {
      key: 'method',
      title: t('mock.mockDetail.control.apiInfo'),
      dataIndex: 'method',
      width: '35%'
    },
    {
      key: 'requestNum',
      title: t('mock.mockDetail.control.requestCount'),
      dataIndex: 'requestNum',
      customRender: ({ text }: { text: string }): string => text || '--',
      width: '10%'
    },
    {
      key: 'simulateErrorNum',
      title: t('mock.mockDetail.control.mockExceptionCount'),
      dataIndex: 'simulateErrorNum',
      width: '10%'
    },
    {
      key: 'pushbackNum',
      title: t('mock.mockDetail.control.pushbackCount'),
      dataIndex: 'pushbackNum',
      width: '10%'
    },
    {
      key: 'successNum',
      title: t('mock.mockDetail.control.successCount'),
      dataIndex: 'successNum',
      customRender: ({ text }: { text: string }): string => text || '--',
      width: '10%'
    },
    {
      key: 'exceptionNum',
      title: t('mock.mockDetail.control.exceptionCount'),
      dataIndex: 'exceptionNum',
      width: '10%'
    }
  ];

  /**
   * Configuration for mock service statistics display.
   */
  const mockServiceCountConfig = [
    {
      name: t('mock.mockDetail.control.apiCount'),
      key: 'apisNum' as const,
      icon: 'icon-jiekoushu'
    },
    {
      name: t('mock.mockDetail.control.requestCount'),
      key: 'requestNum' as const,
      icon: 'icon-qingqiushu'
    },
    {
      name: t('mock.mockDetail.control.mockExceptionCount'),
      key: 'simulateErrorNum' as const,
      icon: 'icon-moniyichangshu'
    },
    {
      name: t('mock.mockDetail.control.pushbackCount'),
      key: 'pushbackNum' as const,
      icon: 'icon-huituishu'
    },
    {
      name: t('mock.mockDetail.control.successCount'),
      key: 'successNum' as const,
      icon: 'icon-chenggongshu1'
    },
    {
      name: t('mock.mockDetail.control.exceptionCount'),
      key: 'exceptionNum' as const,
      icon: 'icon-yichangshu1'
    }
  ];

  /**
   * Color mapping for statistic count icons.
   */
  const countIconColors: Record<string, string> = {
    apisNum: 'text-status-pending',
    requestNum: 'text-status-process',
    pushbackNum: 'text-status-orange',
    simulateErrorNum: 'text-status-error1',
    successNum: 'text-status-success',
    exceptionNum: 'text-status-error'
  };

  /**
   * Color mapping for HTTP method labels.
   */
  const httpMethodColors: Record<string, string> = {
    GET: 'text-http-get',
    POST: 'text-http-post',
    DELETE: 'text-http-delete',
    PATCH: 'text-http-patch',
    PUT: 'text-http-put',
    OPTIONS: 'text-http-options',
    HEAD: 'text-http-head',
    TRACE: 'text-http-trace'
  };

  return {
    columns,
    mockServiceCountConfig,
    countIconColors,
    httpMethodColors
  };
}
