import vue from '@vitejs/plugin-vue';
import { defineConfig } from 'vite';
import { resolve } from 'path';
import viteCompression from 'vite-plugin-compression';
import vueJsx from '@vitejs/plugin-vue-jsx';
import { nodePolyfills } from 'vite-plugin-node-polyfills';

import server from './server/dev-server';

export default defineConfig({
  server,
  plugins: [
    vue(),
    vueJsx(),
    viteCompression({
      algorithm: 'gzip', // 压缩算法
      ext: '.gz', // 压缩文件的扩展名
      threshold: 2048, // 文件大小达到该值（单位：字节）才会被压缩
      deleteOriginFile: false, // 是否删除原始文件
      verbose: true // 是否在控制台输出压缩信息
    }),
    nodePolyfills()
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      'src': resolve(__dirname, 'src')
    }
  },
  envDir: 'conf',
  clearScreen: false,
  json: {
    namedExports: true,
    stringify: false
  },
  build: {
    outDir: 'dist',
    target: 'es2020',
    minify: false,
    manifest: false,
    assetsDir: 'assets',
    cssCodeSplit: true,
    assetsInlineLimit: 4096,
    chunkSizeWarningLimit: 500,
    reportCompressedSize: false,
    rollupOptions: {
      external: ['canvg']
    }
  }
});
