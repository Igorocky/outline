package org.igye.outline2.tools;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexReplace {
    public static void main(String[] args) throws IOException {
        new RegexReplace().run("roboto-300-400-500.css");
        new RegexReplace().run("mui-icons.css");
    }

    public void run(String cssFileName) throws IOException {
        Map<String, String> urlMap = new HashMap<>();
        String localVersionDirName = "local-version";
        String cssDir = "./src/main/webapp/css/";
        String localVersionDir = cssDir + localVersionDirName + "/";
        replace(
                cssDir + cssFileName,
                Pattern.compile("url\\(([^\\(\\)]+)\\)"),
                matcher -> {
                    System.out.println(matcher.group(0));
                    String oldUrl = matcher.group(1);
                    if (!urlMap.containsKey(oldUrl)) {
                        String fileName = UUID.randomUUID().toString();
                        urlMap.put(oldUrl, "/css/" + localVersionDirName + "/" + fileName);
                        copyUrlToFile(oldUrl, new File(localVersionDir + fileName));
                    }
                    return "url(" + urlMap.get(oldUrl) + ")";
                },
                localVersionDir + cssFileName
        );
    }

    public void copyUrlToFile(String url, File destination) {
        try {
            FileUtils.copyURLToFile(new URL(url), destination);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void replace(String srcFile, Pattern pattern, Function<Matcher, String> replacement, String dstFile) throws IOException {
        String content = FileUtils.readFileToString(new File(srcFile), StandardCharsets.UTF_8);
        Matcher matcher = pattern.matcher(content);
        StringBuilder newContent = new StringBuilder();
        int prevEnd = 0;
        while (matcher.find()) {
            newContent.append(content, prevEnd, matcher.start());
            newContent.append(replacement.apply(matcher));
            prevEnd = matcher.end();
        }
        newContent.append(content, prevEnd, content.length());
        File dstFileF = new File(dstFile);
        dstFileF.getParentFile().mkdirs();
        FileUtils.writeStringToFile(dstFileF, newContent.toString(), StandardCharsets.UTF_8);
    }
}
