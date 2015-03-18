// $LICENSE
/**
 * Copyright 2013-2014 Spotify AB. All rights reserved.
 *
 * The contents of this file are licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 **/
package com.spotify.ffwd.debug;

import java.util.Collection;

import lombok.extern.slf4j.Slf4j;

import com.google.inject.Inject;
import com.spotify.ffwd.model.Event;
import com.spotify.ffwd.model.Metric;
import com.spotify.ffwd.output.BatchedPluginSink;

import eu.toolchain.async.AsyncFramework;
import eu.toolchain.async.AsyncFuture;

@Slf4j
public class DebugPluginSink implements BatchedPluginSink {
    @Inject
    private AsyncFramework async;

    @Override
    public AsyncFuture<Void> sendEvent(Event event) {
        log.info("E: {}", event);
        return async.resolved(null);
    }

    @Override
    public AsyncFuture<Void> sendMetric(Metric metric) {
        log.info("M: {}", metric);
        return async.resolved(null);
    }

    @Override
    public AsyncFuture<Void> sendEvents(Collection<Event> events) {
        int i = 0;

        for (final Event e : events)
            log.info("E#{}: {}", i++, e);

        return async.resolved(null);
    }

    @Override
    public AsyncFuture<Void> sendMetrics(Collection<Metric> metrics) {
        int i = 0;

        for (final Metric m : metrics)
            log.info("E#{}: {}", i++, m);

        return async.resolved(null);
    }

    @Override
    public AsyncFuture<Void> start() {
        return async.resolved(null);
    }

    @Override
    public AsyncFuture<Void> stop() {
        return async.resolved(null);
    }

    @Override
    public boolean isReady() {
        return true;
    }
}