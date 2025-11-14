/**
 * 轨迹数据下载导出工具
 * 基于蜗牛吐泡泡的下载代码进行优化，适配个人轨迹管理系统
 * 支持多平台、多格式导出
 */

// 创建文件夹
const createDir = async (path, callback) => {
    // #ifdef APP-PLUS
    // 申请本地存储读写权限
    plus.android.requestPermissions([
        'android.permission.WRITE_EXTERNAL_STORAGE',
        'android.permission.READ_EXTERNAL_STORAGE',
        'android.permission.INTERNET',
        'android.permission.ACCESS_WIFI_STATE'
    ], success => {
        const File = plus.android.importClass('java.io.File')
        let file = new File(path)
        // 文件夹不存在即创建
        if (!file.exists()) {
            file.mkdirs()
            callback && callback()
            return false
        }
        callback && callback()
        return false
    }, error => {
        uni.$u.toast('无法获取权限，文件下载将出错')
    })
    // #endif
    // #ifndef APP-PLUS
    // 非App环境直接执行回调
    callback && callback()
    // #endif
}

/**
 * 下载轨迹文件
 * @param {Object} fileInfo 文件信息
 * @param {string} fileInfo.fileUrl 文件下载URL
 * @param {string} fileInfo.fileName 文件名
 * @param {string} fileInfo.fileType 文件类型（gpx/kml/csv/json）
 * @param {string} customFolderName 自定义文件夹名称，默认为 '轨迹导出'
 */
export const downloadTrackFile = (fileInfo, customFolderName = '轨迹导出') => {
    // #ifdef APP-PLUS
    let osName = plus.os.name
    if (osName === 'Android') {
        let mkdirsName = '/' + customFolderName
        const Environment = plus.android.importClass('android.os.Environment')
        let path = 'file://' + Environment.getExternalStorageDirectory() + mkdirsName

        // 创建文件夹
        createDir(path, () => {
            uni.showLoading({
                title: '正在下载'
            })

            let dtask = plus.downloader.createDownload(fileInfo.fileUrl, {
                filename: path + '/' + fileInfo.fileName
            }, function(d, status) {
                uni.hideLoading()
                if (status == 200) {
                    let fileSaveUrl = plus.io.convertLocalFileSystemURL(d.filename)

                    uni.showModal({
                        title: '导出成功',
                        content: `轨迹文件已保存！\n\n文件：${fileInfo.fileName}\n保存路径：${mkdirsName}\n\n您可以在文件管理器的"${customFolderName}"文件夹中找到它。`,
                        showCancel: false,
                        confirmText: '知道了'
                    })
                } else {
                    plus.downloader.clear()
                    uni.showToast({
                        title: '下载失败，请重试',
                        icon: 'none'
                    })
                }
            })
            dtask.start()
        })
    } else {
        // iOS系统
        uni.showLoading({
            title: '正在下载'
        })

        let dtask = plus.downloader.createDownload(fileInfo.fileUrl, {}, function(d, status) {
            uni.hideLoading()
            if (status == 200) {
                let fileSaveUrl = plus.io.convertLocalFileSystemURL(d.filename)

                uni.showModal({
                    title: '导出成功',
                    content: `轨迹文件已保存！\n\n文件：${fileInfo.fileName}\n\n如需保存到本地，请在文件预览界面点击分享按钮保存。`,
                    showCancel: false,
                    confirmText: '打开文件',
                    success: function(res) {
                        if (res.confirm) {
                            uni.openDocument({
                                filePath: d.filename,
                                success: (sus) => {
                                    console.log('成功打开文件')
                                },
                                fail: (err) => {
                                    console.error('打开文件失败:', err)
                                }
                            })
                        }
                    }
                })
            } else {
                plus.downloader.clear()
                uni.showToast({
                    title: '下载失败，请重试',
                    icon: 'none'
                })
            }
        })
        dtask.start()
    }
    // #endif

    // #ifdef H5
    // H5环境直接打开下载链接
    window.open(fileInfo.fileUrl)
    uni.showModal({
        title: '导出成功',
        content: '文件已开始下载，请检查浏览器下载文件夹',
        showCancel: false
    })
    // #endif

    // #ifdef MP
    // 小程序环境复制链接
    uni.setClipboardData({
        data: fileInfo.fileUrl,
        success: () => {
            uni.$u.toast('链接已复制，请在浏览器打开下载')
        }
    })
    // #endif
}

/**
 * 批量下载轨迹文件
 * @param {Array} fileList 文件列表
 * @param {string} customFolderName 自定义文件夹名称
 */
export const batchDownloadTracks = (fileList, customFolderName = '轨迹批量导出') => {
    if (!fileList || fileList.length === 0) {
        uni.showToast({
            title: '没有可下载的文件',
            icon: 'none'
        })
        return
    }

    uni.showModal({
        title: '批量下载',
        content: `确定要下载 ${fileList.length} 个轨迹文件吗？`,
        success: (res) => {
            if (res.confirm) {
                // 显示下载进度
                let completedCount = 0
                const totalCount = fileList.length

                uni.showLoading({
                    title: `下载中 (0/${totalCount})`
                })

                // 逐个下载文件
                fileList.forEach((fileInfo, index) => {
                    setTimeout(() => {
                        downloadTrackFile(fileInfo, customFolderName)
                        completedCount++

                        if (completedCount === totalCount) {
                            uni.hideLoading()
                            uni.showToast({
                                title: '批量下载完成',
                                icon: 'success'
                            })
                        } else {
                            uni.showLoading({
                                title: `下载中 (${completedCount}/${totalCount})`
                            })
                        }
                    }, index * 1000) // 间隔1秒下载，避免同时下载过多文件
                })
            }
        }
    })
}

/**
 * 导出轨迹数据到本地文件
 * @param {Object} trackData 轨迹数据
 * @param {string} format 导出格式 (gpx/kml/csv/geojson)
 * @param {string} customFolderName 自定义文件夹名称
 */
export const exportTrackData = async (trackData, format = 'gpx', customFolderName = '轨迹导出') => {
    try {
        // 生成文件名
        const trackName = trackData.track?.trackName || '未命名轨迹'
        const timestamp = new Date().toISOString().slice(0, 19).replace(/:/g, '-')
        const fileName = `${trackName}_${timestamp}.${getFileExtension(format)}`

        // 调用后端导出API
        const response = await trackApi.exportTrack(trackData.track.id, format)

        // 创建文件信息对象
        const fileInfo = {
            fileUrl: response.url || window.URL.createObjectURL(new Blob([response.data])),
            fileName: fileName,
            fileType: format
        }

        // 下载文件
        downloadTrackFile(fileInfo, customFolderName)

    } catch (error) {
        console.error('导出轨迹数据失败:', error)
        uni.showToast({
            title: '导出失败，请重试',
            icon: 'none'
        })
    }
}

/**
 * 获取文件扩展名
 * @param {string} format 文件格式
 * @returns {string} 文件扩展名
 */
const getFileExtension = (format) => {
    const extensions = {
        'gpx': 'gpx',
        'kml': 'kml',
        'csv': 'csv',
        'geojson': 'json'
    }
    return extensions[format] || 'txt'
}

/**
 * 检查存储权限
 * @returns {Promise<boolean>} 是否有权限
 */
export const checkStoragePermission = () => {
    return new Promise((resolve) => {
        // #ifdef APP-PLUS
        if (plus.os.name === 'Android') {
            plus.android.requestPermissions([
                'android.permission.WRITE_EXTERNAL_STORAGE',
                'android.permission.READ_EXTERNAL_STORAGE'
            ], success => {
                resolve(true)
            }, error => {
                resolve(false)
            })
        } else {
            // iOS系统默认有权限
            resolve(true)
        }
        // #endif
        // #ifndef APP-PLUS
        // 非App环境默认有权限
        resolve(true)
        // #endif
    })
}

/**
 * 获取可用存储空间
 * @returns {Promise<number>} 可用空间（字节）
 */
export const getAvailableStorage = () => {
    return new Promise((resolve) => {
        // #ifdef APP-PLUS
        try {
            const Environment = plus.android.importClass('android.os.Environment')
            const StatFs = plus.android.importClass('android.os.StatFs')

            const path = Environment.getExternalStorageDirectory()
            const stat = new StatFs(path)
            const blockSize = stat.getBlockSize()
            const availableBlocks = stat.getAvailableBlocks()
            const availableBytes = blockSize * availableBlocks

            resolve(availableBytes)
        } catch (error) {
            console.error('获取存储空间失败:', error)
            resolve(-1) // 无法获取
        }
        // #endif
        // #ifndef APP-PLUS
        // 非App环境返回-1表示无法获取
        resolve(-1)
        // #endif
    })
}

/**
 * 格式化文件大小
 * @param {number} bytes 字节数
 * @returns {string} 格式化后的文件大小
 */
export const formatFileSize = (bytes) => {
    if (bytes === 0) return '0 B'

    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))

    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * 清理临时文件
 */
export const cleanupTempFiles = () => {
    // #ifdef APP-PLUS
    try {
        // 清理下载器缓存
        plus.downloader.clear()
        console.log('临时文件清理完成')
    } catch (error) {
        console.error('清理临时文件失败:', error)
    }
    // #endif
}