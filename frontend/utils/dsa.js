uni.request({
    url: 'https://www.xxx.com/xxx',//请求地址
    method: 'GET',//请求方法
    data: {},//请求参数
    header: {},//请求头
    success: (res) => {
        console.log(res.data);
    },
    fail: (err) => {
        console.log(err);
    }
 });



"app": {
  "launch_path": "/pages/launch/index"
}
<style>
  .uni-page-launch {
    background-color: #fff;
    display: flex;
    justify-content: center;
    align-items: center;
  }
</style>


uni.request({
    // #ifdef H5
    url:`/api?action=login&staffid=${that.staffid}&password=${that.password}`,
    // #endif 
    // #ifndef H5
    url:`http://www.zj-zp.com/lbssys/mobile.ashx?action=login&staffid=${that.staffid}&password=${that.password}`,
    // #endif 
    header: {
        'content-type': 'application/x-www-form-urlencoded'
    },
    method: 'GET',
    success: (res) => {
    //code
    }
})

if(res.data.status=="error"){							
    this.failcount+=1
    uni.setStorageSync('failcount',this.failcount)
    console.log(this.failcount)
    if (this.failcount==4){
        uni.showToast({
            title:"如果连续五次输入错误,您将于三分钟后重新输入",
            duration: 2000,
            icon:'none',
            mask:true,
            position:'bottom'
        })
        return 
    }
    if(this.failcount==5){
        uni.showToast({
            title:"你已经连续五次输入错误,请于三分钟后重新输入",
            duration:2000,
            icon:'none',
            mask:true,
            position:'bottom'
        })
        that.staffid=''
        that.password=''
        var nowtime = new Date()
        var nowtimevalue = nowtime.getTime()//转成毫秒数
        var endtimevalue = new Date(nowtimevalue+180000).getTime()
        
        //var endtime = new Date(endtimevalue)
        //uni.setStorageSync('endtime',endtime)
        uni.setStorageSync('endtime',endtimevalue)
        console.log(nowtime)
        //console.log(endtime)
        console.log(endtimevalue)
        var timer = setInterval(()=>{
            that.countdowns-=1
            //console.log(this.failcount)
            if(that.countdowns==0){
                clearInterval(timer)
                this.failcount=0
                uni.setStorageSync('failcount',this.failcount)
                uni.removeStorageSync('endtime')
                this.disabled=false
                this.countdowns=180
                
            }
        },1000)
        this.disabled=true
        return 
    }	
    uni.showToast({
        title: '账号或密码错误',
        icon: 'none',
        duration: 2000,
        mask:true,
        position:'bottom'
    });		
}

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

"Ba-KeepAlive" : {
    "__plugin_info__" : {
        "name" : "安卓保活（采用多种主流技术） Ba-KeepAlive - [试用版，仅用于自定义调试基座]",
        "description" : "原生保活插件，支持市面上大部分机型，Android4.4到13.0 。为定位、推送、websocket、定时任务、蓝牙、聊天等保驾护航（**注意：**不保证支持所有机型和场景，建议先试用再购买）",
        "platforms" : "Android",
        "url" : "https://ext.dcloud.net.cn/plugin?id=9423",
        "android_package_name" : "",
        "ios_bundle_id" : "",
        "isCloud" : true,
        "bought" : 0,
        "pid" : "9423",
        "parameters" : {}
    }
}
uni.onCompassChange((res) => {
    this.alpha = res.direction
  });

watch: {
alpha(val) {
    this.rotatePointer(val)
}
},