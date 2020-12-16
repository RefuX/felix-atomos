/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.felix.atomos.utils.substrate.impl;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NativeImageArgumentsImpl implements DefaultNativeImageArguments
{

    private static List<String> addArgIfExitsPath(final List<String> arguments,
        final String parameterName, final List<Path> values)
    {
        if (values != null && !values.isEmpty())
        {
            arguments.add(combineArgPath(parameterName, values.stream()));
        }
        return arguments;
    }

    private static String combineArg(final String parameterName,
        final Stream<String> values)
    {
        return combineArg(parameterName,
            values.filter(Objects::nonNull).sorted().collect(Collectors.joining(",")));
    }

    private static String combineArg(final String parameterName, final String value)
    {
        return parameterName + "=" + value;
    }

    private static String combineArgPath(final String parameterName,
        final Stream<Path> values)
    {
        return combineArg(parameterName, values.filter(Objects::nonNull)//
            .sorted(NativeImageArgumentsImpl::byAbsolutePath)//
            .map(Path::toAbsolutePath).map(Path::toString));
    }

    private static int byAbsolutePath(Path p1, Path p2)
    {
        return p1.toAbsolutePath().toString().compareTo(p2.toAbsolutePath().toString());
    }

    boolean allowIncompleteClasspath = false;
    final List<Path> classPathFiles = new ArrayList<>();
    boolean debugAttach = false;
    final List<Path> dynamicProxyConfigurationFiles = new ArrayList<>();
    String imageName;
    final List<String> initializeAtBuildTime = new ArrayList<>();
    String mainClass;
    boolean noFallback = true;
    final List<Path> reflectionConfigurationFiles = new ArrayList<>();
    boolean reportExceptionStackTraces = false;
    boolean reportUnsupportedElementsAtRuntime = false;
    final List<Path> resourceConfigurationFiles = new ArrayList<>();
    boolean traceClassInitialization = false;
    boolean verbose = true;
    final List<String> vmFlags = new ArrayList<>();
    final Map<String, String> vmSystemProperties = new HashMap<>();
    boolean printClassInitialization = false;

    NativeImageArgumentsImpl()
    {
    }

    private List<String> addArgIfExits(final List<String> arguments,
        final String parameterName, final List<String> values)
    {
        if (values != null && !values.isEmpty())
        {
            arguments.add(combineArg(parameterName, values.stream()));
        }
        return arguments;
    }

    private List<String> addArgIfTrue(final List<String> arguments,
        final String parameterName, final boolean value)
    {
        if (value)
        {
            arguments.add(parameterName);
        }
        return arguments;
    }

    @Override
    public boolean allowIncompleteClasspath()
    {

        return allowIncompleteClasspath;
    }

    @Override
    public List<String> arguments()
    {
        final List<String> arguments = new ArrayList<>();
        final List<String> otherArguments = new ArrayList<>();
        //-cp
        arguments.add(NI_PARAM_CP);
        String cp = classPathFiles().stream()//
            .filter(Objects::nonNull)//
            .map(Path::toAbsolutePath)//
            .sorted(NativeImageArgumentsImpl::byAbsolutePath)//
            .map(Path::toString)//
            .collect(Collectors.joining(":"));
        arguments.add(cp);

        //--verbose
        addArgIfTrue(otherArguments, NI_PARAM_VERBOSE, verbose);
        //initialize-at-build-time
        addArgIfExits(otherArguments, NI_PARAM_INITIALIZE_AT_BUILD_TIME,
            initializeAtBuildTime());
        //H:ReflectionConfigurationFiles
        addArgIfExitsPath(otherArguments, NI_PARAM_H_REFLECTION_CONFIGURATION_FILES,
            reflectionConfigurationFiles());
        //H:ResourceConfigurationFiles
        addArgIfExitsPath(otherArguments, NI_PARAM_H_RESOURCE_CONFIGURATION_FILES,
            resourceConfigurationFiles());
        //H:DynamicProxyConfigurationFiles
        addArgIfExitsPath(otherArguments, NI_PARAM_H_DYNAMIC_PROXY_CONFIGURATION_FILES,
            dynamicProxyConfigurationFiles());
        //--allow-incomplete-classpath
        addArgIfTrue(otherArguments, NI_PARAM_ALLOW_INCOMPLETE_CLASSPATH,
            allowIncompleteClasspath());
        //-H:+ReportUnsupportedElementsAtRuntime
        addArgIfTrue(otherArguments, NI_PARAM_H_REPORT_UNSUPPORTED_ELEMENTS_AT_RUNTIME,
            reportUnsupportedElementsAtRuntime());
        //-H:+ReportExceptionStackTraces
        addArgIfTrue(otherArguments, NI_PARAM_H_REPORT_EXCEPTION_STACK_TRACES,
            reportExceptionStackTraces());
        //
        addArgIfTrue(otherArguments, NI_PARAM_H_PRINT_CLASS_INITIALIZATION,
            traceClassInitialization());
        //--no-fallback
        addArgIfTrue(otherArguments, NI_PARAM_NO_FALLBACK, noFallback());
        //--debug-attach
        addArgIfTrue(otherArguments, NI_PARAM_DEBUG_ATTACH, debugAttach());
        //-H:+PrintClassInitialization
        addArgIfTrue(otherArguments, NI_PARAM_PRINT_CLASS_INITIALIZATION,
            printClassInitialization());

        //-H:Class
        otherArguments.add(combineArg(NI_PARAM_H_CLASS, mainClass()));
        //-H:Name"
        otherArguments.add(combineArg(NI_PARAM_H_NAME, name()));
        //-D<name>=<value> sets a system property for the JVM running the image generator
        vmSystemProperties().forEach((k, v) -> {
            otherArguments.add(combineArg("-D" + k, v));
        });
        vmFlags().forEach(flag -> {
            otherArguments.add("-J" + flag);
        });
        final List<String> additionalArguments = additionalArguments();
        if (additionalArguments != null && !additionalArguments.isEmpty())
        {
            otherArguments.addAll(additionalArguments());
        }

        otherArguments.sort((o1, o2) -> o1.compareTo(o2));
        arguments.addAll(otherArguments);
        return arguments;
    }

    public boolean printClassInitialization()
    {
        return printClassInitialization;
    }

    @Override
    public List<Path> classPathFiles()
    {
        return classPathFiles;
    }

    @Override
    public boolean debugAttach()
    {

        return debugAttach;
    }

    @Override
    public List<Path> dynamicProxyConfigurationFiles()
    {
        return dynamicProxyConfigurationFiles;
    }

    @Override
    public List<String> initializeAtBuildTime()
    {
        return initializeAtBuildTime;
    }

    @Override
    public String mainClass()
    {
        return mainClass;
    }

    @Override
    public String name()
    {
        return imageName;
    }

    @Override
    public boolean noFallback()
    {
        return noFallback;
    }

    @Override
    public List<Path> reflectionConfigurationFiles()
    {
        return reflectionConfigurationFiles;
    }

    @Override
    public boolean reportExceptionStackTraces()
    {
        return reportExceptionStackTraces;
    }

    @Override
    public boolean reportUnsupportedElementsAtRuntime()
    {
        return reportUnsupportedElementsAtRuntime;
    }

    @Override
    public List<Path> resourceConfigurationFiles()
    {
        return resourceConfigurationFiles;
    }

    @Override
    public boolean traceClassInitialization()
    {
        return traceClassInitialization;
    }

    @Override
    public List<String> vmFlags()
    {
        return vmFlags;
    }

    @Override
    public Map<String, String> vmSystemProperties()
    {
        return vmSystemProperties;
    }

    @Override
    public boolean verbose()
    {

        return verbose;
    }
}
