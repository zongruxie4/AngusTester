import uitlAssert from '@/components/apis/assertion/utils/assert';

import uitlExpression from '@/components/apis/assertion/utils/expression/Expression';
import extract from '@/components/apis/assertion/utils/extract';
import uitlJsonpath from '@/components/apis/assertion/utils/JsonPath';
import uitlProxy from '@/components/apis/assertion/utils/assert/Proxy';
import uitlRegexp from '@/components/apis/assertion/utils/Regexp';
import uitlXpath from '@/components/apis/assertion/utils/XPath';

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
