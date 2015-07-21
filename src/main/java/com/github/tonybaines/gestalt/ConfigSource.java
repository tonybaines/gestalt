package com.github.tonybaines.gestalt;

import java.lang.reflect.Method;
import java.util.List;

public interface ConfigSource {

  Object lookup(List<String> path, Method method);

}