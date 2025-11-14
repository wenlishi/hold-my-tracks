<template>
	<view class="container">
		<my-page-head title="新建轨迹"></my-page-head>
		<view class="body">
			<view class="form-container">
				<view class="form-card">

					<view class="form-content">
						<view class="form-item">
							<view class="form-label">
								<text class="label-text">轨迹名称</text>
								<text class="required">*</text>
							</view>
							<view class="input-wrapper">
								<input
									class="form-input"
									type="text"
									placeholder="请输入轨迹名称"
									v-model="lineName"
									:class="{'input-error': showError && !lineName}"
								/>
								<view class="input-icon">
									<text class="iconfont">📍</text>
								</view>
							</view>
							<view v-if="showError && !lineName" class="error-message">
								轨迹名称不能为空
							</view>
						</view>

						<view class="form-item">
							<view class="form-label">
								<text class="label-text">轨迹简介</text>
								<text class="optional">(可选)</text>
							</view>
							<view class="textarea-wrapper">
								<textarea
									class="form-textarea"
									placeholder="请描述您的轨迹，例如：周末登山路线、城市漫步等..."
									v-model="description"
									maxlength="200"
								/>
								<view class="textarea-counter">
									{{ description.length }}/200
								</view>
							</view>
						</view>
					</view>

					<view class="form-actions">
						<button
							class="submit-btn"
							:class="{'btn-loading': isLoading}"
							@tap="handleSubmit"
							:disabled="isLoading"
						>
							<text v-if="!isLoading">创建轨迹</text>
							<text v-else class="loading-text">
								<text class="loading-dots">...</text>
								创建中
							</text>
						</button>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { Debounce } from '../../../utils/debounce.js';
	import myTrack from '../../../utils/myTrack.js'
    import Point from '../../../utils/Point.js';
	import api from '../../../utils/api.js';
	export default {
		data() {
			return {
				lineName:"",
				description:"",
				username:"",
				trackMsg:"",
				tracks:[],
				showError: false,
				isLoading: false
			};
		},
		methods:{
			handleSubmit() {
				// 表单验证
				if (!this.lineName.trim()) {
					this.showError = true
					uni.showToast({
						icon: 'none',
						title: '请输入轨迹名称',
						duration: 2000
					})
					return
				}

				this.showError = false
				this.isLoading = true
				this.addNewTrack()
			},

			addNewTrack:Debounce(function(){
				this.username = uni.getStorageSync("user").username
				var that = this

				// 使用封装的API方法创建轨迹
				api.trackApi.createTrack({
					trackName: that.lineName,
					description: that.description
				})
				.then(res => {
					console.log("新建轨迹成功:", res);

					uni.showToast({
						icon: 'success',
						title: "轨迹创建成功",
						duration: 1500
					})

					// 使用返回的轨迹ID
					that.trackMsg = res.id
					uni.setStorageSync("trackMsg", that.trackMsg)

					// 创建本地轨迹对象
					var track1 = new myTrack(that.trackMsg, that.lineName, that.description)

					// 更新本地轨迹列表
					if(uni.getStorageSync("tracks")){
						var tracks = uni.getStorageSync("tracks")
						that.tracks = tracks
						that.tracks.unshift(track1)
					}
					else{
						that.tracks.unshift(track1)
					}
					uni.setStorageSync("tracks", that.tracks)

					// 延迟跳转，让用户看到成功提示
					setTimeout(() => {
						// 这里可以添加跳转到坐标拾取页面的逻辑
						uni.navigateBack()
					}, 1500)
				})
				.catch(err => {
					console.error('新建轨迹失败:', err);
					uni.showToast({
						icon: 'error',
						title: "创建失败，请重试",
						duration: 2000
					})
				})
				.finally(() => {
					that.isLoading = false
				});
			}),

		}
	}
</script>

<style lang="scss">
.container {
	position: relative;
	width: 100vw;
	height: 100vh;
	background: #f8f9fa;
}

.body {
	width: 100%;
	padding: 150rpx 40rpx 40rpx;
	box-sizing: border-box;
}

.form-container {
	display: flex;
	justify-content: center;
	align-items: center;
	min-height: calc(100vh - 150rpx);
	margin-top: -160rpx;
}

.form-card {
	background: #ffffff;
	border-radius: 24rpx;
	padding: 60rpx 50rpx;
	box-shadow: 0 8rpx 40rpx rgba(0, 0, 0, 0.08);
	border: 1rpx solid #f0f0f0;
	width: 100%;
	max-width: 600rpx;
	animation: slideUp 0.6s ease-out;
}


.form-content {
	margin-bottom: 60rpx;
}

.form-item {
	margin-bottom: 50rpx;
}

.form-label {
	display: flex;
	align-items: center;
	margin-bottom: 20rpx;
}

.label-text {
	font-size: 32rpx;
	font-weight: 600;
	color: #2c3e50;
}

.required {
	color: #e74c3c;
	margin-left: 8rpx;
	font-size: 28rpx;
}

.optional {
	color: #95a5a6;
	margin-left: 8rpx;
	font-size: 26rpx;
}

.input-wrapper {
	position: relative;
}

.form-input {
	width: 100%;
	height: 100rpx;
	padding: 0 40rpx 0 80rpx;
	border: 2rpx solid #e8e8e8;
	border-radius: 16rpx;
	font-size: 32rpx;
	background: #ffffff;
	transition: all 0.3s ease;
	box-sizing: border-box;
}

.form-input:focus {
	border-color: #3498db;
	box-shadow: 0 0 0 4rpx rgba(52, 152, 219, 0.1);
	outline: none;
}

.form-input::placeholder {
	color: #bdc3c7;
}

.input-error {
	border-color: #e74c3c !important;
	box-shadow: 0 0 0 4rpx rgba(231, 76, 60, 0.1) !important;
}

.input-icon {
	position: absolute;
	left: 30rpx;
	top: 50%;
	transform: translateY(-50%);
	font-size: 36rpx;
}

.error-message {
	color: #e74c3c;
	font-size: 26rpx;
	margin-top: 12rpx;
	animation: shake 0.5s ease-in-out;
}

.textarea-wrapper {
	position: relative;
}

.form-textarea {
	width: 100%;
	height: 240rpx;
	padding: 30rpx;
	border: 2rpx solid #e8e8e8;
	border-radius: 16rpx;
	font-size: 32rpx;
	background: #ffffff;
	transition: all 0.3s ease;
	box-sizing: border-box;
	resize: none;
}

.form-textarea:focus {
	border-color: #3498db;
	box-shadow: 0 0 0 4rpx rgba(52, 152, 219, 0.1);
	outline: none;
}

.form-textarea::placeholder {
	color: #bdc3c7;
}

.textarea-counter {
	position: absolute;
	right: 20rpx;
	bottom: 20rpx;
	font-size: 24rpx;
	color: #95a5a6;
	background: rgba(255, 255, 255, 0.9);
	padding: 4rpx 12rpx;
	border-radius: 12rpx;
}

.form-actions {
	text-align: center;
}

.submit-btn {
	background: #3498db;
	color: white;
	border: none;
	border-radius: 16rpx;
	padding: 28rpx 80rpx;
	font-size: 34rpx;
	font-weight: 600;
	box-shadow: 0 4rpx 16rpx rgba(52, 152, 219, 0.2);
	transition: all 0.3s ease;
	position: relative;
	overflow: hidden;
}

.submit-btn:active {
	transform: translateY(1rpx);
	box-shadow: 0 2rpx 8rpx rgba(52, 152, 219, 0.3);
}

.submit-btn:disabled {
	background: #bdc3c7;
	box-shadow: none;
	transform: none;
}

.btn-loading {
	background: #95a5a6 !important;
}

.loading-text {
	display: flex;
	align-items: center;
	justify-content: center;
}

.loading-dots {
	animation: pulse 1.5s infinite;
	margin-right: 8rpx;
}

/* 动画效果 */
@keyframes slideUp {
	from {
		opacity: 0;
		transform: translateY(50rpx);
	}
	to {
		opacity: 1;
		transform: translateY(0);
	}
}

@keyframes shake {
	0%, 100% { transform: translateX(0); }
	25% { transform: translateX(-10rpx); }
	75% { transform: translateX(10rpx); }
}

@keyframes pulse {
	0%, 100% { opacity: 1; }
	50% { opacity: 0.5; }
}

/* 响应式设计 */
@media (max-width: 750rpx) {
	.body {
		padding: 150rpx 30rpx 30rpx;
	}

	.form-card {
		padding: 40rpx 30rpx;
		border-radius: 20rpx;
	}

	.form-title {
		font-size: 42rpx;
	}

	.form-subtitle {
		font-size: 26rpx;
	}
}
</style>