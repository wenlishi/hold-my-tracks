/**
 * 轨迹导出工具类 (文本流兼容版)
 * 修复 Blob is not defined 问题
 */
const { trackApi } = require('@/utils/api.js');

export const simpleExportTrack = async (trackId, format, defaultName) => {
    try {
        console.log('>>> 开始导出流程');
        uni.showLoading({ title: '正在请求...', mask: true });

        // 1. 请求接口
        const res = await trackApi.exportTrack(trackId, format);
        
        // 2. 提取数据
        let data = res.data || res;
        
        // 3. 解析文件名
        let finalName = `${defaultName || '轨迹'}_${new Date().getTime()}.${format === 'geojson' ? 'json' : format}`;
        const headers = res.header || res.headers || {};
        const dispositionKey = Object.keys(headers).find(k => k.toLowerCase() === 'content-disposition');
        if (dispositionKey) {
            const disposition = headers[dispositionKey];
            let match = disposition.match(/filename\*=UTF-8''(.+)/i);
            if (match && match[1]) finalName = decodeURIComponent(match[1]);
            else {
                match = disposition.match(/filename="(.+?)"/i);
                if (match && match[1]) finalName = decodeURIComponent(match[1]);
            }
        }

        console.log('>>> 准备写入文件:', finalName);

        // 4. 执行保存 (带超时保护)
        await Promise.race([
            saveTrackFile(data, finalName),
            new Promise((_, reject) => setTimeout(() => reject(new Error('写入文件超时')), 10000))
        ]);
        
        console.log('>>> 流程结束');

    } catch (error) {
        console.error('>>> 导出异常:', error);
        uni.showModal({
            title: '导出失败',
            content: error.message || '未知错误',
            showCancel: false
        });
    } finally {
        uni.hideLoading();
    }
};

export const saveTrackFile = (data, fileName) => {
    return new Promise((resolve, reject) => {
        // #ifdef H5
        saveToH5(data, fileName);
        resolve();
        // #endif

        // #ifdef APP-PLUS
        saveToAppNative(data, fileName).then(resolve).catch(reject);
        // #endif
        
        // #ifndef H5 || APP-PLUS
        reject(new Error('不支持的运行环境'));
        // #endif
    });
};

// [App专用] Native 写入逻辑 (移除 Blob，使用字符串转换)
const saveToAppNative = (data, fileName) => {
    return new Promise((resolve, reject) => {
        // 1. 转换数据：二进制 -> 字符串
        // 既然是 GPX/CSV/KML，它们本质都是文本。我们手动把它转回字符串，避免写入乱码。
        let contentToWrite = '';
        try {
            if (typeof data === 'string') {
                contentToWrite = data;
            } else {
                // 兼容处理 Uint8Array / ArrayBuffer
                const uint8Arr = new Uint8Array(data);
                // 使用自定义方法处理 UTF-8 转换，防止中文乱码
                contentToWrite = uint8ArrayToString(uint8Arr);
            }
        } catch (e) {
            console.error('>>> 数据转换失败:', e);
            reject(new Error('数据格式转换失败'));
            return;
        }

        // 2. 写入文件
        plus.io.requestFileSystem(plus.io.PRIVATE_DOC, (fs) => {
            fs.root.getFile(fileName, { create: true }, (fileEntry) => {
                fileEntry.createWriter((writer) => {
                    
                    writer.onwrite = (e) => {
                        console.log('>>> 写入成功');
                        openFileWithSystem(fileEntry.toURL(), fileName);
                        resolve();
                    };

                    writer.onerror = (e) => {
                        console.error('>>> 写入失败', e);
                        reject(new Error('IO写入失败'));
                    };

                    // 直接写入字符串，plus.io 会自动处理
                    writer.write(contentToWrite);

                }, (e) => reject(e));
            }, (e) => reject(e));
        }, (e) => reject(e));
    });
};

/**
 * 辅助方法：将 Uint8Array (UTF-8) 转换为 字符串
 * 解决 App 端不支持 TextDecoder 和 Blob 的问题
 */
function uint8ArrayToString(fileData) {
    let dataString = "";
    for (let i = 0; i < fileData.length; i++) {
        dataString += String.fromCharCode(fileData[i]);
    }
    // 使用 decodeURIComponent + escape 解决中文乱码
    try {
        return decodeURIComponent(escape(dataString));
    } catch (e) {
        // 如果转换失败，返回原始 ASCII 字符（可能会乱码，但至少能写进去）
        console.warn('UTF-8 解码失败，降级处理');
        return dataString;
    }
}

const openFileWithSystem = (filePath, fileName) => {
    setTimeout(() => {
        uni.showModal({
            title: '导出成功',
            content: `文件已生成: ${fileName}\n\n请点击【打开/分享】发送给微信好友。`,
            confirmText: '打开/分享',
            showCancel: false,
            success: (res) => {
                if (res.confirm) {
                    uni.openDocument({
                        filePath: filePath,
                        showMenu: true,
                        fileType: 'txt', 
                        success: () => console.log('预览成功'),
                        fail: () => {
                            uni.share({
                                provider: "system",
                                type: "file",
                                filePath: filePath,
                                success: () => console.log("分享成功"),
                                fail: (e) => uni.showToast({ title: '无法调起分享', icon: 'none' })
                            });
                        }
                    });
                }
            }
        });
    }, 100);
};

const saveToH5 = (data, fileName) => {
    try {
        const blob = new Blob([data], { type: 'application/octet-stream' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
    } catch (e) { console.error(e); }
};

module.exports = { simpleExportTrack, saveTrackFile };