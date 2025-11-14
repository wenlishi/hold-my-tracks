// const path = require('path')

// module.exports = {
//   configureWebpack: {
//     resolve: {
//       alias: {
//         '@': path.resolve(__dirname, './')
//       }
//     }
//   },
//   transpileDependencies: ['@dcloudio/uni-app'],
//   devServer: {
//     port: '8080',
//     disableHostCheck: true,
//     proxy: {
//       '/api': {
//         target: 'http://localhost:8082', // 指向你的后端服务
//         changeOrigin: true,
//         pathRewrite: {
//           '^/api': '/api'
//         }
//       }
//     }
//   }
// }
