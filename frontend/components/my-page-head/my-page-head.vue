<template>
	<view class= "title" >
		<view v-if="showBack" class="goback" @click="goback">
			<image src="../../static/goback.png" mode=""></image>
		</view>
		{{title}}
	</view>
</template>

<script>
	export default {
		name: "page-head",
		props: {
			title: {
				type: String,
				default: ""
			},
			showBack: {
				type: Boolean,
				default: true
			}
		},
		methods:{
			goback(){
				uni.navigateBack()
			}
		}
	}
</script>

<style lang="scss">
	.title{
		text-align:center;
		line-height: 200rpx;
		font-size: 34rpx;
		font-weight: 600;
		top: 0;
		color: #ffffff;
		position: fixed;
		z-index: 2;
		height: 150rpx;
		width: 100vw;
		background: #037CD5;
		.goback{
			position: absolute;
			top:10%;
			left: 2%;
			width: 50rpx;
			height: 50rpx;
			image{
				width: 100%;
				height: 100%;
			}
		}
	
	}
</style>