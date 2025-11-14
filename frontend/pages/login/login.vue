<template>
	<view class="container" @click="judgeIfBlank">
		<view class="header"></view>
		<view class="content" >
			<form @submit="formSubmit">
			<view class="avatorWrapper">
				<view class="avator">
					<image class="img" src="../../static/loginmap.png" mode="widthFix"></image>
				</view>
			</view>
			<view class="form" >
				<view class="inputadmin" @mousedown="showClearAccount">
					<view class="accountlogo">
						<image src="../../static/input/account.png" mode=""></image>
					</view>
					<input :disabled="disabled" v-model="username" class="input"  name="username"  type="text"  :placeholder="disabled==false?'请输入账号':'请等待'+countdowns+'秒后重新输入'" @focus="showClearAccount" @click="hideClearPassword" @blur="blurAccount"/>
					<view class="clear" v-if="username&&showClearAct&&!isBlank" @click="clearAccount">
						<image src="../../static/input/clear.png" mode=""></image>
					</view>
				</view>
				<view class="inputpsd" @mousedown="showClearPassword">
					<view class="passwordlogo">
						<image src="../../static/input/password.png" mode=""></image>
					</view>
					<input :disabled="disabled" :password="!showPassword" v-model="password" class="input"  name="password"  placeholder="请输入密码" @focus="showClearPassword" @click="hideClearAccount" @blur="blurPassword"/>
					<view class="clear" v-if="password&&showClearPsd&&!isBlank" @click="clearPassword">
						<image src="../../static/input/clear.png" mode=""></image>
					</view>
					<view class="password" @click="showPsd">
						<image :src="showPassword==true?'../../static/password/psd_show.png':'../../static/password/psd_hide.png'" mode="aspectFill"></image>
					</view>
				</view>
				<button :disabled="disabled" form-type="submit" class="loginBtn">登录</button>
			</view>
			</form>
		</view>

	    <view class="bottom">

	    </view>
	</view>
</template>

<script>
	import { Debounce } from '@/utils/debounce.js'
	import { authApi } from '@/utils/api.js'
	export default {
		data() {
			return {
				username:"",
				password:"",
				//blurpassword:'',
				disabled:false,
				failcount:0,
				countdownm:'',
				countdowns:180,
				showPassword:false,
				showClearAct:false,
				showClearPsd:false,
				blurPsd:true,
				blurAct:true,
				isBlank:false
			}
		},
		onLoad() {

		},
		//onShow生命周期函数的时候判断一下，如果是错的话就进行倒计时
	    onShow(){
			//failcount 是登录的失败次数
			if(uni.getStorageSync('failcount')){

				let res = uni.getStorageSync('failcount')
				this.failcount = parseInt(res)//用缓存中的failcoount更新页面数据
				///console.log(res)
				console.log(this.failcount)
			}

			//console.log("iscountdownover=="+getApp().globalData.iscountdownover)
			if(getApp().globalData.iscountdownover==false){
				var lefttime = getApp().globalData.lefttime
				this.countdowns = parseInt(lefttime/1000)
				console.log(this.countdowns)
				var timer = setInterval(()=>{
					this.countdowns-=1
					//console.log(this.failcount)
					if(this.countdowns==0){
						console.log("this.failcount == 0")
						this.failcount=0
						this.disabled=false
						this.countdowns=180
						console.log("uni.setStorageSync('failcount',this.failcount)")
						uni.setStorageSync('failcount',this.failcount)
						uni.removeStorageSync('endtime')
						clearInterval(timer)

					}
				},1000)
				//console.log("成功")
				this.disabled=true
				getApp().globalData.iscountdownover=true
			}else{
				if(this.failcount==5){
					this.failcount = 0
					uni.setStorageSync('failcount',this.failcount)
				}

			}
		},
		methods: {
					/**发布提交 */
					formSubmit:Debounce(function (e){
						//console.log(e)
						// console.log(e)
						var that = this;
						if(that.username==''){
							uni.showToast({
								title:"账户不能为空",
								mask:true,
								icon:'none',
								position:'bottom'
							})
						}
						if(that.username!=''&&that.password==''){
							uni.showToast({
								title:"密码不能为空",
								mask:true,
								icon:'none',
								position:'bottom',
		
							})
						}
						if(that.username!=''&&that.password!=''){
							// 使用新的API工具类进行登录
							authApi.login(that.username, that.password)
								.then(res => {
									console.log('登录成功:', res); // <--- 请务必在控制台查看这个打印结果
									
									// 登录失败的逻辑 (保持不变)
									if (res.status === 'error') {
										this.failcount += 1;
										uni.setStorageSync('failcount', this.failcount);
		
										console.log(this.failcount);
										if (this.failcount == 4) {
											uni.showToast({
												title: "如果连续五次输入错误,您将于三分钟后重新输入",
												duration: 2000,
												icon: 'none',
												mask: true,
												position: 'bottom'
											});
											return;
										}
										if (this.failcount == 5) {
											uni.showToast({
												title: "你已经连续五次输入错误,请于三分钟后重新输入",
												duration: 2000,
												icon: 'none',
												mask: true,
												position: 'bottom'
											});
											that.username = '';
											that.password = '';
											var nowtime = new Date();
											var nowtimevalue = nowtime.getTime(); //转成毫秒数
											var endtimevalue = new Date(nowtimevalue + 180000).getTime();
		
											uni.setStorageSync('endtime', endtimevalue);
											console.log(nowtime);
											console.log(endtimevalue);
											var timer = setInterval(() => {
												that.countdowns -= 1;
												if (that.countdowns == 0) {
													clearInterval(timer);
													this.failcount = 0;
													uni.setStorageSync('failcount', this.failcount);
													uni.removeStorageSync('endtime');
													this.disabled = false;
													this.countdowns = 180;
												}
											}, 1000);
											this.disabled = true;
											return;
										}
										uni.showToast({
											title: '账号或密码错误',
											icon: 'none',
											duration: 2000,
											mask: true,
											position: 'bottom'
										});
										
									/**************************************************
									 * * ▼▼▼▼▼▼▼▼▼▼ [修改部分] 开始 ▼▼▼▼▼▼▼▼▼▼
									 * * (这是登录成功时的 else 块)
									 * **************************************************/
									} else {
										// 用户密码输入正确
										
										// 1. 检查后端返回的数据中是否包含 token
										//    【请根据你 'console.log('登录成功:', res)' 的打印结果确认】
										//    如果token在res.data.token里, 就改成 res.data.token
										if (res.token) { 
											
											// 2. 【核心】将 token 保存到本地存储
											//    键名 'token' 必须和你的API拦截器中 getStorageSync('token') 一致
											uni.setStorageSync('token', res.token);
											console.log('Token 已成功保存!');
					
											// 3. (推荐) 保存用户信息，但不再保存明文密码
											uni.setStorageSync('user', { username: that.username });
					
											// 4. 重置失败计数器
											that.failcount = 0;
											uni.setStorageSync('failcount', that.failcount); // 确保存储中的也清零
					
											// 5. 提示并跳转
											uni.showToast({
												title: '登录成功',
												icon: 'success'
											});
											
											uni.switchTab({
												url: "/pages/map/map"
											});
											
										} else {
											// 登录请求成功了，但后端返回的数据里没有 token
											console.error('登录成功，但响应中未找到 token:', res);
											uni.showToast({
												title: '登录异常：未获取到Token',
												icon: 'none',
												duration: 2000,
												position: 'bottom'
											});
										}
									}
									/**************************************************
									 * * ▲▲▲▲▲▲▲▲▲▲ [修改部分] 结束 ▲▲▲▲▲▲▲▲▲▲
									 * **************************************************/
									 
								})
								.catch(err => {
									console.error('登录失败:', err);
									uni.showToast({
										icon: 'none',
										title: "请求接口失败",
										mask: true,
										duration: 2000,
										position: 'bottom'
									});
								});
						}
					},1000), // <-- Debounce 和 1000ms 保持不变
					
					//点击显示或隐藏密码 (保持不变)
					showPsd(){
						//确保禁用状态下无法点击
						if(this.disabled==false){
							this.showPassword=!this.showPassword
						}
		
					},
		
					// (以下所有方法均保持不变)
					showClearAccount(){
						//console.log(this.showClearAct)
						this.showClearAct=true
						this.blurAct = false
		
						//console.log(this.showClearAct)
						//console.log("isBlank == "+this.isBlank)
					},
					hideClearAccount(){
						this.showClearAct=false
						//console.log("aa")
					},
					//清空账号输入框
					clearAccount(){
		
						console.log(this.username)
						this.username = ''
					}
					,
					//显示或隐藏清空密码框功能图标
					showClearPassword(){
						this.showClearPsd=true
						this.blurPsd = false
					},
					hideClearPassword(){
						this.showClearPsd=false
					},
					//清空密码输入框
					clearPassword(){
						this.password = ''
					},
					blurAccount(){
						this.blurAct = true
					},
					blurPassword(){
						this.blurPsd = true
					},
					//判断点击的位置是不是空白处，如果点的是输入框就不是空白处
					judgeIfBlank(){
						this.isBlank = this.blurAct==true&&this.blurPsd==true
		
					}
				},
				
	}
</script>

<style lang="scss">
	.container {
		position: relative;
		background: #F5F5F5;
		height: 100vh;
		width: 100vw;

		.header{
			// height:40%;
			// // background: #037CD5;
			// background-image: url("../../static/title.png");
			// background-size: 100%;
			// background-repeat: no-repeat;
		}
		.content{
			position: absolute;
			background: #ffffff;
			border-radius: 25rpx;
			z-index: 2;

			/* 响应式布局 */
			/* 手机端 */
			width: 80%;
			left: 10%;
			top: 15%;
			height: 60%;

			/* 平板端 */
			@media (min-width: 768px) {
				width: 60%;
				left: 20%;
				top: 20%;
				height: 50%;
			}

			/* 桌面端 */
			@media (min-width: 1024px) {
				width: 40%;
				left: 30%;
				top: 25%;
				height: 45%;
			}
		}
		.bottom{
			position: absolute;
			width: 100%;
		    // background: #037CD5;
			height: 40%;
			background-image: url("../../static/backg.png");
			background-size: 100% 100%;
			bottom: 0;//使得盒子紧贴父级的底部
		}

	}
	.avatorWrapper{
		height: 20vh;
		width: 100%;
		display: flex;
		justify-content: center;
		align-items: flex-end;

		/* 响应式调整 */
		@media (min-width: 768px) {
			height: 25vh;
		}
	}
	.avator{
		width: 200rpx;
		height: 200rpx;
		overflow: hidden;

		/* 响应式调整 */
		@media (min-width: 768px) {
			width: 250rpx;
			height: 250rpx;
		}
	}
	.avator .img{
		width: 100%
	}
	.form{
		padding: 0 50rpx;
		margin-top: 100rpx;

		/* 响应式调整 */
		@media (min-width: 768px) {
			padding: 0 80rpx;
			margin-top: 120rpx;
		}

		@media (min-width: 1024px) {
			padding: 0 100rpx;
			margin-top: 150rpx;
		}
		.inputadmin{
			width: 100%;
			height: 80rpx;
			background: #FFFFFF;
			border-radius: 5rpx;
			box-sizing: border-box;
			padding: 0 20rpx;
			margin-top: 25px;
			//background: red;
			position: relative;
			box-shadow:0px 15px 15px -15px #AAA;

			/* 响应式调整 */
			@media (min-width: 768px) {
				height: 100rpx;
				margin-top: 30px;
			}

			@media (min-width: 1024px) {
				height: 120rpx;
				margin-top: 35px;
			}
			.accountlogo{
				position: absolute;
				left: 2%;
				width: 32rpx;
				height: 32rpx;
				//background: yellow;
				margin-top: 22rpx;
				z-index: 3;
				image{
					width: 100%;
					height: 100%;
				}

			}
			.input{
				position: absolute;
				left: 15%;
				width: 70%;
				height: 100%;
				text-align: center;
				//background: #3DD9D6;
				font-size: 30rpx;
			}
			.clear{
				position: absolute;
				left:92%;
				margin-top: 24rpx;
				width: 32rpx;
				height: 32rpx;
				//background: yellow;
				z-index: 3;
				image{
					width: 100%;
					height: 100%;
				}
			}
		}
		.inputpsd{
			width: 100%;
			height: 80rpx;
			background: #FFFFFF;
			border-radius: 5rpx;
			box-sizing: border-box;
			padding: 0 10rpx;
			margin-top: 25px;
			//background: red;
			position: relative;
			box-shadow:0px 15px 15px -15px #AAA;
			.passwordlogo{
				position: absolute;
				left: 2%;
				width: 32rpx;
				height: 32rpx;
				//background: yellow;
				margin-top: 22rpx;
				z-index: 3;
				image{
					width: 100%;
					height: 100%;
				}

			}
			.input{
				position: absolute;
				left: 15%;
				width: 70%;
				height: 100%;
				text-align: center;
				//background: #3DD9D6;
				font-size: 30rpx;
				//z-index: 3;

			}
			.clear{
				position: absolute;
				left:92%;
				margin-top: 24rpx;
				width: 32rpx;
				height: 32rpx;
				//background: yellow;
				z-index: 3;
				image{
					width: 100%;
					height: 100%;
				}
			}
			.password{
				position: absolute;
				left: 5%;
				margin-top: 24rpx;
				margin-left: 20rpx;
				width: 32rpx;
				height: 32rpx;
				//background: yellow;
				z-index: 2;
				image{
					width: 100%;
					height: 100%;
				}
		}
		}
		.loginBtn{
			width: 100%;
			height: 80upx;
			background: #037CD5;
			border-radius: 5rpx;
			margin-top: 50px;
			display: flex;
			justify-content: center;
			align-items: center;
			color: aliceblue;
			font-size: 32rpx;

			/* 响应式调整 */
			@media (min-width: 768px) {
				height: 100upx;
				margin-top: 60px;
				font-size: 36rpx;
			}

			@media (min-width: 1024px) {
				height: 120upx;
				margin-top: 70px;
				font-size: 40rpx;
			}
		}
	}
	// .loginBtn .btnValue{
	// 	color: white;
	// }
	// .forgotBtn{
	// 	text-align: center;
	// 	color: #EAF6F9;
	// 	font-size: 15px;
	// 	margin-top: 20px;
	// }
</style>