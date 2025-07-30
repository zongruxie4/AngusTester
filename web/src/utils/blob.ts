import { http } from '@xcan-angus/infra';

const dataURLtoBlob = (dataurl, type?: string) => {
  try {
    const arr = dataurl.split(',');
    const mime = type || arr[0].match(/:(.*?);/)?.[1];
    const bstr = atob(arr[1] || arr[0]);
    let n = bstr.length;
    const u8arr = new Uint8Array(n);
    while (n--) {
      u8arr[n] = bstr.charCodeAt(n);
    }
    return new Blob([u8arr], {
      type: mime
    });
  } catch {
    return undefined;
  }
};

const fileToBuffer = (file) => {
  const fr = new FileReader();
  // const fileName = file.name;
  fr.readAsArrayBuffer(file);
  return new Promise((resolve) => {
    fr.addEventListener('loadend', (e) => {
      const buf = e.target?.result;
      resolve(buf);
    });
  });
};

const isBase64 = (str):boolean => {
  if (str === '' || str.trim() === '') {
    return false;
  }
  try {
    return btoa(atob(str)) === str;
  } catch (err) {
    return false;
  }
};

const blobTobase64 = (blob?: Blob): Promise<string | undefined> => {
  if (!blob) {
    return Promise.resolve(undefined);
  }
  const fileReader = new FileReader();
  fileReader.readAsDataURL(blob);
  return new Promise((resolve) => {
    fileReader.onload = () => {
      resolve(fileReader.result as string);
    };
  });
};

const fileToBlob = (file): Blob => {
  const blob = new Blob([file], { type: 'application/octet-stream' });
  return blob;
};

const getBlobByUrl = (dataUrl?: string):Promise<Blob | undefined> => {
  if (!dataUrl) {
    return Promise.resolve(undefined);
  }
  return http.get(dataUrl).then(([error, resp]) => {
    if (error) {
      return;
    }
    return fileToBlob(resp.data);
  });
};

const getFileSuffixByContentType = (contentType) => {
  if (contentType === 'application/x-authoware-bin') {
    return 'aab';
  } else if (contentType === 'application/x-authoware-map') {
    return 'aam';
  } else if (contentType === 'application/x-authoware-seg') {
    return 'aas';
  } else if (contentType === 'application/postscript') {
    return 'ai';
  } else if (contentType === 'audio/x-aiff') {
    return 'aif';
  } else if (contentType === 'audio/X-Alpha5') {
    return 'als';
  } else if (contentType === 'application/x-mpeg') {
    return 'amc';
  } else if (contentType === 'application/octet-stream') {
    return 'ani';
  } else if (contentType === 'text/plain') {
    return 'asc';
  } else if (contentType === 'application/astound') {
    return 'asd';
  } else if (contentType === 'application/x-asap') {
    return 'asp';
  } else if (contentType === 'video/x-msvideo') {
    return 'awb';
  } else if (contentType === 'audio/amr-wb') {
    return 'asd';
  } else if (contentType === 'application/x-bzip2') {
    return 'bz2';
  } else if (contentType === 'images/x-cals') {
    return 'cal';
  } else if (contentType === 'application/x-cnc') {
    return 'ccn';
  } else if (contentType === 'application/x-cocoa') {
    return 'cco';
  } else if (contentType === 'application/x-netcdf') {
    return 'cdf';
  } else if (contentType === 'application/x-msclip') {
    return 'clp';
  } else if (contentType === 'application/x-cult3d-object') {
    return 'co';
  } else if (contentType === 'application/mac-compactpro') {
    return 'cpt';
  } else if (contentType === 'application/x-mscardfile') {
    return 'crd';
  } else if (contentType === 'x-lml/x-evm') {
    return 'dcm';
  } else if (contentType === 'application/x-director') {
    return 'dir';
  } else if (contentType === 'application/msword') {
    return 'doc';
  } else if (contentType === 'application/x-autocad') {
    return 'dgw';
  } else if (contentType === 'application/x-expandedbook') {
    return 'ebk';
  } else if (contentType === 'chemical/x-embl-dl-nucleotide') {
    return 'emb';
  } else if (contentType === 'application/postscript') {
    return 'eps';
  } else if (contentType === 'audio/echospeech') {
    return 'es';
  } else if (contentType === 'application/x-earthtime') {
    return 'etc';
  } else if (contentType === 'text/x-setext') {
    return 'etx';
  } else if (contentType === 'images/x-freehand') {
    return 'fh4';
  } else if (contentType === 'application/x-maker') {
    return 'fm';
  } else if (contentType === 'video/isivideo') {
    return 'fvi';
  } else if (contentType === 'application/x-gzip') {
    return 'gz';
  } else if (contentType === 'application/winhlp') {
    return 'hlp';
  } else if (contentType === 'application/mac-binhex40') {
    return 'hqx';
  } else if (contentType === 'x-conference/x-cooltalk') {
    return 'ice';
  } else if (contentType === 'audio/melody') {
    return 'imy';
  } else if (contentType === 'application/x-ipscript') {
    return 'ips';
  } else if (contentType === 'application/x-ipix') {
    return 'ipx';
  } else if (contentType === 'audio/x-mod') {
    return 'xm';
  } else if (contentType === 'i-world/i-vrml') {
    return 'ivr';
  } else if (contentType === 'text/vnd.sun.j2me.app-descriptor') {
    return 'jad';
  } else if (contentType === 'application/java-archive') {
    return 'jar';
  } else if (contentType === 'application/x-java-jnlp-file') {
    return 'jnlp';
  } else if (contentType === 'application/x-javascript') {
    return 'js';
  } else if (contentType === 'application/fastman') {
    return 'lcc';
  } else if (contentType === 'application/x-digitalloca') {
    return 'lcl';
  } else if (contentType === 'application/x-digitalloca') {
    return 'lcr';
  } else if (contentType === 'video/x-ms-asf') {
    return 'lsf';
  } else if (contentType === 'video/x-ms-asf') {
    return 'lsx';
  } else if (contentType === 'application/x-msmediaview') {
    return 'm13';
  } else if (contentType === 'audio/x-mpegurl') {
    return 'm3u';
  } else if (contentType === 'magnus-internal/imagemap') {
    return 'map';
  } else if (contentType === 'application/mbedlet') {
    return 'mbd';
  } else if (contentType === 'application/x-mascot') {
    return 'mct';
  } else if (contentType === 'application/x-msaccess') {
    return 'mdb';
  } else if (contentType === 'audio/x-mod') {
    return 'xmd';
  } else if (contentType === 'text/x-vmel') {
    return 'mel';
  } else if (contentType === 'aapplication/x-mif') {
    return 'mi';
  } else if (contentType === 'images/x-cals') {
    return 'mil';
  } else if (contentType === 'application/x-skt-lbs') {
    return 'mmf';
  } else if (contentType === 'application/x-msmoney') {
    return 'mny';
  } else if (contentType === 'application/x-mocha') {
    return 'moc';
  } else if (contentType === 'application/x-yumekara') {
    return 'mof';
  } else if (contentType === 'chemical/x-mdl-molfile') {
    return 'mol';
  } else if (contentType === 'chemical/x-mopac-input') {
    return 'mop';
  } else if (contentType === 'video/quicktime') {
    return 'mov';
  } else if (contentType === 'audio/x-mpeg') {
    return 'mp3';
  } else if (contentType === 'application/vnd.mpohun.certificate') {
    return 'mpc';
  } else if (contentType === 'audio/mpeg') {
    return 'mpga';
  } else if (contentType === 'application/vnd.mophun.application') {
    return 'mpn';
  } else if (contentType === 'application/vnd.ms-project') {
    return 'mpp';
  } else if (contentType === 'application/x-mapserver') {
    return 'mps';
  } else if (contentType === 'text/x-mrml') {
    return 'mrl';
  } else if (contentType === 'application/metastream') {
    return 'mts';
  } else if (contentType === 'application/zip') {
    return 'nar';
  } else if (contentType === 'application/x-netcdf') {
    return 'nc';
  } else if (contentType === 'application/x-scream') {
    return 'nmz';
  } else if (contentType === 'images/vnd.nok-oplogo-color') {
    return 'nokia-op-logo';
  } else if (contentType === 'application/x-netfpx') {
    return 'npx';
  } else if (contentType === 'application/x-neva1') {
    return 'nva';
  } else if (contentType === 'application/x-AtlasMate-Plugin') {
    return 'oom';
  } else if (contentType === 'audio/x-epac') {
    return 'pae';
  } else if (contentType === 'images/x-portable-bitmap') {
    return 'pbm';
  } else if (contentType === 'application/font-tdpfr') {
    return 'pfr';
  } else if (contentType === 'images/x-portable-graymap') {
    return 'pgm';
  } else if (contentType === 'application/x-perl') {
    return 'pm';
  } else if (contentType === 'images/x-portable-anymap') {
    return 'pnm';
  } else if (contentType === 'application/vnd.ms-powerpoint') {
    return 'ppt';
  } else if (contentType === 'images/x-portable-pixmap') {
    return 'ppm';
  } else if (contentType === 'application/x-cprplayer') {
    return 'pqf';
  } else if (contentType === 'application/x-ns-proxy-autoconfig') {
    return 'proxy';
  } else if (contentType === 'application/postscript') {
    return 'ps';
  } else if (contentType === 'application/listenup') {
    return 'ptlk';
  } else if (contentType === 'application/x-mspublisher') {
    return 'pub';
  } else if (contentType === 'audio/vnd.qcelp') {
    return 'qcp';
  } else if (contentType === 'video/quicktime') {
    return 'qt';
  } else if (contentType === 'images/x-quicktime') {
    return 'qti';
  } else if (contentType === 'text/vnd.rn-realtext3d') {
    return 'r3t';
  } else if (contentType === 'audio/x-pn-realaudio') {
    return 'ra';
  } else if (contentType === 'audio/x-pn-realaudiop') {
    return 'ram';
  } else if (contentType === 'application/x-rar-compressed') {
    return 'rar';
  } else if (contentType === 'images/x-cmu-raster') {
    return 'ras';
  } else if (contentType === 'application/rdf+xml') {
    return 'rdf';
  } else if (contentType === 'images/vnd.rn-realflash') {
    return 'rf';
  } else if (contentType === 'application/x-richlink') {
    return 'rlf';
  } else if (contentType === 'audio/x-pn-realaudio') {
    return 'rm';
  } else if (contentType === 'audio/x-pn-realaudio') {
    return 'rmvb';
  } else if (contentType === 'application/vnd.rn-realplayer') {
    return 'rnx';
  } else if (contentType === 'images/vnd.rn-realpix') {
    return 'rp';
  } else if (contentType === 'audio/x-pn-realaudio-plugin') {
    return 'rpm';
  } else if (contentType === 'images/vnd.rn-realflash') {
    return 'rf';
  } else if (contentType === 'text/vnd.rn-realtext') {
    return 'rt';
  } else if (contentType === 'x-lml/x-gps') {
    return 'rte';
  } else if (contentType === 'text/richtext') {
    return 'rtx';
  } else if (contentType === 'video/vnd.rn-realvideo') {
    return 'rv';
  } else if (contentType === 'application/x-rogerwilco') {
    return 'rwc';
  } else if (contentType === 'audio/x-mod') {
    return 's3m';
  } else if (contentType === 'application/x-supercard') {
    return 'sca';
  } else if (contentType === 'application/x-msschedule') {
    return 'scd';
  } else if (contentType === 'application/e-score') {
    return 'sdf';
  } else if (contentType === 'application/x-stuffit') {
    return 'sea';
  } else if (contentType === 'magnus-internal/parsed-html') {
    return 'shtml';
  } else if (contentType === 'application/presentations') {
    return 'shw';
  } else if (contentType === 'images/vnd.stiwap.sis') {
    return 'si7';
  } else if (contentType === 'images/vnd.lgtwap.sis') {
    return 'si9';
  } else if (contentType === 'application/vnd.symbian.install') {
    return 'sis';
  } else if (contentType === 'application/x-stuffit') {
    return 'sit';
  } else if (contentType === 'application/x-Koan') {
    return 'skt';
  } else if (contentType === 'application/x-salsa') {
    return 'slc';
  } else if (contentType === 'application/studiom') {
    return 'smp';
  } else if (contentType === 'audio/x-smd') {
    return 'smz';
  } else if (contentType === 'text/x-speech') {
    return 'spc';
  } else if (contentType === 'application/futuresplash') {
    return 'spl';
  } else if (contentType === 'application/x-sprite') {
    return 'spr';
  } else if (contentType === 'application/x-wais-source') {
    return 'src';
  } else if (contentType === 'application/hyperstudio') {
    return 'stk';
  } else if (contentType === 'audio/x-mod') {
    return 'stm';
  } else if (contentType === 'images/vnd') {
    return 'svf';
  } else if (contentType === 'images/svg-xml') {
    return 'svg';
  } else if (contentType === 'application/x-shockwave-flash') {
    return 'swfl';
  } else if (contentType === 'application/x-troff') {
    return 't';
  } else if (contentType === 'application/x-timbuktu') {
    return 'tbt';
  } else if (contentType === 'application/x-tar') {
    return 'tgz';
  } else if (contentType === 'application/vnd.eri.thm') {
    return 'thm';
  } else if (contentType === 'application/x-troff') {
    return 'tr';
  } else if (contentType === 'x-lml/x-gps') {
    return 'trk';
  } else if (contentType === 'application/x-msterminal') {
    return 'trm';
  } else if (contentType === 'audio/tsplayer') {
    return 'tsi';
  } else if (contentType === 'application/dsptype') {
    return 'tsp';
  } else if (contentType === 'text/tab-separated-values') {
    return 'tsv';
  } else if (contentType === 'text/tab-separated-values') {
    return 'tsv';
  } else if (contentType === 'application/t-time') {
    return 'ttz';
  } else if (contentType === 'text/plain') {
    return 'txt';
  } else if (contentType === 'audio/x-mod') {
    return 'ult';
  } else if (contentType === 'application/x-uuencode') {
    return 'uue';
  } else if (contentType === 'application/x-cdlink') {
    return 'vcd';
  } else if (contentType === 'application/vocaltec-media-desc') {
    return 'vmd';
  } else if (contentType === 'application/vocaltec-media-file') {
    return 'vmf';
  } else if (contentType === 'application/x-dreamcast-vms-info') {
    return 'vmi';
  } else if (contentType === 'audio/voxware') {
    return 'vox';
  } else if (contentType === 'audio/x-twinvq-plugin') {
    return 'vqe';
  } else if (contentType === 'audio/x-twinvq') {
    return 'vql';
  } else if (contentType === 'x-world/x-vream') {
    return 'vre';
  } else if (contentType === 'workbook/formulaone') {
    return 'vts';
  } else if (contentType === 'images/vnd.wap.wbmp') {
    return 'wbmp';
  } else if (contentType === 'application/vnd.xara') {
    return 'web';
  } else if (contentType === 'images/wavelet') {
    return 'wi';
  } else if (contentType === 'application/x-msmetafile') {
    return 'wmf';
  } else if (contentType === 'text/vnd.wap.wmlscript') {
    return 'ws';
  } else if (contentType === 'application/vnd.wap.wmlc' || contentType === 'application/vnd.wap.wmlscriptc') {
    return 'wmlc';
  } else if (contentType === 'x-lml/x-gps') {
    return 'wpt';
  } else if (contentType === 'application/x-mswrite') {
    return 'wri';
  } else if (contentType === 'x-world/x-vrml') {
    return 'wrz';
  } else if (contentType === 'video/wavelet') {
    return 'wv';
  } else if (contentType === 'video/x-ms-wvx') {
    return 'wvx';
  } else if (contentType === 'application/x-gzip') {
    return 'x-gzip';
  } else if (contentType === 'application/vnd.xara') {
    return 'xar';
  } else if (contentType === 'images/x-xbitmap') {
    return 'xbm';
  } else if (contentType === 'application/x-xdma') {
    return 'xdma';
  } else if (contentType === 'application/vnd.fujixerox.docuworks') {
    return 'xdw';
  } else if (contentType === 'application/xhtml+xml') {
    return 'xhtml';
  } else if (contentType === 'application/vnd.ms-excel') {
    return 'xlc';
  } else if (contentType === 'application/x-excel') {
    return 'xll';
  } else if (contentType === 'application/vnd.ms-excel') {
    return 'xls';
  } else if (contentType === 'application/x-xpinstall') {
    return 'xpi';
  } else if (contentType === 'images/x-xpixmap') {
    return 'xpm';
  } else if (contentType === 'images/x-xwindowdump') {
    return 'xwd';
  } else if (contentType === 'chemical/x-pdb') {
    return 'xyz';
  } else if (contentType === 'application/x-compress') {
    return 'z';
  } else {
    const suffixStr = contentType.split('/')[1] || '';
    if (!suffixStr) {
      return '';
    }
    const suffixStrs = suffixStr.split('-');
    return suffixStrs[suffixStrs.length - 1];
  }
};

const transUint8ArrayToBase64 = (array) => {
  let binary = '';
  const len = array.length;
  for (let i = 0; i < len; i++) {
    binary += String.fromCharCode(array[i]);
  }
  return window.btoa(binary).replace(/=/g, '');
};

const transBase64ToUint8Array = (base64) => {
  const rawData = atob(base64);
  const outputArray = new Uint8Array(rawData.length);
  for (let i = 0; i < rawData.length; ++i) {
    outputArray[i] = rawData.charCodeAt(i);
  }
  return outputArray;
};

export { dataURLtoBlob, isBase64, fileToBlob, blobTobase64, getBlobByUrl, fileToBuffer, getFileSuffixByContentType, transUint8ArrayToBase64, transBase64ToUint8Array };
