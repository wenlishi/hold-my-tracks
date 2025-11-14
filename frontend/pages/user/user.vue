<template>
	<view class="container">
		<view class="head">
			<!-- <view class="avatar">
				<image src="../../static/user/avatar.png"></image>
			</view> -->
			<!-- <fui-avatar class="avatar" size="large" src="../../static/user/avatar.png" radius="65"></fui-avatar> -->
			<view class="background-pic">
				<image src="../../static/user/staticbj.png" ></image>
			</view>	
			<text class="user-name">
			  用户名:{{this.username}}
			</text>	
		</view>
		<view class="content">
			<view class="list">
				<uni-list class="content-list" :border="false">
					<uni-list-item title="用户信息" class="first-list-item" link to="/pages/UserInfo/UserInfo"  @click="gotoGetUserInfo" showArrow
						thumb="/static/user/user-content/myaccount.png"
						thumb-size="medium"  :border="false"/>
					<uni-list-item title="设备更新"  showArrow :clickable="true" @click="gotoRenewPhoneType"
						thumb="/static/user/user-content/update.png"
						thumb-size="medium"  :border="false"/>
					<uni-list-item title="修改密码"  showArrow :clickable="true" @click="gotoChangePsd"
						thumb="/static/user/user-content/changepsd.png"
						thumb-size="medium"  :border="false"/>
					<uni-list-item title="帮助中心"  showArrow
						thumb="/static/user/user-content/help.png"
						thumb-size="medium"  :border="false"/>
					<uni-list-item title="退出登录" showArrow :clickable="true" @click="toLogout"
						thumb="/static/user/user-content/logout.png"
						thumb-size="medium"  :border="false"/>
					<uni-list-item :border="false"></uni-list-item>
					<uni-list-item class="last-list-item" :border="false"></uni-list-item>
				</uni-list>
			</view>
		</view>
	</view>
</template>

<script>
	// import {  
	//     mapMutations  
	// } from 'vuex';
	export default {
		data() {
			return {
				username:'',
			};
		},
		onLoad() {
			const user = uni.getStorageSync("user")
			this.username = user.username || '未登录'
		},
		methods:{
			// ...mapMutations(['logout']),

			// navTo(url){
			// 	this.$api.msg(`跳转到${url}`);
			// },
			//退出登录
			toLogout(){
				uni.showModal({
				    content: '确定要退出登录么',
				    success: (e)=>{
						console.log(e)
				    	if(e.confirm){
				    		// this.logout();
				    		// setTimeout(()=>{
				    		// 	uni.navigateBack();
				    		// }, 200)
							uni.removeStorageSync("user")
							uni.removeStorageSync("userinfo")
							console.log("清除成功")
							uni.reLaunch({url:"/pages/login/login"})
							
				    	}
				    }
				});
			},
			//switch
			switchChange(e){
				let statusTip = e.detail.value ? '打开': '关闭';
				this.$api.msg(`${statusTip}消息推送`);
			},
			//获取用户信息
			gotoGetUserInfo(){
     //            try {
     //            	const user = uni.getStorageSync('user');
					// this.username = user.username
					// console.log(this.username)
					// uni.request({
					// 	// #ifdef H5
					// 	url:`/api?action=getstaffinfo&username=${this.username}`,
					// 	// #endif 
					// 	// #ifndef H5
					// 	url:`http://www.zj-zp.com/lbssys/mobile.ashx?action=getstaffinfo&username=${this.username}`,
					// 	// #endif 
					// 	header: {
					// 		'content-type': 'application/x-www-form-urlencoded'
					// 	},
					// 	method: 'GET',
					// 	success(res) {
					// 		console.log(res)
					// 	}
					// })
     //            } catch (e) {
     //            	console.log(e)
     //            }	
			},
			gotoRenewPhoneType(){
				uni.navigateTo({
					url:"/pages/updatePhoneType/updatePhoneType"
				})
			},
			gotoChangePsd(){
				uni.navigateTo({
					url:"/pages/changePsd/changePsd"
				})
			}
		}
	}
</script>

<style lang='scss'>
	.container{
		position: relative;
		width:100vw;
		/* height: 100vh; */
		background: #F5F6F8;
	}
	.head{
		height: 400rpx;
		width: 750rpx;
		position: relative;
		border-radius: 0 0 25rpx 25rpx;
		/* background: linear-gradient(to bottom,#03A99F,#037CD5); */
		image{
			border-radius: 0 0 40rpx 40rpx ;
		}
		/* background: yellowgreen; */	
			.background-pic{
				position:absolute;
				height: 100%;
				width: 100%;
					image{
						width: 100%;
						height: 100%;
					}
			}
			.user-name{
				position:absolute;
				/* z-index: 2; */
				top: 40%;
				left: 30%;
				color: #fafafa;
				font-size: 1.0em;
			}
	}
	.avatar{
		width: 100rpx;
		height: 100rpx;
		position: absolute;
		top: 20%;
		left: 10%;
		z-index: 3;
		border-radius: 50%;
		image{
			width: 100%;
			height:100%;
		};
	}
	
	.content{
		position: relative;
		margin: 90rpx 0;
		width: 100%;
		height: 50%;
		background: #F5F6F8;
		display: flex;
		justify-content: center;
		align-items: center;
	}
	.list{
		position: absolute;
		/* background: yellow; */
		border-radius: 25rpx;
		width: 90%;
		height: 100%;
		
		
	}
	.content-list{
		border-radius: 25rpx;
	}
	.first-list-item{
		border-radius: 25rpx 25rpx 0 0 ;
	}
	.last-list-item{
		border-radius: 0 0 25rpx 25rpx;
		
	}
	
	.my_list_msg{
		image{
			width: 48rpx;
			height: 48rpx;
		}
		view{
			background:#ffffff;
		}
		
	}

/* 	.background-pic{
		width: 100%;
		heght:100%;
		image{
			width: 100%;
			heght: 100%;
		}
	} */
	
	/* page{
		background: #fafafa;
	}
	.list-cell{
		display:flex;
		align-items:baseline;
		padding: 20upx;
		line-height:60upx;
		position:relative;
		background: #fff;
		justify-content: center;
		&.log-out-btn{
			margin-top: 40upx;
			.cell-tit{
				color: #fafafa;
				text-align: center;
				margin-right: 0;
			}
		}
		&.cell-hover{
			background:#fafafa;
		}
		&.b-b:after{
			left: 30upx;
		}
		&.m-t{
			margin-top: 16upx; 
		}
		.cell-more{
			align-self: baseline;
			font-size: 16rpx;
			color:#fafada;
			margin-left:10upx;
		}
		.cell-tit{
			flex: 1;
			font-size: 16rpx;
			color: #fafafa;
			margin-right:10upx;
		}
		.cell-tip{
			font-size: 16rpx;
			color: #fafafa;
			
		}
		switch{
			transform: translateX(16upx) scale(.84);
		}
	} */
</style>