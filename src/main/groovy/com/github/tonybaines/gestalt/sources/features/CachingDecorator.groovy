package com.github.tonybaines.gestalt.sources.features

import com.github.tonybaines.gestalt.ConfigSource
import com.github.tonybaines.gestalt.NoCache
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.util.concurrent.UncheckedExecutionException

import java.lang.reflect.Method
import java.util.concurrent.Callable

class CachingDecorator<T> implements ConfigSource {
  private final ConfigSource delegate
  private final Cache<String, Object> cache

  CachingDecorator(ConfigSource delegate) {
    this.delegate = delegate
    cache = CacheBuilder.newBuilder().build()
  }

  @Override
  def lookup(List<String> path, Method method) {
    try {
      if (method.declaringClass.isAnnotationPresent(NoCache) || method.isAnnotationPresent(NoCache)) {
        return delegate.lookup(path, method)
      } else {
        return cache.get(keyFrom(path), new Callable<Object>() {
          Object call() throws Exception {
            delegate.lookup(path, method)
          }
        })
      }
    }
    catch (CacheLoader.InvalidCacheLoadException e) {
      if (e.message.contains('CacheLoader returned null for key')) return null
      else throw e
    }
    catch (UncheckedExecutionException e) {
      throw e.cause
    }
  }

  static String keyFrom(List<String> strings) { strings.join('.') }
}
