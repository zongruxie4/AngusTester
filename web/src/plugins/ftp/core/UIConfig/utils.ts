import pako from 'pako';

const fileToArrayBuffer = (file:File):Promise<ArrayBuffer> => {
  return new Promise((resolve) => {
    const reader = new FileReader();
    reader.onload = function (event) {
      const result = event.target.result as ArrayBuffer;
      resolve(result);
    };

    reader.readAsArrayBuffer(file);
  });
};

const arrayBufferToGzipBase64 = (buffer:ArrayBuffer):string => {
  const uint8Array = pako.gzip(buffer);
  const base64 = btoa(String.fromCharCode.apply(null, uint8Array));
  return base64;
};

const arrayBufferToString = (buffer:ArrayBuffer):string => {
// 创建TextDecoder对象
  const decoder = new TextDecoder('utf-8');
  // 将ArrayBuffer转换为字符串
  return decoder.decode(buffer);
};

const arrayBufferToBase64 = (buffer:ArrayBuffer):string => {
  const uint8Array = new Uint8Array(buffer);
  const binary = String.fromCharCode.apply(null, uint8Array);
  return btoa(binary);
};

const stringToArrayBuffer = (data:string):ArrayBuffer => {
  const encoder = new TextEncoder();
  const encodedArray = encoder.encode(data);
  return encodedArray.buffer;
};

const base64ToArrayBuffer = (data:string):ArrayBuffer => {
  const binaryString = window.atob(data);
  const len = binaryString.length;
  const bytes = new Uint8Array(len);
  for (let i = 0; i < len; i++) {
    bytes[i] = binaryString.charCodeAt(i);
  }

  return bytes.buffer;
};

const gzipBase64ToArrayBuffer = (data:string):ArrayBuffer => {
  // 1. 将base64字符串转换为二进制字符串
  const binaryString = atob(data);

  // 2. 创建Uint8Array以便可以将二进制字符串转换为ArrayBuffer
  const len = binaryString.length;
  const bytes = new Uint8Array(len);
  for (let i = 0; i < len; i++) {
    bytes[i] = binaryString.charCodeAt(i);
  }

  // 3. 使用pako库解压缩数据
  const inflate = pako.ungzip(bytes);

  return inflate.buffer;
};

export {
  arrayBufferToGzipBase64,
  fileToArrayBuffer,
  arrayBufferToString,
  arrayBufferToBase64,
  stringToArrayBuffer,
  base64ToArrayBuffer,
  gzipBase64ToArrayBuffer
};
