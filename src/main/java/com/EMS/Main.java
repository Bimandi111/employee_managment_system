package com.EMS;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {

        String webappDir = new File("src/main/webapp").getAbsolutePath();
        log.info("Webapp dir: {}", webappDir);

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);

        Connector connector = new Connector();
        connector.setPort(PORT);
        connector.setURIEncoding("UTF-8");
        tomcat.setConnector(connector);

        Path workDir = Files.createTempDirectory("tomcat-work-");
        tomcat.setBaseDir(workDir.toAbsolutePath().toString());

        Context ctx = tomcat.addWebapp("", new File(webappDir).getAbsolutePath());

        WebResourceRoot resources = new StandardRoot(ctx);

        File classesDir = new File("target/classes");
        if (classesDir.exists()) {
            resources.addPreResources(new DirResourceSet(
                    resources, "/WEB-INF/classes", classesDir.getAbsolutePath(), "/"));
        }

        File libDir = new File("target/dependency");
        if (libDir.exists()) {
            resources.addPreResources(new DirResourceSet(
                    resources, "/WEB-INF/lib", libDir.getAbsolutePath(), "/"));
        }

        ctx.setResources(resources);

        try {
            tomcat.start();
        } catch (LifecycleException e) {
            System.out.println("Failed to start Tomcat" + e);
            System.exit(1);
        }

        System.out.println("App started: http://localhost:" + PORT);

        tomcat.getServer().await();
    }
}