<script>
    const keepAlive = uni.requireNativePlugin('Ba-KeepAlive')
	console.log(keepAlive)
	
	export default {
		globalData: {  
		            countdowntime: '',
					iscountdownover:true,
					lefttime:0,//上次进程结束时候倒计时剩余的时间
					channelId: 'Ba-KeepAlive',
					channelName: "Ba-KeepAlive",
					title: "Ba-KeepAlive",
					content: "Ba-KeepAlive is running",
					dataResult: "",
					type: undefined
		        },
		onLaunch: function() {
			console.log('App Launch')	
			
			this.register()
			this.isRunning()
			this.whiteList()
			this.openWhiteList()
			this.getWhiteList()
			
			let user= uni.getStorageSync('user')
			if (user) {
				//存在则关闭启动页进入首
				// #ifdef APP-PLUS
				plus.navigator.closeSplashscreen()
				// #endif
			} else {
				//不存在则跳转至登录页
				uni.reLaunch({
					url: "/pages/login/login",
					success: () => {
						// #ifdef APP-PLUS
						plus.navigator.closeSplashscreen()
						// #endif
					}
				})
			}
			
			//启动的时候先判断一下是否倒计时结束
			var nowtime = new Date().getTime()
			console.log(nowtime)
			
			let endtime = uni.getStorageSync('endtime')
			//endtime = endtime.getTime()
			//如果endtime不为空
			if(endtime){
				//如果现在的时间小于上一次倒计时截止的时间
				if (nowtime<endtime){
					//把状态是否倒计时结束改为false，
					//然后在login的生命周期函数中判断
					//如果是false的话就执行倒计时函数
					this.globalData.lefttime=endtime-nowtime
					this.globalData.iscountdownover=false
					console.log(this.globalData.lefttime)
				}
			}
			
			console.log(endtime)
		},
		onShow: function() {
			console.log('App Show')
		
		},
		onHide: function() {
			console.log('App Hide')
			
		},
		methods:{
			 register() { //注册
				keepAlive.register({
						channelId: this.channelId,
						channelName: this.channelName,
						title: this.title,
						content: this.content,
					},
					(res) => {
						console.log(res);
						uni.showToast({
							title: res.msg,
							icon: "none",
							duration: 3000
						})
					});
			},
			isRunning() { //是否正在运行
				keepAlive.isRunning((res) => {
					console.log(res);
					uni.showToast({
						title: res.msg,
						icon: "none",
						duration: 3000
					})
				});
			},
			restart() { //重启
				keepAlive.restart((res) => {
					console.log(res);
					uni.showToast({
						title: res.msg,
						icon: "none",
						duration: 3000
					})
				});
			},
			unregister() { //注销
				keepAlive.unregister((res) => {
					console.log(res);
					uni.showToast({
						title: res.msg,
						icon: "none",
						duration: 3000
					})
				});
			},
			whiteList() { //获取并打开白名单
				keepAlive.whiteList({
					isDialog: true,
					reason: "轨迹服务",
					whiteType: 0,
				}, (res) => {
					console.log(res);
					uni.showToast({
						title: res.msg,
						icon: "none",
						duration: 3000
					})
				});
			},
			openWhiteList() { //打开白名单
				try {
					var list = JSON.parse(this.dataResult);
					keepAlive.openWhiteList({
						isDialog: true,
						reason: "轨迹服务",
						type: list[0].type,
					}, (res) => {
						console.log(res);
						uni.showToast({
							title: res.msg,
							icon: "none",
							duration: 3000
						})
					});
				} catch (e) {
					uni.showToast({
						title: "请先获取有效的白名单项",
						icon: "none",
						duration: 3000
					})
				}
			},
			getWhiteList() { //获取白名单项
				keepAlive.getWhiteList({
					whiteType: 0,
				}, (res) => {
					console.log(res);
					this.dataResult = res.data ? JSON.stringify(res.data) : "";

					uni.showToast({
						title: res.msg,
						icon: "none",
						duration: 3000
					})
				});
			},
		}
	}
</script>

<style>
	/*每个页面公共css */

	@import './common/uni.css';
	.uni-tabbar {
			// tab 样式
			// 背景色
			background-color: #E8F4FD !important;
	
			// tabBar 样式
			// 上边框
			.uni-tabbar-border {
				background-color: transparent !important; // tabBar 上边框的颜色
			}
	
			.uni-tabbar__item {
	
				// tabBar 单项样式
				height: 100rpx !important;
	
				// &:not(:last-child) {
				// 	border-right: 4rpx solid #eee;
				// }
	
				.uni-tabbar__icon {
					// tabBar 图标样式
					width: 40rpx !important;
					height: 40rpx !important;
				}
	
				.uni-tabbar__label {
					// tabBar 文字样式
					// color:yellow !important
				}
			}
		}
		.uni-map-control{
			width: 0;
			height: 0;
		}
		.uni-map-control-icon{
			width: 0;
			height: 0;
		}

</style>
