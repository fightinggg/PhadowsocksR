package com.example.psr;

import com.example.psr.http.HttpProxyLauncher;
import com.example.psr.https.HttpsProxyLauncher;
import com.example.psr.socks5.Socks5ProxyLauncher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

@Slf4j
public class Main {
    public static void main(String[] args) {
        CommandLineParser parser = new GnuParser();

        Options options = new Options();
        options.addOption("p", "port", true, "port");
        options.addOption("", "protocol", true, "protocol");
        options.addOption("", "password", true, "password");

        CommandLine commandLine = null;
        try {
            commandLine = parser.parse(options, args);
        } catch (ParseException e) {
            String info = """
                    could not parse your command line, please use
                    java -jar psr.jar --port 1080 --protocol http
                    java -jar psr.jar --port 1080 --protocol socks5
                    java -jar psr.jar --port 1080 --protocol https --password root:root
                    java -jar psr.jar
                    """;
            log.info(info);
            System.exit(-1);
        }
        int port = Integer.parseInt(commandLine.getOptionValue("port", "1080"));
        String protocol = commandLine.getOptionValue("protocol", "https");

        switch (protocol) {
            case "http":
                HttpProxyLauncher.run(port);
            case "socks5":
                Socks5ProxyLauncher.run(port);
            case "https":
                String password = commandLine.getOptionValue("password", "root:root");
                HttpsProxyLauncher.run(port,password);
        }
    }
}
