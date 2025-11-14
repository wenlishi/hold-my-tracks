<template>
	<view class="container">
		<my-page-head title="用户信息"></my-page-head>
		<view class="body">
			<view class="uni-common-mt">
				<view class="uni-list">
					<view class="uni-list-cell">
						<view class="uni-pd">
							<view class="uni-label" style="width:350rpx;">设备型号</view>
						</view>
						<view class="uni-list-cell-db">
							<input class="uni-input" type="text" :disabled="true" placeholder="未获取" :value="userinfo.PhoneType" />
						</view>
					</view>
					<view class="uni-list-cell">
						<view class="uni-pd">
							<view class="uni-label" style="width:350rpx;">账号</view>
						</view>
						<view class="uni-list-cell-db">
							<input class="uni-input" type="text" :disabled="true" placeholder="未获取"  :value="userinfo.Account"/>
						</view>
					</view>
					<view class="uni-list-cell">
						<view class="uni-pd">
							<view class="uni-label" style="width:350rpx;">昵称</view>
						</view>
						<view class="uni-list-cell-db">
							<input class="uni-input" type="text" :disabled="true" placeholder="未获取" :value="userinfo.Name"/>
						</view>
					</view>
					<view class="uni-list-cell">
						<view class="uni-pd">
							<view class="uni-label" style="width:350rpx;">电话</view>
						</view>
						<view class="uni-list-cell-db">
							<input class="uni-input" type="text" :disabled="true" placeholder="未获取" :value="userinfo.Phone" />
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
						<button type="primary" @tap="getSystemInfo">获取登录用户信息</button>
					</view>
				</view>
		    </view>
	    </view>
	</view>
</template>

<script>
	import { Debounce } from '@/utils/debounce.js';
	import api from '@/utils/api.js';
	export default {
		data() {
			return {
				title: '获取登录用户信息',
				username:"",
				userinfo:{}
				
			}
		},
		onLoad() {
			const user = uni.getStorageSync('user'); 
			    
			    if (user && user.username) { // 或者 user.username，取决于你存的是什么
			        this.username = user.username; // <--- 确保这里被赋值
			        
			        // 然后再调用
			        this.getSystemInfo(); 
			    } else {
			        console.error("无法获取到 username，用户未登录或缓存中没有");
			        uni.showToast({
			            title: '请先登录',
			            icon: 'none',
			            complete: () => {
			                // 跳转到登录页
			            }
			        });
			    }
		},
		onUnload:function(){
			this.userinfo ={};
		},
		methods: {
			getSystemInfo:Debounce(function () {
			            
			            // 3. 【已删除】删除 "this.username=user.username" 这一行
			            //    因为 this.username 已经在 onLoad 中被赋值了
			            
						console.log(this.username) // 调试：检查 this.username 是否有值
						var that = this
			            
			            // 4. 【已注释】旧的 uni.request 代码保持注释
						// try{ ... }catch(e){ ... }
			            
			            // 5. 【正确】使用 api.mobileRequest
						api.userApi.mobileGetStaffInfo(this.username)
						    .then(res => {
						        console.log("获取用户信息成功:", res);

						        that.userinfo = res; // 'res' 直接就是 userinfo 对象
						        uni.setStorageSync("userinfo", that.userinfo);

						        var userinfo = uni.getStorageSync("userinfo");
						        console.log("已存入缓存:", userinfo);

						    })
			                .catch(err => {
			                    // 6. 【推荐】加上 .catch 来处理错误
			                    console.error('获取用户信息失败:', err);
			                    uni.showToast({
			                        icon: 'none',
			                        position: 'center',
			                        title: "获取信息失败" // 拦截器会处理401，这里只处理500或网络失败
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
	.uni-list:after {
		position: absolute;
		z-index: 10;
		right: 0;
		bottom: 0;
		left: 0;
		height: 1px;
		content: '';
		-webkit-transform: scaleY(.5);
		transform: scaleY(.5);
		background-color: #c8c7cc;
	}

	.uni-list::before {
		position: absolute;
		z-index: 10;
		right: 0;
		top: 0;
		left: 0;
		height: 1px;
		content: '';
		-webkit-transform: scaleY(.5);
		transform: scaleY(.5);
		background-color: #c8c7cc;
	}
    .uni-pd {
    	padding-left: 30rpx;
    }
</style>
