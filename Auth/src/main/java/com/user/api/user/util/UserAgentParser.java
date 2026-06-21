package com.user.api.user.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parseo simple de User-Agent a un texto legible ("Chrome 149 en Windows").
 * No pretende ser exhaustivo (para eso existen librerías como YAUAA) — alcanza
 * para mostrar algo entendible en el correo de alerta de inicio de sesión.
 */
public final class UserAgentParser {

    private UserAgentParser() {
    }

    private static final Pattern EDGE = Pattern.compile("Edg(?:A|iOS)?/(\\d+)");
    private static final Pattern OPERA = Pattern.compile("OPR/(\\d+)");
    private static final Pattern CHROME = Pattern.compile("Chrome/(\\d+)");
    private static final Pattern FIREFOX = Pattern.compile("Firefox/(\\d+)");
    private static final Pattern SAFARI_VERSION = Pattern.compile("Version/(\\d+).*Safari");

    public static String describe(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return "Dispositivo desconocido";
        }

        String browser = parseBrowser(userAgent);
        String os = parseOs(userAgent);

        if (os == null) return browser;
        return browser + " en " + os;
    }

    private static String parseBrowser(String ua) {
        Matcher m = EDGE.matcher(ua);
        if (m.find()) return "Microsoft Edge " + m.group(1);

        m = OPERA.matcher(ua);
        if (m.find()) return "Opera " + m.group(1);

        m = CHROME.matcher(ua);
        if (m.find()) return "Chrome " + m.group(1);

        m = FIREFOX.matcher(ua);
        if (m.find()) return "Firefox " + m.group(1);

        if (ua.contains("Safari") && !ua.contains("Chrome")) {
            m = SAFARI_VERSION.matcher(ua);
            if (m.find()) return "Safari " + m.group(1);
            return "Safari";
        }

        return "Navegador desconocido";
    }

    private static String parseOs(String ua) {
        if (ua.contains("Windows NT 10.0")) return "Windows 10/11";
        if (ua.contains("Windows NT")) return "Windows";
        if (ua.contains("Android")) {
            Matcher m = Pattern.compile("Android (\\d+)").matcher(ua);
            return m.find() ? "Android " + m.group(1) : "Android";
        }
        if (ua.contains("iPhone") || ua.contains("iPad")) {
            Matcher m = Pattern.compile("OS (\\d+)[_.](\\d+)").matcher(ua);
            return m.find() ? "iOS " + m.group(1) + "." + m.group(2) : "iOS";
        }
        if (ua.contains("Mac OS X")) {
            Matcher m = Pattern.compile("Mac OS X (\\d+)[_.](\\d+)").matcher(ua);
            return m.find() ? "macOS " + m.group(1) + "." + m.group(2) : "macOS";
        }
        if (ua.contains("Linux")) return "Linux";
        return null;
    }
}
