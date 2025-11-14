package com.track.service;

import com.track.dto.TrackDetail;
import com.track.entity.TrackPoint;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * 轨迹导出服务
 * 支持GPX、KML、CSV格式导出
 */
@Service
public class TrackExportService {

    /**
     * 导出为GPX格式
     */
    public byte[] exportToGpx(TrackDetail trackDetail) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

        String trackName = trackDetail.getTrack().getTrackName() != null ?
                          trackDetail.getTrack().getTrackName() : "未命名轨迹";
        String description = trackDetail.getTrack().getDescription() != null ?
                           trackDetail.getTrack().getDescription() : "";

        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<gpx version=\"1.1\" creator=\"个人轨迹管理系统\" xmlns=\"http://www.topografix.com/GPX/1/1\">\n");
        writer.write("  <metadata>\n");
        writer.write("    <name>" + escapeXml(trackName) + "</name>\n");
        writer.write("    <desc>" + escapeXml(description) + "</desc>\n");
        writer.write("    <time>" + formatIsoTime(new Date()) + "</time>\n");
        writer.write("  </metadata>\n");
        writer.write("  <trk>\n");
        writer.write("    <name>" + escapeXml(trackName) + "</name>\n");
        writer.write("    <desc>" + escapeXml(description) + "</desc>\n");
        writer.write("    <trkseg>\n");

        List<TrackPoint> points = trackDetail.getTrackPoints();
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

        String trackName = trackDetail.getTrack().getTrackName() != null ?
                          trackDetail.getTrack().getTrackName() : "未命名轨迹";
        String description = trackDetail.getTrack().getDescription() != null ?
                           trackDetail.getTrack().getDescription() : "";

        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        writer.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n");
        writer.write("  <Document>\n");
        writer.write("    <name>" + escapeXml(trackName) + "</name>\n");
        writer.write("    <description>" + escapeXml(description) + "</description>\n");
        writer.write("    <Style id=\"trackStyle\">\n");
        writer.write("      <LineStyle>\n");
        writer.write("        <color>ff0078d4</color>\n");
        writer.write("        <width>4</width>\n");
        writer.write("      </LineStyle>\n");
        writer.write("    </Style>\n");
        writer.write("    <Placemark>\n");
        writer.write("      <name>" + escapeXml(trackName) + "</name>\n");
        writer.write("      <description>" + escapeXml(description) + "</description>\n");
        writer.write("      <styleUrl>#trackStyle</styleUrl>\n");
        writer.write("      <LineString>\n");
        writer.write("        <tessellate>1</tessellate>\n");
        writer.write("        <coordinates>\n");

        List<TrackPoint> points = trackDetail.getTrackPoints();
        for (TrackPoint point : points) {
            if (point.getLongitude() != null && point.getLatitude() != null) {
                writer.write("          " + point.getLongitude() + "," + point.getLatitude());
                if (point.getAltitude() != null) {
                    writer.write("," + point.getAltitude());
                }
                writer.write("\n");
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
     */
    public byte[] exportToCsv(TrackDetail trackDetail) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

        // 写入CSV表头
        writer.write("序号,经度,纬度,海拔(m),速度(m/s),卫星数量,地址,采集时间\n");

        List<TrackPoint> points = trackDetail.getTrackPoints();
        for (int i = 0; i < points.size(); i++) {
            TrackPoint point = points.get(i);

            writer.write((i + 1) + ",");
            writer.write((point.getLongitude() != null ? point.getLongitude().toString() : "") + ",");
            writer.write((point.getLatitude() != null ? point.getLatitude().toString() : "") + ",");
            writer.write((point.getAltitude() != null ? point.getAltitude().toString() : "") + ",");
            writer.write((point.getSpeed() != null ? point.getSpeed().toString() : "") + ",");
            writer.write((point.getSatelliteCount() != null ? point.getSatelliteCount().toString() : "") + ",");
            writer.write(escapeCsv(point.getAddress() != null ? point.getAddress() : "") + ",");
            writer.write((point.getCreateTime() != null ? formatDateTime(point.getCreateTime()) : ""));
            writer.write("\n");
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

        String trackName = trackDetail.getTrack().getTrackName() != null ?
                          trackDetail.getTrack().getTrackName() : "未命名轨迹";

        writer.write("{\n");
        writer.write("  \"type\": \"FeatureCollection\",\n");
        writer.write("  \"features\": [\n");
        writer.write("    {\n");
        writer.write("      \"type\": \"Feature\",\n");
        writer.write("      \"properties\": {\n");
        writer.write("        \"name\": \"" + escapeJson(trackName) + "\",\n");
        writer.write("        \"description\": \"" + escapeJson(trackDetail.getTrack().getDescription()) + "\",\n");
        writer.write("        \"totalPoints\": " + trackDetail.getTrackPoints().size() + "\n");
        writer.write("      },\n");
        writer.write("      \"geometry\": {\n");
        writer.write("        \"type\": \"LineString\",\n");
        writer.write("        \"coordinates\": [\n");

        List<TrackPoint> points = trackDetail.getTrackPoints();
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

        writer.write("        ]\n");
        writer.write("      }\n");
        writer.write("    }\n");
        writer.write("  ]\n");
        writer.write("}\n");

        writer.flush();
        return outputStream.toByteArray();
    }

    /**
     * 辅助方法：转义XML特殊字符
     */
    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }

    /**
     * 辅助方法：转义CSV特殊字符
     */
    private String escapeCsv(String text) {
        if (text == null) return "";
        if (text.contains(",") || text.contains("\"") || text.contains("\n")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }
        return text;
    }

    /**
     * 辅助方法：转义JSON特殊字符
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * 辅助方法：格式化ISO时间 (Date版本)
     */
    private String formatIsoTime(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return sdf.format(date);
    }

    /**
     * 辅助方法：格式化ISO时间 (LocalDateTime版本)
     */
    private String formatIsoTime(LocalDateTime localDateTime) {
        if (localDateTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return localDateTime.format(formatter);
    }

    /**
     * 辅助方法：格式化日期时间 (Date版本)
     */
    private String formatDateTime(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    /**
     * 辅助方法：格式化日期时间 (LocalDateTime版本)
     */
    private String formatDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(formatter);
    }
}