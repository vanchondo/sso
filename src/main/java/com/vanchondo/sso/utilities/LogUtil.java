package com.vanchondo.sso.utilities;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public abstract class LogUtil {
  private LogUtil() {}

  public static String getMethodName(Object ref){
    return String.format("::%s::", ref.getClass().getEnclosingMethod().getName());
  }

  public static String getArtifactVersion() {
    try {
      ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
      Resource[] resources = resolver.getResources("classpath:META-INF/MANIFEST.MF");

      for (Resource resource : resources) {
        try {
          Manifest manifest = new Manifest(resource.getInputStream());
          Attributes attributes = manifest.getMainAttributes();
          String version = attributes.getValue("Implementation-Version");

          if (version != null) {
            return version;
          }
        } catch (IOException e) {
          // Handle IOException
          e.printStackTrace();
        }
      }
    } catch (IOException e) {
      // Handle IOException
      e.printStackTrace();
    }

    return "Unknown";
  }

}
