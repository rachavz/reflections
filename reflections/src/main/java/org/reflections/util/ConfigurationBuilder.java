package org.reflections.util;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.reflections.Configuration;
import org.reflections.adapters.*;
import org.reflections.serializers.Serializer;
import org.reflections.serializers.XmlSerializer;
import org.reflections.scanners.*;

import java.net.URL;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * a fluent builder for {@link org.reflections.Configuration}, to be used for constructing a {@link org.reflections.Reflections} instance
 * <p>usage:
 * <pre>
 *      new Reflections(
 *          new ConfigurationBuilder()
 *              .filterInputsBy(new FilterBuilder().include("your project's common package prefix here..."))
 *              .setUrls(ClasspathHelper.getUrlsForCurrentClasspath())
 *              .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner().filterResultsBy(myClassAnnotationsFilter)));
 * </pre>
 * <p>default constructor sets reasonable defaults, such as SingleThreadExecutor for scanning, accept all for {@link #inputsFilter}
 * , scanners set to {@link org.reflections.scanners.SubTypesScanner}, {@link org.reflections.scanners.TypeAnnotationsScanner},
 * using {@link org.reflections.serializers.XmlSerializer}
 */
@SuppressWarnings({"RawUseOfParameterizedType"})
public class ConfigurationBuilder implements Configuration {
    private Set<Scanner> scanners;
    private Set<URL> urls;
    private MetadataAdapter metadataAdapter;
    private Predicate<String> inputsFilter;
    private Serializer serializer;
    private Supplier<ExecutorService> executorServiceSupplier;

    public ConfigurationBuilder() {
        //defaults
        scanners = Sets.<Scanner>newHashSet(new SubTypesScanner(), new TypeAnnotationsScanner());
        metadataAdapter = new JavassistAdapter();
        inputsFilter = Predicates.alwaysTrue();
        serializer = new XmlSerializer();
        executorServiceSupplier = new Supplier<ExecutorService>() {
            public ExecutorService get() {
                return Executors.newSingleThreadExecutor();
            }
        };
    }

    public Set<Scanner> getScanners() {
		return scanners;
	}

    /** set the scanners instances for scanning different metadata */
    public ConfigurationBuilder setScanners(final Scanner... scanners) {
        this.scanners = ImmutableSet.of(scanners);
        return this;
    }

    public Set<URL> getUrls() {
        return urls;
    }

    /** set the urls to be scanned
     * <p>use {@link org.reflections.util.ClasspathHelper} convenient methods to get the relevant urls
     * */
    public ConfigurationBuilder setUrls(final Collection<URL> urls) {
		this.urls = ImmutableSet.copyOf(urls);
        return this;
	}

    /** set the urls to be scanned
     * <p>use {@link org.reflections.util.ClasspathHelper} convenient methods to get the relevant urls
     * */
    public ConfigurationBuilder setUrls(final URL... urls) {
		this.urls = ImmutableSet.of(urls);
        return this;
	}

    public MetadataAdapter getMetadataAdapter() {
        return metadataAdapter;
    }

    /** sets the metadata adapter used to fetch metadata from classes */
    public ConfigurationBuilder setMetadataAdapter(final MetadataAdapter metadataAdapter) {
        this.metadataAdapter = metadataAdapter;
        return this;
    }

    public boolean acceptsInput(String inputFqn) {
        return inputsFilter.apply(inputFqn);
    }

    /** sets the input filter for all resources to be scanned
     * <p> supply a {@link com.google.common.base.Predicate} or use the {@link FilterBuilder}*/
    public ConfigurationBuilder filterInputsBy(Predicate<String> inputsFilter) {
        this.inputsFilter = inputsFilter;
        return this;
    }

    public Supplier<ExecutorService> getExecutorServiceSupplier() {
        return executorServiceSupplier;
    }

    /** sets the executor service used for scanning.
     * <p>default is ThreadPoolExecutor with a single core */
    public ConfigurationBuilder setExecutorServiceSupplier(Supplier<ExecutorService> executorServiceSupplier) {
        this.executorServiceSupplier = executorServiceSupplier;
        return this;
    }

    /** sets the executor service used for scanning to ThreadPoolExecutor with core size as {@link java.lang.Runtime#getRuntime#availableProcessors()}
     * <p>default is ThreadPoolExecutor with a single core */
    public ConfigurationBuilder useParallelExecutor() {
        return useParallelExecutor(Runtime.getRuntime().availableProcessors());
    }

    /** sets the executor service used for scanning to ThreadPoolExecutor with core size as the given availableProcessors parameter
     * <p>default is ThreadPoolExecutor with a single core */
    public ConfigurationBuilder useParallelExecutor(final int availableProcessors) {
        setExecutorServiceSupplier(new Supplier<ExecutorService>() { public ExecutorService get() {
                return Executors.newFixedThreadPool(availableProcessors);
            }
        });
        return this;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    /** sets the serializer used when issuing {@link org.reflections.Reflections#save} */
    public ConfigurationBuilder setSerializer(Serializer serializer) {
        this.serializer = serializer;
        return this;
    }
}