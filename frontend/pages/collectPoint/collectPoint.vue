
<template>
	<view class="container">
		<view  class="map-canvas">
			<map class="map" :enable-satellite="true" :enable-overlooking="true" :enable-rotate="false" :latitude="location.latitude" :longitude="location.longitude" :scale="scale"></map> 
		</view>	
		<cover-view class="control-desk">
			<cover-view class="data-box">
				<dataCell class="datacell" title="所在城市" data="赣州市" :height="90"></dataCell>
				<dataCell class="datacell" title="状态" data="采点中..." :height="90"></dataCell>
				<dataCell class="datacell" title="da" data="dsa" :height="90"></dataCell>
				<dataCell class="datacell" title="经度" data="114.9342°E" :height="90"></dataCell>
				<dataCell class="datacell" title="纬度" data="25.8921°N" :height="90"></dataCell>
				<dataCell class="datacell" title="海拔" data="a" :height="90"></dataCell>
			</cover-view>
		
		</cover-view>
		<cover-view class="start">
			开始
		</cover-view>
		<cover-view class="start">
		    结束
		</cover-view>
		
		<cover-view class="add" @click="gotoAddNewTrack()">
			<cover-image id="image" @click="gotoAddNewTrack()" src="../../static/collectPoint/addNewTrack.png" mode="aspectFill"></cover-image>
		</cover-view>
		<cover-view class="locating" @click="getLocation()">
			<cover-image id="locatingimg" @click="getLocation()" src="../../static/collectPoint/locating.png" mode="aspectFill"></cover-image>
		</cover-view>
	</view>
</template>
<script>
    import * as util from '../../utils/util.js'
    var formatLocation = util.formatLocation;
    // #ifdef APP-PLUS
    import permision from "../../common/permission.js"
    // #endif

    export default {
        data() {
            return {
                title: 'getLocation',
                hasLocation: false,
                location: {
					latitude:"",
					longitude:"",
					altitude:""
				},
                type: '',
				scale:17,
				address:"",
				speed:""
            }
        },
		onLoad() {
				console.log("onload")
				  // uni.getLocation({
				  //     type: 'gcj02',
					 //  geocode:true,
				  //     success:(res)=> {  
						//   //一定要写成箭头函数！！！
						//   console.log(res)
						//   this.location.longitude = res.longitude;
						//   this.location.latitude = res.latitude;
				  //         console.log('当前位置的经度：' + res.longitude);
				  //         console.log('当前位置的纬度：' + res.latitude);
				  //     }
				  // });
		},
		onUnload:function(){
			this.location ={
			};
		},

        methods: {
			gotoAddNewTrack(){
				//限制一次采集一条轨迹，需要一条轨迹采集完成后，才可开始下一条轨迹的采集
				// var trackMsg= uni.getStorageSync("trackMsg")
				// if(trackMsg){
				// 	uni.showToast({
				// 		icon:'none',
				// 		position:'center',
				// 		title:"请先完成当前轨道的采集"
				// 	})
				// 	return
				// }
				uni.navigateTo({
					url:"/pages/secondaryPage/addNewTrack/addNewTrack",
					success() {
						console.log("跳转成功")
					},
					fail(e) {
						console.log(e)
					}
				})
				
			},
            togglePopup(type) {
                this.type = type;
            },
            showConfirm() {
                this.type = 'showpopup';
            },
            hideConfirm() {
                this.type = '';
            },
            async getLocation() {
				console.log("点及了获取位置")
                // #ifdef APP-PLUS
                let status = await this.checkPermission();
                if (status !== 1) {
                    return;
                }
                // #endif
                // #ifdef MP-WEIXIN || MP-TOUTIAO || MP-QQ
                let status = await this.getSetting();
                if (status === 2) {
                    this.showConfirm();
                    return;
                }
                // #endif


                this.doGetLocation();
				console.log(this.location)
            },
            doGetLocation() {
				var that = this
				console.log("触发了获取位置")
                uni.getLocation({
					type:"gcj02",//wgs84返回的是gps坐标，gcj02 返回国测局坐标，高德定位仅支持gcj02坐标系，支持解析地址信息
					altitude:true,
					geocode:true,//解析出地理信息
					accuracy:"best",
					isHighAccuracy:"true",
					highAccuracyExpireTime:4000,
                    success: (res) => {
						console.log("获取位置成功")
						console.log(res)
						console.log(res.latitude)
						console.log(res.address)
						console.log(res.address.poiName)
						console.log(typeof(res.latitude))
                        that.hasLocation = true;
						that.location.latitude = res.latitude.toString()
						that.location.longitude = res.longitude.toString()
						
						try{
							//pc端res.altitude为null
							that.speed = res.speed.toString()
							that.location.altitude = res.altitude.toString()
							that.address = res.address.poiName
							if(res.address.poiName==undefined){
								that.address = "undefined"
							}	
						}catch(e){
							//TODO handle the exception
						}
						
                        // this.location = formatLocation(res.longitude, res.latitude);
						console.log(that.location)
						
                    },
                    fail: (err) => {
						console.log("调用接口路失败了")
                        // #ifdef MP-BAIDU
                        if (err.errCode === 202 || err.errCode === 10003) { // 202模拟器 10003真机 user deny
                            this.showConfirm();
                        }
                        // #endif
                        // #ifndef MP-BAIDU
                        if (err.errMsg.indexOf("auth deny") >= 0) {
                            uni.showToast({
								icon:'none',
                                title: "访问位置被拒绝"
                            })
                        } else {
                            uni.showToast({
								icon:'none',
                                title: err.errMsg
                            })
                        }
                        // #endif
                    }
                })
            },
            getSetting: function() {
                return new Promise((resolve, reject) => {
                    uni.getSetting({
                        success: (res) => {
                            if (res.authSetting['scope.userLocation'] === undefined) {
                                resolve(0);
                                return;
                            }
                            if (res.authSetting['scope.userLocation']) {
                                resolve(1);
                            } else {
                                resolve(2);
                            }
                        }
                    });
                });
            },
            openSetting: function() {
                this.hideConfirm();
                uni.openSetting({
                    success: (res) => {
                        if (res.authSetting && res.authSetting['scope.userLocation']) {
                            this.doGetLocation();
                        }
                    },
                    fail: (err) => {}
                })
            },
            async checkPermission() {
				console.log(permision)
                let status = permision.isIOS ? await permision.requestIOS('location') :
                    await permision.requestAndroid('android.permission.ACCESS_FINE_LOCATION');

                if (status === null || status === 1) {
                    status = 1;
                } else if (status === 2) {
                    uni.showModal({
                        content: "系统定位已关闭",
                        confirmText: "确定",
                        showCancel: false,
                        success: function(res) {
                        }
                    })
                } else if (status.code) {
                    uni.showModal({
                        content: status.message
                    })
                } else {
                    uni.showModal({
                        content: "需要定位权限",
                        confirmText: "设置",
                        success: function(res) {
                            if (res.confirm) {
                                permision.gotoAppSetting();
                            }
                        }
                    })
                }

                return status;
            },
            clear: function() {
                this.hasLocation = false
            }
        }
    }
</script>

<style>
	uni-page-body,html,body{
		height: 100%;
		box-sizing: border-box;
		background: #ffffff;/* //默认的全局背景色 */
	}
	.container{
		height:100vh;
		width: 100vw;
		position: relative;
		/* background: yellow; */
		
	}
	.add{
		top:  60%;
		right:5%;
	/* 	z-index: 3; */
		position: absolute;
		width: 100rpx;
		height: 100rpx;
		background: #ffffff;
		border-radius: 50%;
		/* background:green; */	
	}
	.datacell{
		float: left;
		margin-top: 15rpx;
		margin-left: 20rpx;
	}
	.locating{
		z-index: 5;
	/* 	display: inline-block; */
	    position: absolute;
		width: 80rpx;
		height:80rpx;
		background: #ffffff;
		top: 70%;
		left:5%;
		border-radius: 50%;
		
	}
	
	#locatingimg{
		left: 15%;
		top: 15%;
		position: absolute;
		width: 70%;
		height: 70%;
	}
	.map-canvas {
	  width: 750rpx;
	  height: 100%;
	}  
	  
	.map{
		pointer-events: none;
		width: 750rpx;
		height: 100%;
		
	}
	.control-desk{
		/* color: aqua; */
	/* 	margin-bottom: 100rpx; */
		position: absolute;
		border-radius: 0 ;
		bottom: 0;
		width: 750rpx;
		height: 350rpx;
		background: rgba(255, 255, 255, 0.9);
	/* 	background: yellow; */
	}
	.data-box{
		position: absolute;
		height:70%;
		bottom: 0;
	/* 	background: red; */
		width: 90%;
		left: 5%;
	}
	.start{
		text-align: center;
		color: #ffffff;
		font-size: 50rpx;
		line-height: 90rpx;
		left: 37%;
		width: 26%;
		height: 8%;
		/* bottom: 30%; */
		background: rgba(0, 120, 212, 0.9);
		position: absolute;
		top:73%;
		border-radius: 50%;
	}

	#image{
		position: absolute;
		left: 10%;
		top: 10%;
		width: 80%;
		height: 80%;
	}
    .popup-view {
        width: 500rpx;
    }

    .popup-title {
        display: block;
        font-size: 16px;
        line-height: 3;
        margin-bottom: 10px;
        text-align: center;
    }

    .popup-buttons button {
        margin-left: 4px;
        margin-right: 4px;
    }
	::v-deep .amap-logo {
	  opacity: 0 !important;
	}
	::v-deep .amap-copyright {
	  opacity: 0 !important;
	}
</style>

<!-- <template>
	<view class="alarmManagement">
		
		<view class="map" >
			<web-view src="/static/html/map.html"></web-view>
		</view>
	</view>
</template> -->
