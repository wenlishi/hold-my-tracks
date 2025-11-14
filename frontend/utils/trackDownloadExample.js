/**
 * 轨迹数据下载导出工具使用示例
 * 演示如何使用 trackDownload.js 中的功能
 */

import {
    downloadTrackFile,
    batchDownloadTracks,
    exportTrackData,
    checkStoragePermission,
    getAvailableStorage,
    formatFileSize,
    cleanupTempFiles
} from './trackDownload.js'

/**
 * 示例1：单个轨迹文件下载
 */
export const exampleSingleDownload = () => {
    const fileInfo = {
        fileUrl: 'https://example.com/api/tracks/123/export/gpx',
        fileName: '晨跑轨迹_2024-01-01.gpx',
        fileType: 'gpx'
    }

    // 使用默认文件夹名称
    downloadTrackFile(fileInfo)

    // 或者使用自定义文件夹名称
    // downloadTrackFile(fileInfo, '我的轨迹文件')
}

/**
 * 示例2：批量轨迹文件下载
 */
export const exampleBatchDownload = () => {
    const fileList = [
        {
            fileUrl: 'https://example.com/api/tracks/123/export/gpx',
            fileName: '晨跑轨迹_2024-01-01.gpx',
            fileType: 'gpx'
        },
        {
            fileUrl: 'https://example.com/api/tracks/124/export/kml',
            fileName: '夜跑轨迹_2024-01-02.kml',
            fileType: 'kml'
        },
        {
            fileUrl: 'https://example.com/api/tracks/125/export/csv',
            fileName: '骑行轨迹_2024-01-03.csv',
            fileType: 'csv'
        }
    ]

    // 批量下载到自定义文件夹
    batchDownloadTracks(fileList, '我的轨迹集合')
}

/**
 * 示例3：直接导出轨迹数据
 */
export const exampleExportTrackData = async () => {
    // 假设这是从API获取的轨迹数据
    const trackData = {
        track: {
            id: 123,
            trackName: '晨跑轨迹',
            description: '早晨跑步记录',
            startTime: '2024-01-01 06:00:00',
            endTime: '2024-01-01 07:00:00',
            totalDistance: 5000.0,
            totalPoints: 120,
            status: 2,
            createTime: '2024-01-01 06:00:00'
        },
        trackPoints: [
            // 轨迹点数据...
        ],
        stats: {
            totalDistance: 5000.0,
            totalPoints: 120,
            averageSpeed: 5.0,
            maxSpeed: 8.5,
            duration: 3600,
            altitudeChange: 50.0
        }
    }

    // 导出为不同格式
    await exportTrackData(trackData, 'gpx', '晨跑轨迹导出')
    // await exportTrackData(trackData, 'kml', '晨跑轨迹导出')
    // await exportTrackData(trackData, 'csv', '晨跑轨迹导出')
    // await exportTrackData(trackData, 'geojson', '晨跑轨迹导出')
}

/**
 * 示例4：检查权限和存储空间
 */
export const exampleCheckStorage = async () => {
    // 检查存储权限
    const hasPermission = await checkStoragePermission()

    if (!hasPermission) {
        uni.showModal({
            title: '权限提示',
            content: '需要存储权限才能下载文件，请授权后重试',
            showCancel: false
        })
        return
    }

    // 检查可用存储空间
    const availableBytes = await getAvailableStorage()

    if (availableBytes > 0) {
        const availableSize = formatFileSize(availableBytes)
        uni.showModal({
            title: '存储空间',
            content: `当前可用存储空间：${availableSize}`,
            showCancel: false
        })
    }
}

/**
 * 示例5：在轨迹详情页中使用
 * 可以集成到 pages/trackDetail/trackDetail.vue 中
 */
export const integrateWithTrackDetail = {
    methods: {
        // 导出轨迹方法
        async exportTrack(format) {
            try {
                // 检查权限
                const hasPermission = await checkStoragePermission()
                if (!hasPermission) {
                    uni.showToast({
                        title: '需要存储权限',
                        icon: 'none'
                    })
                    return
                }

                // 获取轨迹数据
                const trackData = this.trackDetail

                // 导出数据
                await exportTrackData(trackData, format, '个人轨迹')

            } catch (error) {
                console.error('导出失败:', error)
                uni.showToast({
                    title: '导出失败',
                    icon: 'none'
                })
            }
        },

        // 批量导出方法
        async batchExportTracks(trackList) {
            const fileList = trackList.map(track => ({
                fileUrl: `${this.baseUrl}/api/tracks/${track.id}/export/gpx`,
                fileName: `${track.trackName}.gpx`,
                fileType: 'gpx'
            }))

            batchDownloadTracks(fileList, '轨迹批量导出')
        }
    }
}

/**
 * 示例6：清理临时文件
 */
export const exampleCleanup = () => {
    // 在应用退出或需要清理时调用
    cleanupTempFiles()
}

/**
 * 使用说明：
 * 1. 在需要使用的页面中导入工具函数
 * 2. 根据需求调用相应的函数
 * 3. 处理权限和错误情况
 * 4. 提供用户友好的提示
 */

// 导出所有示例
module.exports = {
    exampleSingleDownload,
    exampleBatchDownload,
    exampleExportTrackData,
    exampleCheckStorage,
    integrateWithTrackDetail,
    exampleCleanup
}