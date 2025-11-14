//先生命一个mAMap对象

//使用mAMap对象之前需要先声明它。在使用高德地图API时，需要先在代码中创建一个MapView对象，然后通过调用getMap()方法获取mAMap对象的实例。示例代码如下：

// 创建MapView对象

MapView mapView = (MapView) findViewById(R.id.map_view);
//、R.id.map_view是布局文件中MapView控件的ID。
// 获取mAMap对象实例

mAMap = mapView.getMap();

//生成热力点坐标列表
LatLng[] latlngs = new LatLng[500];
double x = 39.904979;
double y = 116.40964;
 
for (int i = 0; i < 500; i++) {
double x_ = 0;
double y_ = 0;
x_ = Math.random() * 0.5 - 0.25;
y_ = Math.random() * 0.5 - 0.25;
latlngs[i] = new LatLng(x + x_, y + y_);
}


// 构建热力图 HeatmapTileProvider
HeatmapTileProvider.Builder builder = new HeatmapTileProvider.Builder();
builder.data(Arrays.asList(latlngs)) // 设置热力图绘制的数据
.gradient(ALT_HEATMAP_GRADIENT); // 设置热力图渐变，有默认值 DEFAULT_GRADIENT，可不设置该接口
// Gradient 的设置可见参考手册
// 构造热力图对象
HeatmapTileProvider heatmapTileProvider = builder.build();

// 初始化 TileOverlayOptions
TileOverlayOptions tileOverlayOptions = new TileOverlayOptions();
tileOverlayOptions.tileProvider(heatmapTileProvider); // 设置瓦片图层的提供者 
// 向地图上添加 TileOverlayOptions 类对象
mAMap.addTileOverlay(tileOverlayOptions);