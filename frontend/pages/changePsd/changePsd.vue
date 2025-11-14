<template>
	<view class="container">
		<my-page-head title="修改密码"></my-page-head>
		<view class="body">
			<view class="uni-common-mt">
				<view class="uni-list">
					<view class="uni-list-cell">
						<view class="uni-pd">
							<view class="uni-label" style="width:180rpx;">新密码</view>
						</view>
						<view class="uni-list-cell-db">
							<input class="uni-input" type="text" v-model="newPassword" placeholder="请输入新密码"  />
						</view>
					</view>
					<view class="uni-list-cell">
						<view class="uni-pd">
							<view class="uni-label" style="width:180rpx;">确认密码</view>
						</view>
						<view class="uni-list-cell-db">
							<input class="uni-input" type="text" v-model="passwordConfirm" placeholder="请再次输入密码"  />
						</view>
					</view>
				</view>
		
			</view>
			<view class="uni-padding-wrap">
				<view class="uni-btn-v">
					<button type="primary" @tap="changePsd()">确定</button>
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
				username:"",
				newPassword:"",
				passwordConfirm:""
			};
		},
		methods:{
			goback(){
				uni.navigateBack()
			},
			changePsd:Debounce( function(){
				if(this.newPassword==""){
					uni.showToast({
						title:"请输入新密码",
						position:'center',
						icon:'none'
					})
					return 
				}
				if(this.passwordConfirm==""){
					uni.showToast({
						title:"请确认密码",
						icon:'none',
						position:'center'
					})
					return 
				}
				if(this.passwordConfirm===this.newPassword){
					var user = uni.getStorageSync("user")
					console.log("用户信息:", user)
					var username = user.username
					this.username = username
					console.log("修改密码的用户名:", username)
					console.log("新密码:", this.passwordConfirm)
					var that = this

					// 使用封装的API方法修改密码
					api.userApi.mobileUpdatePassword(that.username, that.passwordConfirm)
						.then(res => {
							console.log("修改密码成功:", res);
							if (res.msg == "密码修改成功") {
								uni.showToast({
									icon: 'none',
									position: 'center',
									title: "密码修改成功"
								})
							}
						})
						.catch(err => {
							console.error('修改密码失败:', err);
							uni.showToast({
								icon: 'none',
								position: 'center',
								title: "修改密码失败"
							})
						});				
				}
				else{
					uni.showToast({
						icon:'none',
						title:"两次输入密码不一致",
						position:'center'
					})
				}
			}) 
		}
	}
</script>

<style lang="scss">
	.container{
		position: relative;
	}
	// .title{
	// 	text-align:center;
	// 	line-height: 100rpx;
	// 	font-size: 32rpx;
	// 	font-weight: 600;
	// 	top: 0;
	// 	color: white;
	// 	position: fixed;
	// 	z-index: 2;
	// 	height: 100rpx;
	// 	width: 100vw;
	// 	background: #037CD5;
	// 	.goback{
	// 		position: absolute;
	// 		top: 15%;
	// 		left: 1%;
	// 		width: 50rpx;
	// 		height: 50rpx;
	// 		image{
	// 			width: 100%;
	// 			height: 100%;
	// 		}
	//  	}
	
	// }
	
	.body{
		width: 100%;
		position: absolute;
	    margin-top: 150rpx;
	}
	.content{
     	height: 400rpx;
		width: 100vw;
		background:#FFFCF3 ;
	}

</style>
