import uitlAssert from '@/components/apis/assertion/utils/assert';

import uitlExpression from '@/components/apis/assertion/utils/expression';
import extract from '@/components/apis/assertion/utils/extract';
import uitlJsonpath from '@/components/apis/assertion/utils/jsonpath';
import uitlProxy from '@/components/apis/assertion/utils/proxy';
import uitlRegexp from '@/components/apis/assertion/utils/regexp';
import uitlXpath from '@/components/apis/assertion/utils/xpath';

export const assertUtil = {
  assert: uitlAssert,
  uitlExpression,
  extract,
  jsonpath: uitlJsonpath,
  proxy: uitlProxy,
  regexp: uitlRegexp,
  xpath: uitlXpath
};

export default {
  assert: uitlAssert,
  uitlExpression,
  extract,
  jsonpath: uitlJsonpath,
  proxy: uitlProxy,
  regexp: uitlRegexp,
  xpath: uitlXpath
};
