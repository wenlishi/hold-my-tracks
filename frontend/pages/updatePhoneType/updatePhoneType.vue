<template>
	<view class="container">
		<my-page-head title="设备更新"></my-page-head>
		<view class="body">
			<view class="uni-common-mt">
				<view class="uni-list">
					<view class="uni-list-cell">
						<view class="uni-pd">
							<view class="uni-label" style="width:350rpx;">设备型号</view>
						</view>
						<view class="uni-list-cell-db">
							<input class="uni-input" type="text" :disabled="true" placeholder="未获取" :value="systemInfo.model"/>
						</view>
					</view>
					<view class="uni-list-cell">
						<view class="uni-pd">
							<view class="uni-label" style="width:350rpx;">客户端平台</view>
						</view>
						<view class="uni-list-cell-db">
							<input class="uni-input" type="text" :disabled="true" placeholder="未获取" :value="systemInfo.platform"/>
						</view>
					</view>
					<view class="uni-list-cell">
						<view class="uni-pd">
							<view class="uni-label" style="width:350rpx;">操作系统版本</view>
						</view>
						<view class="uni-list-cell-db">
							<input class="uni-input" type="text" :disabled="true" placeholder="未获取" :value="systemInfo.system"/>
						</view>
					</view>
					<view class="uni-list-cell">
						<view class="uni-pd">
							<view class="uni-label" style="width:350rpx;">语言</view>
						</view>
						<view class="uni-list-cell-db">
							<input class="uni-input" type="text" :disabled="true" placeholder="未获取" :value="systemInfo.language"/>
						</view>
					</view>
					<!-- #ifdef MP -->
					<view class="uni-list-cell">
						<view class="uni-pd">
							<view class="uni-label" style="width:350rpx;">基础库版本</view>
						</view>
						<view class="uni-list-cell-db">
							<input class="uni-input" type="text" :disabled="true" placeholder="未获取" :value="systemInfo.SDKVersion"/>
						</view>
					</view>
					<!-- #endif -->
				</view>
				<view class="uni-padding-wrap">
					<view class="uni-btn-v">
						<button type="primary" @tap="renewPhoneType">更新登录设备信息</button>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { Debounce } from '@/utils/debounce.js'
	import api from '@/utils/api.js'
	export default {
		data() {
			return {
				title:'更新登录设备信息',
				systemInfo: {},
				username:""
			};
		},
		methods:{
			renewPhoneType:Debounce(function(){
				uni.getSystemInfo({
					success: (res) => {
						console.log(res)
						this.systemInfo = res
						console.log(this.systemInfo)
						//console.log("成功")
					}
				})
				var user = uni.getStorageSync("user")
				console.log("用户信息:", user)
				if (!user || !user.username) {
					uni.showToast({
						title: "用户信息获取失败，请重新登录",
						icon: 'none',
						position: 'center'
					})
					return
				}
				this.username = user.username

				// 使用新的API调用方式
				api.userApi.mobileUpdateStaffInfo(this.username, this.systemInfo.model)
					.then(res => {
						console.log("更新设备信息成功:", res);
						if (res.msg == "修改成功") {
							uni.showToast({
								title: "登录设备信息更新成功",
								position: 'center',
								icon: 'none'
							})
						} else {
							uni.showToast({
								title: "出错了，更新失败",
								icon: 'none',
								position: 'center'
							})
						}
					})
					.catch(err => {
						console.error('更新设备信息失败:', err);
						uni.showToast({
							title: '网络请求失败',
							icon: 'none'
						});
					});
			})
		}
	}
</script>

<style lang="scss">
	.container{
		position: relative;
		width: 100vw;
		height: 100vh;
	}
	.body{
		width: 100%;
		position: absolute;
		margin-top: 150rpx;
	}
</style>