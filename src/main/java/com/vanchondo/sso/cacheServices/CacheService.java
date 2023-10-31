package com.vanchondo.sso.cacheServices;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.HashMap;
import java.util.Map;

public abstract class CacheService {
  protected final CacheManager cacheManager;
  private Map<String, Cache> cacheMap = new HashMap<>();

  protected CacheService(CacheManager cacheManager) {
    this.cacheManager = cacheManager;
  }

  protected Cache getServiceCache(String cacheName) {
    if (cacheManager == null) {
      return null;
    }
    if (!cacheMap.containsKey(cacheName)) {
      cacheMap.put(cacheName, cacheManager.getCache(cacheName));
    }
    return cacheMap.get(cacheName);
  }
}
