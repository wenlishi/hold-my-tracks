package com.track.service;

import com.track.dto.TrackDetail;
import com.track.entity.Track;
import com.track.entity.TrackPoint;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * 轨迹导出服务
 * 支持GPX、KML、CSV、GeoJSON格式导出
 */
@Service
public class TrackExportService {

    /**
     * 根据格式生成带后缀的文件名
     */
    public String generateFileName(Track track, String format) {
        String name = (track != null && track.getTrackName() != null) ? track.getTrackName() : "未命名轨迹";
        // 去除文件名中的非法字符
        name = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        
        switch (format.toLowerCase()) {
            case "gpx": return name + ".gpx";
            case "kml": return name + ".kml";
            case "csv": return name + ".csv";
            case "geojson": return name + ".json";
            default: return name + ".txt";
        }
    }

    /**
     * 导出为GPX格式
     */
    public byte[] exportToGpx(TrackDetail trackDetail) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

        String trackName = getSafeString(trackDetail.getTrack().getTrackName(), "未命名轨迹");
        String description = getSafeString(trackDetail.getTrack().getDescription(), "");

        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<gpx version=\"1.1\" creator=\"TrackSystem\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n");
        writer.write("  <metadata>\n");
        writer.write("    <name>" + escapeXml(trackName) + "</name>\n");
        writer.write("    <desc>" + escapeXml(description) + "</desc>\n");
        // 使用当前 UTC 时间
        writer.write("    <time>" + formatIsoTime(new Date()) + "</time>\n");
        writer.write("  </metadata>\n");
        writer.write("  <trk>\n");
        writer.write("    <name>" + escapeXml(trackName) + "</name>\n");
        writer.write("    <desc>" + escapeXml(description) + "</desc>\n");
        writer.write("    <trkseg>\n");

        List<TrackPoint> points = trackDetail.getTrackPoints();
        if (points != null) {
            for (TrackPoint point : points) {
                if (point.getLatitude() != null && point.getLongitude() != null) {
                    writer.write("      <trkpt lat=\"" + point.getLatitude() + "\" lon=\"" + point.getLongitude() + "\">\n");
                    if (point.getAltitude() != null) {
                        writer.write("        <ele>" + point.getAltitude() + "</ele>\n");
                    }
                    if (point.getCreateTime() != null) {
                        writer.write("        <time>" + formatIsoTime(point.getCreateTime()) + "</time>\n");
                    }
                    writer.write("      </trkpt>\n");
                }
            }
        }

        writer.write("    </trkseg>\n");
        writer.write("  </trk>\n");
        writer.write("</gpx>\n");

        writer.flush();
        return outputStream.toByteArray();
    }

    /**
     * 导出为KML格式
     */
    public byte[] exportToKml(TrackDetail trackDetail) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

        String trackName = getSafeString(trackDetail.getTrack().getTrackName(), "未命名轨迹");
        String description = getSafeString(trackDetail.getTrack().getDescription(), "");

        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");
        writer.write("  <Document>\n");
        writer.write("    <name>" + escapeXml(trackName) + "</name>\n");
        writer.write("    <description>" + escapeXml(description) + "</description>\n");
        // 定义线条样式
        writer.write("    <Style id=\"trackStyle\">\n");
        writer.write("      <LineStyle>\n");
        writer.write("        <color>ff0078d4</color>\n");
        writer.write("        <width>4</width>\n");
        writer.write("      </LineStyle>\n");
        writer.write("    </Style>\n");
        writer.write("    <Placemark>\n");
        writer.write("      <name>" + escapeXml(trackName) + "</name>\n");
        writer.write("      <styleUrl>#trackStyle</styleUrl>\n");
        writer.write("      <LineString>\n");
        writer.write("        <tessellate>1</tessellate>\n");
        writer.write("        <coordinates>\n");

        List<TrackPoint> points = trackDetail.getTrackPoints();
        if (points != null) {
            for (TrackPoint point : points) {
                if (point.getLongitude() != null && point.getLatitude() != null) {
                    writer.write("          " + point.getLongitude() + "," + point.getLatitude());
                    if (point.getAltitude() != null) {
                        writer.write("," + point.getAltitude());
                    }
                    writer.write("\n");
                }
            }
        }

        writer.write("        </coordinates>\n");
        writer.write("      </LineString>\n");
        writer.write("    </Placemark>\n");
        writer.write("  </Document>\n");
        writer.write("</kml>\n");

        writer.flush();
        return outputStream.toByteArray();
    }

    /**
     * 导出为CSV格式
     * 【重要修复】添加 BOM 头，防止 Excel 打开中文乱码
     */
    public byte[] exportToCsv(TrackDetail trackDetail) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        // 【关键修复】写入 BOM (Byte Order Mark) 用于标识 UTF-8
        // Excel 看到这三个字节才会用 UTF-8 打开，否则中文会乱码
        outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        
        Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

        // 写入CSV表头
        writer.write("序号,经度,纬度,海拔(m),速度(m/s),卫星数量,地址,采集时间\n");

        List<TrackPoint> points = trackDetail.getTrackPoints();
        if (points != null) {
            for (int i = 0; i < points.size(); i++) {
                TrackPoint point = points.get(i);

                writer.write((i + 1) + ",");
                writer.write(getSafeString(point.getLongitude(), "") + ",");
                writer.write(getSafeString(point.getLatitude(), "") + ",");
                writer.write(getSafeString(point.getAltitude(), "") + ",");
                writer.write(getSafeString(point.getSpeed(), "") + ",");
                writer.write(getSafeString(point.getSatelliteCount(), "") + ",");
                // CSV 特殊字符转义
                writer.write(escapeCsv(point.getAddress()) + ",");
                writer.write(formatDateTime(point.getCreateTime())); // CSV通常用本地时间，比较直观
                writer.write("\n");
            }
        }

        writer.flush();
        return outputStream.toByteArray();
    }

    /**
     * 导出为GeoJSON格式
     */
    public byte[] exportToGeoJson(TrackDetail trackDetail) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

        String trackName = getSafeString(trackDetail.getTrack().getTrackName(), "未命名轨迹");
        String description = getSafeString(trackDetail.getTrack().getDescription(), "");
        int totalPoints = trackDetail.getTrackPoints() != null ? trackDetail.getTrackPoints().size() : 0;

        writer.write("{\n");
        writer.write("  \"type\": \"FeatureCollection\",\n");
        writer.write("  \"features\": [\n");
        writer.write("    {\n");
        writer.write("      \"type\": \"Feature\",\n");
        writer.write("      \"properties\": {\n");
        writer.write("        \"name\": \"" + escapeJson(trackName) + "\",\n");
        writer.write("        \"description\": \"" + escapeJson(description) + "\",\n");
        writer.write("        \"totalPoints\": " + totalPoints + "\n");
        writer.write("      },\n");
        writer.write("      \"geometry\": {\n");
        writer.write("        \"type\": \"LineString\",\n");
        writer.write("        \"coordinates\": [\n");

        List<TrackPoint> points = trackDetail.getTrackPoints();
        if (points != null) {
            for (int i = 0; i < points.size(); i++) {
                TrackPoint point = points.get(i);
                if (point.getLongitude() != null && point.getLatitude() != null) {
                    writer.write("          [" + point.getLongitude() + ", " + point.getLatitude());
                    if (point.getAltitude() != null) {
                        writer.write(", " + point.getAltitude());
                    }
                    writer.write("]");
                    if (i < points.size() - 1) {
                        writer.write(",");
                    }
                    writer.write("\n");
                }
            }
        }

        writer.write("        ]\n");
        writer.write("      }\n");
        writer.write("    }\n");
        writer.write("  ]\n");
        writer.write("}\n");

        writer.flush();
        return outputStream.toByteArray();
    }

    // ================= 辅助方法 =================

    private String getSafeString(Object value, String defaultVal) {
        return value != null ? value.toString() : defaultVal;
    }

    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    private String escapeCsv(String text) {
        if (text == null) return "";
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    /**
     * 【修复】转换为 UTC ISO 8601 格式 (yyyy-MM-dd'T'HH:mm:ss'Z')
     * GPX/KML 要求必须是 UTC 时间。
     * 假设服务器时间是系统默认时区 (如北京时间)，这里将其转换为 UTC。
     */
    private String formatIsoTime(Date date) {
        if (date == null) return "";
        return date.toInstant().toString(); // Java 8 Instant 默认就是 UTC ISO 格式
    }

    private String formatIsoTime(LocalDateTime localDateTime) {
        if (localDateTime == null) return "";
        // 1. 将 LocalDateTime (不带时区) 视为系统默认时区 (如 Asia/Shanghai)
        // 2. 转换为 UTC 时区
        // 3. 格式化为 ISO 字符串
        return ZonedDateTime.of(localDateTime, ZoneId.systemDefault())
                .withZoneSameInstant(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ISO_INSTANT); // 结果类似: 2023-10-27T10:00:00Z
    }

    /**
     * 本地化时间格式 (用于 CSV)
     */
    private String formatDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) return "";
        return localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}