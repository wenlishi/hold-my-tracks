<template>
<view class="container">
  <my-page-head title="精度计算"></my-page-head>
  <view class="page">
	
    <view class="form-item">
      <view class="form-label">实际路径的 x 坐标：</view>
      <view class="form-field">
         <textarea v-model="x" class="input-border" placeholder="以换行分隔"></textarea>
      </view>
    </view>
    <view class="form-item">
      <view class="form-label">实际路径的 y 坐标：</view>
      <view class="form-field">
        <textarea v-model="y" class="input-border" placeholder="以换行分隔"></textarea>
      </view>
    </view>
    
    <view class="form-item">
      <view class="form-field">
        <button @click="calculate">计算</button>
      </view>
    </view>
    <view class="result">
      <view v-for="(distance, index) in distances" :key="index">
        第 {{index+1}} 个点与实际路径的距离为：{{distance}}
      </view>
    </view>
  </view>
  </view>
</template>

<script>
  export default {
    data() {
      return {
        x: '',
        y: '',
        x_p: '',
        y_p: '',
        distances: []
      };
    },
    methods: {
      calculate() {
        const x = this.x.trim().split(' ').map(item => Number(item));
        const y = this.y.trim().split(' ').map(item => Number(item));
        const x_p = this.x_p.trim().split(' ').map(item => Number(item));
        const y_p = this.y_p.trim().split(' ').map(item => Number(item));
        const n = x.length;
        this.distances = [];
        for (let i = 0; i < x_p.length; i++) {
          let minDistance = Infinity;
          for (let j = 0; j < n; j++) {
            const distance = Math.sqrt(Math.pow(x[j] - x_p[i], 2) + Math.pow(y[j] - y_p[i], 2));
            minDistance = Math.min(minDistance, distance);
          }
          this.distances.push(minDistance);
        }
      }
    }
  };
</script>

<style>
	.container{
		position: relative;
	}
   .page {
     
	  top: 300rpx;
	  position: absolute;
      width: 100%;
      padding: 16px;
    }
    .form-item {
		
		
      width: 90%;
      max-width: 400px;
      margin-bottom: 16px;
    }
    .form-label {
      display: block;
      font-size: 14px;
      font-weight: bold;
      margin-bottom: 4px;
    }
    .form-field {
      display: flex;
      align-items: center;
    }
    .form-field input {
      flex: 1;
      padding: 8px;
      border: 1px solid #ccc;
      border-radius: 4px;
      font-size: 14px;
    }
    .result {
      margin-top: 16px;
    }
	  .input-border {
	    padding: 10px;
	    border: 1px solid #ccc;
	    border-radius: 5px;
	    font-size: 16px;
	    flex: 1;
	    max-width: 80%;
	    resize: vertical;
	  }
</style>