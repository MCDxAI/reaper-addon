package me.ghosttypes.reaper.util.os;

import meteordevelopment.meteorclient.utils.network.Http;

import java.io.*;
import java.util.ArrayList;

public class FileHelper {

    public static String getFormattedPath(File f) {
        return "\"" + f.getPath() + "\"";
    }

    public static Process start(File f) {
        try {
            String[] cmd = new String[]{"java", "-jar", getFormattedPath(f)};
            ProcessBuilder b = new ProcessBuilder(cmd);
            return b.start();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<String> downloadList(String url) {
        if (url == null) return null;
        try {
            ArrayList<String> list = new ArrayList<>();
            InputStream is = Http.get(url).sendInputStream();
            BufferedReader r = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = r.readLine()) != null) list.add(line);
            r.close();
            is.close();
            return list;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void downloadFile(String url, File outFile) {
        if (url == null || outFile == null) return;
        try {
            if (!outFile.exists()) outFile.createNewFile();
            BufferedInputStream bs = new BufferedInputStream(Http.get(url).sendInputStream());
            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = bs.read(buffer, 0, 1024)) != -1) fos.write(buffer, 0, bytesRead);
            bs.close();
            fos.close();
        } catch (Exception e) {
            // Silent fail - network operations
        }
    }
}
