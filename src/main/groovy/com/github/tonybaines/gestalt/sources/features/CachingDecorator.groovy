package com.github.tonybaines.gestalt.sources.features

import com.github.tonybaines.gestalt.ConfigSource
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.util.concurrent.UncheckedExecutionException

import java.lang.reflect.Method
import java.util.concurrent.Callable

class CachingDecorator implements ConfigSource {
  private final ConfigSource delegate
  private final Cache<String, Object> cache

  CachingDecorator(ConfigSource delegate) {
    this.delegate = delegate
    cache = new CacheBuilder<String, Object>().build()
  }

  @Override
  def lookup(List<String> path, Method method) {
    try {
      return cache.get(keyFrom(path), new Callable<Object>() {
        Object call() throws Exception {
          delegate.lookup(path, method)
        }
      })
    }
    catch (CacheLoader.InvalidCacheLoadException e) {
      if (e.message.contains('CacheLoader returned null for key')) return null
      else throw e
    }
    catch (UncheckedExecutionException e) {
      throw e.cause
    }
  }

  String keyFrom(List<String> strings) { strings.join('.') }
}
