package com.example.laundry2;

import androidx.annotation.NonNull;
import androidx.lifecycle.*;

public final class TestLifeCycle implements LifecycleOwner {
    private final LifecycleRegistry registry = new LifecycleRegistry(this);

    public TestLifeCycle () {
    }

    public TestLifeCycle create() {
        return handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
    }

    public TestLifeCycle start() {
        return handleLifecycleEvent(Lifecycle.Event.ON_START);
    }

    public TestLifeCycle resume() {
        return handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
    }

    public TestLifeCycle pause() {
        return handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
    }

    public TestLifeCycle stop() {
        return handleLifecycleEvent(Lifecycle.Event.ON_STOP);
    }

    public TestLifeCycle destroy() {
        return handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
    }

    @NonNull
    public Lifecycle.State getCurrentState() {
        return registry.getCurrentState();
    }

    private TestLifeCycle handleLifecycleEvent(@NonNull Lifecycle.Event event) {
        registry.handleLifecycleEvent(event);
        return this;
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return registry;
    }

    public static TestLifeCycle initialized() {
        return new TestLifeCycle ();
    }

    public static TestLifeCycle resumed() {
        return initialized().resume();
    }
}
