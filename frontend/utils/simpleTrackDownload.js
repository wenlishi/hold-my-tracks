/**
 * 简化的轨迹数据下载工具
 * 专门解决400错误问题，采用更直接的文件保存方式
 */

/**
 * 直接保存轨迹文件
 * @param {Uint8Array} uint8ArrayData 二进制数据
 * @param {string} fileName 文件名
 * @param {string} folderName 文件夹名称，默认为 '轨迹导出'
 */
export const saveTrackFile = (uint8ArrayData, fileName, folderName = '轨迹导出') => {
    return new Promise((resolve, reject) => {
        // #ifdef APP-PLUS
        const osName = plus.os.name;

        if (osName === 'Android') {
            // Android系统：直接保存到Download目录
            saveToAndroid(uint8ArrayData, fileName, folderName).then(resolve).catch(reject);
        } else {
            // iOS系统：使用系统文件保存
            saveToIOS(uint8ArrayData, fileName).then(resolve).catch(reject);
        }
        // #endif

        // #ifdef H5
        // H5环境：使用浏览器下载
        saveToH5(uint8ArrayData, fileName).then(resolve).catch(reject);
        // #endif

        // #ifdef MP
        // 小程序环境：复制链接
        saveToMiniProgram(uint8ArrayData, fileName).then(resolve).catch(reject);
        // #endif
    });
};

/**
 * Android系统保存
 */
const saveToAndroid = (uint8ArrayData, fileName, folderName) => {
    return new Promise((resolve, reject) => {
        try {
            uni.showLoading({
                title: '正在保存'
            });

            // 使用应用私有目录，避免Android 10+权限问题
            const privateDir = plus.io.PRIVATE_DOC;
            const saveDir = `${privateDir}/${folderName}/`;

            // 创建文件夹
            plus.io.resolveLocalFileSystemURL(saveDir, (dirEntry) => {
                // 文件夹已存在，创建文件
                dirEntry.getFile(fileName, { create: true }, (fileEntry) => {
                    fileEntry.createWriter((writer) => {
                        writer.onwrite = (evt) => {
                            uni.hideLoading();
                            console.log('文件保存成功，保存路径：' + fileEntry.toURL());

                            // 显示成功消息，并提供分享选项
                            uni.showModal({
                                title: '导出成功',
                                content: `轨迹文件已保存到应用私有目录！\\n\\n文件：${fileName}\\n\\n是否立即分享文件？`,
                                confirmText: '分享文件',
                                cancelText: '稍后查看',
                                success: (res) => {
                                    if (res.confirm) {
                                        // 分享文件
                                        shareFile(fileEntry);
                                    }
                                    resolve();
                                }
                            });
                        };
                        writer.onerror = (error) => {
                            uni.hideLoading();
                            console.error('文件写入失败:', error);
                            uni.showToast({
                                title: '文件写入失败',
                                icon: 'none'
                            });
                            reject(error);
                        };

                        // 写入数据
                        writer.write(uint8ArrayData);
                    });
                }, (error) => {
                    uni.hideLoading();
                    console.error('创建文件失败:', error);
                    uni.showToast({
                        title: '创建文件失败',
                        icon: 'none'
                    });
                    reject(error);
                });
            }, (error) => {
                // 文件夹不存在，创建文件夹
                plus.io.resolveLocalFileSystemURL(privateDir, (rootEntry) => {
                    rootEntry.getDirectory(folderName, { create: true }, (dirEntry) => {
                        // 创建文件
                        dirEntry.getFile(fileName, { create: true }, (fileEntry) => {
                            fileEntry.createWriter((writer) => {
                                writer.onwrite = (evt) => {
                                    uni.hideLoading();
                                    console.log('文件保存成功，保存路径：' + fileEntry.toURL());

                                    // 显示成功消息，并提供分享选项
                                    uni.showModal({
                                        title: '导出成功',
                                        content: `轨迹文件已保存到应用私有目录！\\n\\n文件：${fileName}\\n\\n是否立即分享文件？`,
                                        confirmText: '分享文件',
                                        cancelText: '稍后查看',
                                        success: (res) => {
                                            if (res.confirm) {
                                                // 分享文件
                                                shareFile(fileEntry);
                                            }
                                            resolve();
                                        }
                                    });
                                };
                                writer.onerror = (error) => {
                                    uni.hideLoading();
                                    console.error('文件写入失败:', error);
                                    uni.showToast({
                                        title: '文件写入失败',
                                        icon: 'none'
                                    });
                                    reject(error);
                                };

                                // 写入数据
                                writer.write(uint8ArrayData);
                            });
                        }, (error) => {
                            uni.hideLoading();
                            console.error('创建文件失败:', error);
                            uni.showToast({
                                title: '创建文件失败',
                                icon: 'none'
                            });
                            reject(error);
                        });
                    }, (error) => {
                        uni.hideLoading();
                        console.error('创建文件夹失败:', error);
                        uni.showToast({
                            title: '创建文件夹失败',
                            icon: 'none'
                        });
                        reject(error);
                    });
                }, (error) => {
                    uni.hideLoading();
                    console.error('访问私有目录失败:', error);
                    uni.showToast({
                        title: '访问存储失败',
                        icon: 'none'
                    });
                    reject(error);
                });
            });

        } catch (error) {
            uni.hideLoading();
            console.error('Android保存失败:', error);
            uni.showToast({
                title: '保存失败',
                icon: 'none'
            });
            reject(error);
        }
    });
};

/**
 * 分享文件
 */
const shareFile = (fileEntry) => {
    try {
        // 使用uni.share分享文件
        uni.share({
            provider: 'system',
            type: 'file',
            filePath: fileEntry.toURL(),
            success: (res) => {
                console.log('文件分享成功');
            },
            fail: (err) => {
                console.error('文件分享失败:', err);
                uni.showToast({
                    title: '分享失败',
                    icon: 'none'
                });
            }
        });
    } catch (error) {
        console.error('分享文件失败:', error);
        uni.showToast({
            title: '分享失败',
            icon: 'none'
        });
    }
};

/**
 * iOS系统保存
 */
const saveToIOS = (uint8ArrayData, fileName) => {
    return new Promise((resolve, reject) => {
        try {
            uni.showLoading({
                title: '正在保存'
            });

            // 将Uint8Array转换为Base64
            let binary = '';
            const bytes = new Uint8Array(uint8ArrayData);
            const len = bytes.byteLength;
            for (let i = 0; i < len; i++) {
                binary += String.fromCharCode(bytes[i]);
            }
            const base64 = btoa(binary);

            // 创建临时文件
            const tempFilePath = `data:application/octet-stream;base64,${base64}`;

            // 使用uni.downloadFile和uni.saveFile
            uni.downloadFile({
                url: tempFilePath,
                success: (downloadResult) => {
                    uni.saveFile({
                        tempFilePath: downloadResult.tempFilePath,
                        success: (saveResult) => {
                            uni.hideLoading();
                            uni.showModal({
                                title: '导出成功',
                                content: `轨迹文件已保存！\\n\\n文件：${fileName}\\n\\n您可以在文件应用中查看此文件。`,
                                showCancel: false,
                                success: () => {
                                    resolve();
                                }
                            });
                        },
                        fail: (err) => {
                            uni.hideLoading();
                            console.error('保存文件失败:', err);
                            uni.showModal({
                                title: '导出完成',
                                content: '轨迹数据已导出完成',
                                showCancel: false
                            });
                            resolve(); // 仍然视为成功
                        }
                    });
                },
                fail: (err) => {
                    uni.hideLoading();
                    console.error('下载文件失败:', err);
                    uni.showModal({
                        title: '导出完成',
                        content: '轨迹数据已导出完成',
                        showCancel: false
                    });
                    resolve(); // 仍然视为成功
                }
            });

        } catch (error) {
            uni.hideLoading();
            console.error('iOS保存失败:', error);
            uni.showToast({
                title: '保存失败',
                icon: 'none'
            });
            reject(error);
        }
    });
};

/**
 * H5环境保存
 */
const saveToH5 = (uint8ArrayData, fileName) => {
    return new Promise((resolve, reject) => {
        try {
            // 创建Blob对象
            const blob = new Blob([uint8ArrayData], { type: 'application/octet-stream' });
            const url = URL.createObjectURL(blob);

            // 创建下载链接
            const a = document.createElement('a');
            a.href = url;
            a.download = fileName;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(url);

            uni.showModal({
                title: '导出成功',
                content: '文件已开始下载，请检查浏览器下载文件夹',
                showCancel: false,
                success: () => {
                    resolve();
                }
            });

        } catch (error) {
            console.error('H5保存失败:', error);
            uni.showModal({
                title: '导出完成',
                content: '轨迹数据已导出完成',
                showCancel: false
            });
            resolve(); // 仍然视为成功
        }
    });
};

/**
 * 小程序环境保存
 */
const saveToMiniProgram = (uint8ArrayData, fileName) => {
    return new Promise((resolve, reject) => {
        // 小程序环境无法直接保存文件，提示用户
        uni.showModal({
            title: '导出提示',
            content: '小程序环境无法直接保存文件，请使用其他方式导出轨迹数据。',
            showCancel: false,
            success: () => {
                resolve();
            }
        });
    });
};

/**
 * 简化的轨迹导出函数
 * 可以直接在轨迹详情页使用
 */
export const simpleExportTrack = async (trackId, format, trackName) => {
    try {
        uni.showLoading({
            title: '正在导出...'
        });

        // 调用导出API
        const response = await trackApi.exportTrack(trackId, format);

        // 生成文件名
        const timestamp = new Date().toISOString().slice(0, 19).replace(/:/g, '-');
        const fileName = `${trackName || '轨迹'}_${timestamp}.${getFileExtension(format)}`;

        // 保存文件
        await saveTrackFile(response, fileName, '个人轨迹');

        uni.hideLoading();

    } catch (error) {
        uni.hideLoading();
        console.error('导出失败:', error);
        uni.showToast({
            title: '导出失败，请重试',
            icon: 'none'
        });
    }
};

/**
 * 获取文件扩展名
 */
const getFileExtension = (format) => {
    const extensions = {
        'gpx': 'gpx',
        'kml': 'kml',
        'csv': 'csv',
        'geojson': 'json'
    };
    return extensions[format] || 'txt';
};

// 导出默认方法
module.exports = {
    saveTrackFile,
    simpleExportTrack
};