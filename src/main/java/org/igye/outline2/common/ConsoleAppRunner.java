package org.igye.outline2.common;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleAppRunner implements Closeable {
    private BufferedReader reader;
    private Process proc;

    public ConsoleAppRunner(String commandToExecute) throws IOException {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command(commandToExecute);
        proc = pb.start();
        reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
    }

    public void send(String str) throws IOException {
        logOut(str);
        proc.getOutputStream().write(str.getBytes());
        proc.getOutputStream().write("\n".getBytes());
        proc.getOutputStream().flush();
    }

    public void read(Function<String,Boolean> lineConsumer) throws IOException {
        String line = reader.readLine();
        logIn(line);
        while (line != null) {
            if (!lineConsumer.apply(line)) {
                return;
            }
            line = reader.readLine();
            logIn(line);
        }
    }

    public Matcher readTill(Pattern pattern) throws IOException {
        final Matcher[] matcher = {null};
        read(line -> {
            final Matcher match = pattern.matcher(line);
            if (match.matches()) {
                matcher[0] = match;
                return false;
            } else {
                return true;
            }
        });
        return matcher[0];
    }

    public String readTill(Function<String, Boolean> matcher) throws IOException {
        final String[] result = {null};
        read(line -> {
            if (matcher.apply(line)) {
                result[0] = line;
                return false;
            } else {
                return true;
            }
        });
        return result[0];
    }

    public void destroy() {
        proc.destroy();
    }

    @Override
    public void close() {
        destroy();
    }

    private void logIn(String str) {
//        System.out.println(">>>>> " + str);
    }

    private void logOut(String str) {
//        System.out.println("<<<<< " + str);
    }
}
