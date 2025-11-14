<template>
	<view class="container" :style="{'width':2.2*height+'rpx','height':height+'rpx'}">
		<view class="title" :style="{'width':2.2*height+'rpx','height':0.4*height+'rpx','line-height':0.4*height+'rpx'}">
			<text :style="{'font-size':0.28*height+'rpx','font-weight':100}">{{title}}</text>
		</view>
		<view class="data" :style="{'width':2.2*height+'rpx','height':0.6*height+'rpx','line-height':0.6*height+'rpx'}">
			<text :style="{'line-height':0.6*height+'rpx','font-size':0.4*height+'rpx','font-weight':100,'color':'rgba(0, 120, 212, 0.9)'}">{{data}}</text>
		</view>
	</view>
</template>

<script>
	export default {
		name:"dataCell",
		props:{
			// width:{
			// 	type:Number,
			// 	default:180
			// },
			height:{
				type:Number,
				default:60
			},
			title:{
				type:String,
				default:""
			},
			data:{
				type:String,
				default:""
			}
			
		}
	}
</script>

<style>
	.container{
		position: relative;
	/* 	// background: red;
		// display: inline-block; */
	}
	.title{
		/* // width: 100%;
		// height: 40%; */
		
		/* background: yellow; */
		position: absolute;
		top: 0;
		left: 0;
	
	}
	.data{
		color:rgba(0, 120, 212, 0.9);
		/* width: 100%;
		height: 60%;
	    background: green; */
		position: absolute;
		left: 0;
		bottom: 0;	
	}
</style>