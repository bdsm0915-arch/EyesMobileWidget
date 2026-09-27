package com.example.eyesmobilewidget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GaugeRenderer {
    private static final int ACCENT = Color.rgb(255, 84, 120);

    private GaugeRenderer() {}

    public static Bitmap renderGauge(Context context, int usedPercent, String topLabel, String mainText,
                                     int labelColor, int trackColor) {
        return renderGauge(context, usedPercent, topLabel, mainText, labelColor, trackColor, true);
    }

    public static Bitmap renderGauge(Context context, int usedPercent, String topLabel, String mainText,
                                     int labelColor, int trackColor, boolean showRemainingArc) {
        int w = 280;
        int h = 178;
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(18f);

        float pad = 28f;
        RectF oval = new RectF(pad, 18f, w - pad, h + 96f);
        p.setColor(trackColor);
        c.drawArc(oval, 180f, 180f, false, p);

        int arcPercent = showRemainingArc ? Math.max(0, Math.min(100, 100 - usedPercent))
                : Math.max(0, Math.min(100, usedPercent));

        // Never invent a large 78% arc when the site did not provide a percentage.
        // A non-zero value with an unknown/rounded-to-zero percentage gets only a 1% marker.
        if (usedPercent == 0 && numericFromText(mainText) > 0
                && mainText != null
                && !mainText.contains("--")
                && !mainText.contains("확인 필요")) {
            arcPercent = 1;
        }

        p.setColor(ACCENT);
        c.drawArc(oval, 180f, 180f * arcPercent / 100f, false, p);

        p.setStyle(Paint.Style.FILL);
        p.setTextAlign(Paint.Align.CENTER);
        Typeface regular = ResourcesCompat.getFont(context, R.font.pretendard_regular);
        Typeface semiBold = ResourcesCompat.getFont(context, R.font.pretendard_semibold);
        if (regular == null) regular = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
        if (semiBold == null) semiBold = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);

        p.setTypeface(regular);
        p.setColor(labelColor);
        p.setTextSize(28f);
        c.drawText(topLabel, w / 2f, 94f, p);

        p.setTypeface(semiBold);
        p.setColor(ACCENT);
        p.setTextSize(mainText != null && mainText.length() > 7 ? 36f : 44f);
        String value = (mainText == null || mainText.trim().isEmpty()) ? "--" : mainText;
        c.drawText(value, w / 2f, 138f, p);
        return bmp;
    }

    public static Bitmap renderTextLine(Context context, String text, int color,
                                            boolean semiBold, float textSizePx,
                                            int width, int height) {
        Bitmap bmp = Bitmap.createBitmap(Math.max(1, width), Math.max(1, height), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        Typeface face = ResourcesCompat.getFont(context,
                semiBold ? R.font.pretendard_semibold : R.font.pretendard_regular);
        if (face == null) {
            face = Typeface.create(Typeface.SANS_SERIF,
                    semiBold ? Typeface.BOLD : Typeface.NORMAL);
        }
        p.setTypeface(face);
        p.setColor(color);
        p.setTextAlign(Paint.Align.CENTER);
        p.setTextSize(textSizePx);
        String value = text == null ? "" : text;
        Paint.FontMetrics fm = p.getFontMetrics();
        float baseline = (height - fm.bottom - fm.top) / 2f;
        c.drawText(value, width / 2f, baseline, p);
        return bmp;
    }

    public static Bitmap renderTextLineLeft(Context context, String text, int color,
                                             boolean semiBold, float textSizePx,
                                             int width, int height, float paddingPx) {
        return renderTextLineAligned(context, text, color, semiBold, textSizePx,
                width, height, Paint.Align.LEFT, paddingPx);
    }

    public static Bitmap renderTextLineRight(Context context, String text, int color,
                                              boolean semiBold, float textSizePx,
                                              int width, int height, float paddingPx) {
        return renderTextLineAligned(context, text, color, semiBold, textSizePx,
                width, height, Paint.Align.RIGHT, paddingPx);
    }

    private static Bitmap renderTextLineAligned(Context context, String text, int color,
                                                 boolean semiBold, float textSizePx,
                                                 int width, int height, Paint.Align align,
                                                 float paddingPx) {
        Bitmap bmp = Bitmap.createBitmap(Math.max(1, width), Math.max(1, height), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
        Typeface face = ResourcesCompat.getFont(context,
                semiBold ? R.font.pretendard_semibold : R.font.pretendard_regular);
        if (face == null) face = Typeface.create(Typeface.SANS_SERIF,
                semiBold ? Typeface.BOLD : Typeface.NORMAL);
        p.setTypeface(face);
        p.setColor(color);
        p.setTextAlign(align);
        p.setTextSize(textSizePx);
        String value = text == null ? "" : text;
        Paint.FontMetrics fm = p.getFontMetrics();
        float baseline = (height - fm.bottom - fm.top) / 2f;
        float x = align == Paint.Align.RIGHT ? width - paddingPx : paddingPx;
        c.drawText(value, x, baseline, p);
        return bmp;
    }

    public static Bitmap renderBodyBackground(Context context, int color) {
        int w = 1024;
        int h = 512;
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        c.drawRect(0f, 0f, w, h, p);
        return bmp;
    }

    public static String mainDisplay(String label, String used, String total) {
        if ("데이터".equals(label)) return dataRemainingOrUsed(used, total);
        if ("음성".equals(label)) return normalizeVoiceUsage(used, total);
        if ("부가통화".equals(label)) return normalizeAddonVoiceUsage(used);
        if ("문자".equals(label)) return normalizeSmsUsage(used, total);
        return used == null ? "--" : compactUnit(used);
    }

    public static String dataRemainingOrUsed(String used, String total) {
        double u = numericToBase(used, true);
        double t = numericToBase(total, true);
        if (u < 0) return "--";
        if (t < 0 || t < u) return formatData(u);
        double rem = t - u;
        if (rem >= 0) return formatData(rem);
        return formatData(u);
    }

    public static String topLabel(String used, String total, boolean data) {
        if (!data) return "사용";
        return "잔여";
    }

    public static String caption(String label, String used, String total) {
        if (label.equals("데이터")) {
            if (total == null || total.equals("--")) return "총량 확인 필요";
            return total + " 기준";
        }
        if (containsAny(total, "기본제공", "무제한", "unlimited")) {
            return "기본제공";
        }
        if (label.equals("음성") || label.equals("문자")) {
            return "기본제공";
        }
        if (label.equals("부가통화")) {
            // v7.6: this plan provides 300 supplementary-call minutes. Keep the caption
            // deterministic even when the EyesMobile page omits the allowance from the DOM.
            return "300분 제공";
        }
        if (used != null && !used.equals("--") && total != null && !total.equals("--")) {
            return used + " / " + total;
        }
        if (total == null || total.equals("--")) return label + " 확인 필요";
        return total;
    }

    private static String normalizeVoiceUsage(String used, String total) {
        if (used != null && !used.equals("--")) {
            String v = used.replaceAll("\\s+", "");
            double seconds = voiceSeconds(v);
            if (seconds >= 0) return formatVoiceDuration(seconds);

            double num = numericToBase(v, false);
            if (num >= 0) return trim(num) + "분";
            return v;
        }
        return containsAny(total, "기본제공", "무제한") ? "0초" : "--";
    }

    private static String normalizeAddonVoiceUsage(String used) {
        if (used == null || used.trim().isEmpty() || used.equals("--") || used.equals("-")
                || used.equals("0") || used.equals("0분")) return "0초";
        return normalizeVoiceUsage(used, "300분");
    }

    /**
     * Supplementary-call allowance is fixed at 300 minutes for this plan.
     * Derive the gauge percentage from the displayed duration so a real 0-second
     * usage can never appear as fully consumed because of a stale parser percent.
     */

    /**
     * Draws the refresh icon as a bitmap so AppWidget hosts cannot replace its glyph/font.
     * The caller may advance {@code angleDegrees} to create a short manual-refresh spinner.
     */
    public static Bitmap renderRefreshIcon(int color, float angleDegrees) {
        int size = 128;
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.rotate(angleDegrees, size / 2f, size / 2f);

        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(color);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeJoin(Paint.Join.ROUND);
        // Keep the same circular-arrow silhouette, but use a lighter visual weight.
        p.setStrokeWidth(7f);

        RectF oval = new RectF(28f, 28f, 100f, 100f);
        float start = -58f;
        float sweep = 292f;
        c.drawArc(oval, start, sweep, false, p);

        double endRad = Math.toRadians(start + sweep);
        float cx = oval.centerX();
        float cy = oval.centerY();
        float rx = oval.width() / 2f;
        float ry = oval.height() / 2f;
        float ex = cx + (float) Math.cos(endRad) * rx;
        float ey = cy + (float) Math.sin(endRad) * ry;

        // Tangent vector at the end of the clockwise arc.
        float tx = -(float) Math.sin(endRad);
        float ty = (float) Math.cos(endRad);
        float nx = -ty;
        float ny = tx;
        float back = 18f;
        float half = 7f;

        Path arrow = new Path();
        arrow.moveTo(ex, ey);
        arrow.lineTo(ex - tx * back + nx * half, ey - ty * back + ny * half);
        arrow.lineTo(ex - tx * back - nx * half, ey - ty * back - ny * half);
        arrow.close();
        p.setStyle(Paint.Style.FILL);
        c.drawPath(arrow, p);
        return bmp;
    }

    public static int addonUsedPercent(String used, int fallbackPercent) {
        double seconds = voiceSeconds(used);
        if (seconds >= 0d) {
            return Math.max(0, Math.min(100, (int) Math.round(seconds * 100d / (300d * 60d))));
        }
        if (used == null || used.trim().isEmpty() || used.equals("--") || used.equals("-")
                || used.equals("0") || used.equals("0분") || used.equals("0초")) {
            return 0;
        }
        return Math.max(0, Math.min(100, fallbackPercent));
    }

    /** Parses all duration components so "0분35초" does not collapse to "0분". */
    private static double voiceSeconds(String value) {
        if (value == null) return -1;
        String v = value.replace(",", "").trim().toUpperCase(Locale.ROOT);
        Matcher clock = Pattern.compile("(?<![0-9])([0-9]{1,3}):([0-9]{2})(?::([0-9]{2}))?(?![0-9])").matcher(v);
        if (clock.find()) {
            try {
                double a = Double.parseDouble(clock.group(1));
                double b = Double.parseDouble(clock.group(2));
                String third = clock.group(3);
                return third == null ? a * 60d + b : a * 3600d + b * 60d + Double.parseDouble(third);
            } catch (Exception ignored) {}
        }
        Matcher m = Pattern.compile(
                "([0-9]+(?:\\.[0-9]+)?)(시간|분|초|MINUTE|MIN)",
                Pattern.CASE_INSENSITIVE).matcher(v);
        double seconds = 0d;
        boolean found = false;
        while (m.find()) {
            double n;
            try { n = Double.parseDouble(m.group(1)); } catch (Exception e) { continue; }
            String unit = m.group(2).toUpperCase(Locale.ROOT);
            if ("시간".equals(unit)) seconds += n * 3600d;
            else if ("초".equals(unit)) seconds += n;
            else seconds += n * 60d;
            found = true;
        }
        return found ? Math.max(0d, seconds) : -1;
    }

    private static String formatVoiceDuration(double rawSeconds) {
        long seconds = Math.max(0L, Math.round(rawSeconds));
        if (seconds == 0L) return "0초";

        long hours = seconds / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long remainSeconds = seconds % 60L;

        if (hours > 0L) {
            StringBuilder out = new StringBuilder();
            out.append(hours).append("시간");
            if (minutes > 0L) out.append(minutes).append("분");
            if (remainSeconds > 0L) out.append(remainSeconds).append("초");
            return out.toString();
        }
        if (minutes > 0L) {
            return remainSeconds > 0L
                    ? minutes + "분" + remainSeconds + "초"
                    : minutes + "분";
        }
        return remainSeconds + "초";
    }

    private static String normalizeSmsUsage(String used, String total) {
        if (used != null && !used.equals("--")) {
            double num = numericToBase(used, false);
            if (num >= 0) return trim(num) + "건";
            return compactUnit(used);
        }
        return containsAny(total, "기본제공", "무제한") ? "0건" : "--";
    }

    private static String compactUnit(String s) {
        return s == null ? "--" : s.replaceAll("\\s+", "");
    }

    private static String formatData(double mb) {
        if (mb >= 1024d) {
            double gb = mb / 1024d;
            if (Math.abs(gb - Math.rint(gb)) < 0.005) return String.format(Locale.KOREA, "%.0fGB", gb);
            return String.format(Locale.KOREA, "%.2fGB", gb).replaceAll("0+$", "").replaceAll("\\.$", "") + "";
        }
        return trim(mb) + "MB";
    }

    private static String trim(double n) {
        if (Math.abs(n - Math.rint(n)) < 0.005) return String.format(Locale.KOREA, "%.0f", n);
        return String.format(Locale.KOREA, "%.1f", n).replaceAll("\\.0$", "");
    }

    private static double numericToBase(String value, boolean data) {
        if (value == null) return -1;
        String v = value.replace(",", "").replace(" ", "").toUpperCase(Locale.ROOT);
        Matcher m = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)(GB|MB|KB|G|M|분|초|MINUTE|MIN|건|회|개)?").matcher(v);
        if (!m.find()) return -1;
        double n;
        try { n = Double.parseDouble(m.group(1)); } catch (Exception e) { return -1; }
        String unit = m.group(2) == null ? "" : m.group(2);
        if (data) {
            if (unit.equals("GB") || unit.equals("G")) n *= 1024d;
            else if (unit.equals("KB")) n /= 1024d;
        }
        return n;
    }

    private static double numericFromText(String value) {
        double n = numericToBase(value, false);
        return Math.max(-1, n);
    }

    private static boolean containsAny(String text, String... values) {
        if (text == null) return false;
        String lower = text.toLowerCase(Locale.ROOT);
        for (String v : values) if (lower.contains(v.toLowerCase(Locale.ROOT))) return true;
        return false;
    }
}
