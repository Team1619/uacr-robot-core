package org.uacr.models.state;

import org.uacr.robot.AbstractModelFactory;
import org.uacr.shared.abstractions.EventBus;
import org.uacr.utilities.Config;
import org.uacr.utilities.YamlConfigParser;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PositionEventState implements State {

    private final AbstractModelFactory fModelFactory;
    private final EventBus fEventBus;
    private final String fStateName;

    @Nullable
    private final State fPrimaryState;
    private final List<State> fForegroundEventStates;
    private final List<State> fBackgroundEventStates;

    public PositionEventState(AbstractModelFactory modelFactory, String name, YamlConfigParser parser, Config config, EventBus eventBus) {
        fModelFactory = modelFactory;
        fEventBus = eventBus;
        fStateName = name;

        fPrimaryState = null;

        fForegroundEventStates = new ArrayList<>();
        fBackgroundEventStates = new ArrayList<>();
    }

    @Override
    public String getName() {
        return fStateName;
    }

    @Override
    public Set<String> getSubsystems() {
        return null;
    }

    @Override
    public Set<State> getSubStates() {
        return null;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void update() {

    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public void dispose() {

    }
}
