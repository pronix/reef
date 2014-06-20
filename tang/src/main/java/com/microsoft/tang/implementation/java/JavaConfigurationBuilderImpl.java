/**
 * Copyright (C) 2014 Microsoft Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.tang.implementation.java;

import com.microsoft.tang.Configuration;
import com.microsoft.tang.ExternalConstructor;
import com.microsoft.tang.JavaClassHierarchy;
import com.microsoft.tang.JavaConfigurationBuilder;
import com.microsoft.tang.annotations.Name;
import com.microsoft.tang.exceptions.BindException;
import com.microsoft.tang.implementation.ConfigurationBuilderImpl;
import com.microsoft.tang.implementation.ConfigurationImpl;
import com.microsoft.tang.types.ClassNode;
import com.microsoft.tang.types.NamedParameterNode;
import com.microsoft.tang.types.Node;
import com.microsoft.tang.util.ReflectionUtilities;

import java.lang.reflect.Type;
import java.net.URL;
import java.util.Set;

public class JavaConfigurationBuilderImpl extends ConfigurationBuilderImpl
    implements JavaConfigurationBuilder {

  public JavaConfigurationBuilderImpl(URL[] jars, Configuration[] confs, Class<? extends ExternalConstructor<?>>[] parsers)
      throws BindException {
    super(jars, confs, parsers);
  }

  JavaConfigurationBuilderImpl() {
    super();
  }

  public JavaConfigurationBuilderImpl(URL[] jars) throws BindException {
    super(jars);
  }

  JavaConfigurationBuilderImpl(JavaConfigurationBuilderImpl impl) {
    super(impl);
  }

  public JavaConfigurationBuilderImpl(Configuration[] confs)
      throws BindException {
    super(confs);
  }

  private class JavaConfigurationImpl extends ConfigurationImpl {
    JavaConfigurationImpl(JavaConfigurationBuilderImpl builder) {
      super(builder);
    }
  }

  @Override
  public ConfigurationImpl build() {
    return new JavaConfigurationImpl(new JavaConfigurationBuilderImpl(this));
  }

  private Node getNode(Class<?> c) {
    return ((JavaClassHierarchy) namespace).getNode(c);
  }

  @Override
  public <T> JavaConfigurationBuilder bind(Class<T> c, Class<?> val) throws BindException {
    super.bind(getNode(c), getNode(val));
    return this;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> JavaConfigurationBuilder bindImplementation(Class<T> c, Class<? extends T> d)
      throws BindException {
    final Node cn = getNode(c);
    final Node dn = getNode(d);
    if (!(cn instanceof ClassNode)) {
      throw new BindException(
          "bindImplementation passed interface that resolved to " + cn
              + " expected a ClassNode<?>");
    }
    if (!(dn instanceof ClassNode)) {
      throw new BindException(
          "bindImplementation passed implementation that resolved to " + dn
              + " expected a ClassNode<?>");
    }
    super.bindImplementation((ClassNode<T>) cn, (ClassNode<? extends T>) dn);
    return this;
  }

  @Override
  public JavaConfigurationBuilder bindNamedParameter(final Class<? extends Name<?>> name, final String value)
      throws BindException {
    if (value == null) {
      throw new IllegalStateException("The value null set to the named parameter is illegal: " + name);
    }
    final Node np = getNode(name);
    if (np instanceof NamedParameterNode) {
      super.bindParameter((NamedParameterNode<?>) np, value);
      return this;
    } else {
      throw new BindException(
          "Detected type mismatch when setting named parameter " + name
              + "  Expected NamedParameterNode, but namespace contains a " + np);
    }
  }

  @Override
  public <T> JavaConfigurationBuilder bindNamedParameter(Class<? extends Name<T>> iface,
                                                         Class<? extends T> impl) throws BindException {
    Node ifaceN = getNode(iface);
    Node implN = getNode(impl);
    if (!(ifaceN instanceof NamedParameterNode)) {
      throw new BindException("Type mismatch when setting named parameter " + ifaceN
          + " Expected NamedParameterNode");
    }
    bind(ifaceN, implN);
    return this;
  }

  @SuppressWarnings({"unchecked"})
  public <T> JavaConfigurationBuilder bindConstructor(Class<T> c,
                                                      Class<? extends ExternalConstructor<? extends T>> v) throws BindException {
    final Node n = getNode(c);
    final Node m = getNode(v);
    if (!(n instanceof ClassNode)) {
      throw new BindException("BindConstructor got class that resolved to " + n + "; expected ClassNode");
    }
    if (!(m instanceof ClassNode)) {
      throw new BindException("BindConstructor got class that resolved to " + m + "; expected ClassNode");
    }
    super.bindConstructor((ClassNode<T>) n, (ClassNode<? extends ExternalConstructor<? extends T>>) m);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> JavaConfigurationBuilder bindSetEntry(Class<? extends Name<Set<T>>> iface, String value) throws BindException {
    final Node n = getNode(iface);

    if (!(n instanceof NamedParameterNode)) {
      throw new BindException("BindSetEntry got an interface that resolved to " + n + "; expected a NamedParameter");
    }
    final Type setType = ReflectionUtilities.getInterfaceTarget(Name.class, iface);
    if (!ReflectionUtilities.getRawClass(setType).equals(Set.class)) {
      throw new BindException("BindSetEntry got a NamedParameter that takes a " + setType + "; expected Set<...>");
    }
//    Type valType = ReflectionUtilities.getInterfaceTarget(Set.class, setType);
    super.bindSetEntry((NamedParameterNode<Set<T>>) n, value);
    return this;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> JavaConfigurationBuilder bindSetEntry(Class<? extends Name<Set<T>>> iface, Class<? extends T> impl) throws BindException {
    final Node n = getNode(iface);
    final Node m = getNode(impl);

    if (!(n instanceof NamedParameterNode)) {
      throw new BindException("BindSetEntry got an interface that resolved to " + n + "; expected a NamedParameter");
    }
    final Type setType = ReflectionUtilities.getInterfaceTarget(Name.class, iface);
    if (!ReflectionUtilities.getRawClass(setType).equals(Set.class)) {
      throw new BindException("BindSetEntry got a NamedParameter that takes a " + setType + "; expected Set<...>");
    }
    final Type valType = ReflectionUtilities.getInterfaceTarget(Set.class, setType);
    if (!ReflectionUtilities.getRawClass(valType).isAssignableFrom(impl)) {
      throw new BindException("BindSetEntry got implementation " + impl + " that is incompatible with expected type " + valType);
    }

    super.bindSetEntry((NamedParameterNode<Set<T>>) n, m);
    return this;
  }
}
