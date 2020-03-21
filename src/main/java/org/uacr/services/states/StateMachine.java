package org.uacr.services.states;

import org.uacr.models.state.SingleState;
import org.uacr.models.state.State;
import org.uacr.robot.RobotManager;
import org.uacr.shared.abstractions.InputValues;
import org.uacr.shared.abstractions.ObjectsDirectory;
import org.uacr.shared.abstractions.RobotConfiguration;
import org.uacr.utilities.logging.LogManager;
import org.uacr.utilities.logging.Logger;

import java.util.*;

/**
 * Determines the life cycles of states and the priority of states who want to become active
 */

public class StateMachine {

    private static final Logger sLogger = LogManager.getLogger(StateMachine.class);

    private final ObjectsDirectory fSharedObjectsDirectory;
    private final RobotConfiguration fRobotConfiguration;
    private final InputValues fSharedInputValues;
    private final RobotManager fRobotManager;
    private Set<String> fAllSubsystemNames = new LinkedHashSet<>();
    private Set<String> fAllStateNames = new LinkedHashSet<>();
    private List<String> fPriorityKeys = new ArrayList<>();
    private Map<String, Set<String>> fAllStateNamesWithPriority = new HashMap<>();
    private Set<String> fDoNotInterruptStateNames = new LinkedHashSet<>();
    private Set<String> fDefaultStateNames = new LinkedHashSet<>();
    private Set<State> fPrimaryActiveStates = new LinkedHashSet<>();
    private Set<State> fActiveStates = new LinkedHashSet<>();

    public StateMachine(ObjectsDirectory objectsDirectory, RobotManager robotManager, RobotConfiguration robotConfiguration, InputValues inputValues) {
        fSharedObjectsDirectory = objectsDirectory;
        fRobotManager = robotManager;
        fRobotConfiguration = robotConfiguration;
        fSharedInputValues = inputValues;
    }

    /**
     * Allows the instance of StateControls to be changed out when switching from Auto to Teleop
     */
    public void initialize() {
        fAllSubsystemNames = fRobotConfiguration.getSubsystemNames();
        fAllStateNames = fRobotConfiguration.getStateNames();
        fAllStateNamesWithPriority = fRobotConfiguration.getStateNamesWithPriority();

        for (Map.Entry<String, Set<String>> priority : fAllStateNamesWithPriority.entrySet()) {
            fPriorityKeys.add(priority.getKey());
        }

        if (fPriorityKeys.contains("do_not_interrupt")) {
            fDoNotInterruptStateNames.addAll(fAllStateNamesWithPriority.get("do_not_interrupt"));
        }

        if (fPriorityKeys.contains("default")) {
            fDefaultStateNames.addAll(fAllStateNamesWithPriority.get("default"));
        }

        Collections.sort(fPriorityKeys);

        fPriorityKeys.remove("do_not_interrupt");
        fPriorityKeys.remove("default");

        Collections.reverse(fPriorityKeys);
    }

    /**
     * Called every frame by the state service
     */
    public void update() {

        // Get a list of states that will be active in this frame
        Set<State> nextActiveStates = getNextActiveStates();

        // Dispose of the states that became inactive in this frame
        disposeInactiveStates(nextActiveStates);

        // Initialize states that became active in this frame
        initializeNewlyActiveStates(nextActiveStates);

        fActiveStates = nextActiveStates;

        // Update all active states
        for (State state : fActiveStates) {
            state.update();
        }
    }

    /**
     * Called when switching into disable mode
     * Clears lists of currently active state and calls update to cause them to be disposed
     */
    public void dispose() {

        // Dispose all active states
        for (State state : fActiveStates) {
            state.dispose();
        }

        fActiveStates.clear();
        fPrimaryActiveStates.clear();
        update();
    }

    /**
     * Get all a list of all states grouped by priority level
     * Compile a list of the states that will be active this frame based on order of priority,
     * as states are added they claim their subsystems so states of lower priority can't be active this frame:
     * <p>
     * - Do not interrupt states that are currently running
     * - The highest priority level states that are ready but not active
     * - The highest priority level states that are ready and currently active
     * - Repeat for each priority level descending in priority
     * - Default states
     * - Sub states of primary states active this frame
     * <p>
     * NOTE: Do not interrupt states should be listed as don't interrupt and in a priority
     *
     * @return a list of states that will be active in this frame
     */
    private Set<State> getNextActiveStates() {
        Set<State> nextActiveStates = new LinkedHashSet<>();
        Set<State> primaryNextActiveStates = new LinkedHashSet<>();
        Set<String> subsystems = new LinkedHashSet<>(fAllSubsystemNames);

        // Loop through all the do not interrupt states
        for (String doNotInterruptStateName : fDoNotInterruptStateNames) {

            // Return now if there are no available subsystems
            if (subsystems.isEmpty()) {
                break;
            }

            // Get the state object from the shared objects directory
            State doNotInterruptState = fSharedObjectsDirectory.getStateObject(doNotInterruptStateName);

            // Is this state currently active?
            boolean isCurrentlyActive = fActiveStates.contains(doNotInterruptState);

            // If the state is currently active, then check if it is done
            boolean isDone = true;
            if (fPrimaryActiveStates.contains(doNotInterruptState)) {
                isDone = fRobotManager.isDone(doNotInterruptStateName, doNotInterruptState);
            }

            // If the state is currently active and can not be interrupted, then add it to a list that will be given first priority
            if (isCurrentlyActive && !isDone && isSubsystemAvailable(doNotInterruptState, subsystems)) {
                primaryNextActiveStates.add(doNotInterruptState);
            }
        }

        // Loop through every priority level
        for (String priority : fPriorityKeys) {

            // States that are currently active and ready
            Set<State> currentlyActiveStatesThatAreReady = new LinkedHashSet<>();

            // All the state names in current priority level
            Set<String> stateNamesInPriority = fAllStateNamesWithPriority.get(priority);

            // Loop through each state in priority level
            for (String stateNameInPriority : stateNamesInPriority) {

                // Get the state object from the shared objects directory
                State stateInPriority = fSharedObjectsDirectory.getStateObject(stateNameInPriority);

                // Return now if there are no available subsystems
                if (subsystems.isEmpty()) {
                    break;
                }

                // Is this state ready?
                boolean isReady = fRobotManager.isReady(stateNameInPriority);

                // Is this state currently active?
                boolean isCurrentlyActive = fActiveStates.contains(stateInPriority);

                // If the state is currently active, then check if it is done
                boolean isDone = true;
                if (fPrimaryActiveStates.contains(stateInPriority)) {
                    isDone = fRobotManager.isDone(stateNameInPriority, stateInPriority);
                }

                // Each state takes the subsystem(s) it requires in order of the priority above. Once the subsystem is taken, all lower priority states have to wait until the subsystem(s) it requires becomes available.
                if (!isCurrentlyActive && isReady && isSubsystemAvailable(stateInPriority, subsystems)) {
                    // If primary a state is not active and is ready and its subsystem(s) are available then add it to the set of primary active states
                    primaryNextActiveStates.add(stateInPriority);
                } else if (isCurrentlyActive && !isDone) {
                    // If it is currently active and not done add it to the currently active states that are ready
                    currentlyActiveStatesThatAreReady.add(stateInPriority);
                }
            }

            // For all states that are currently active, not done, and have their subsystem(s) available at this priority level add them to the set of primary active states
            // This needs to be done here so every non active state at this priority level has a chance to interrupt
            for (State activeStateThatIsReady : currentlyActiveStatesThatAreReady) {
                if (isSubsystemAvailable(activeStateThatIsReady, subsystems)) {
                    primaryNextActiveStates.add(activeStateThatIsReady);
                }
            }
        }

        // Loop through every default states specified in yaml
        for (String defaultStateName : fDefaultStateNames) {

            // Return now if there are no available subsystems
            if (subsystems.isEmpty()) {
                break;
            }

            // Get the state from the shared objects directory
            State defaultState = fSharedObjectsDirectory.getStateObject(defaultStateName);

            // If the default state's subsystem(s) are available add it to the set of primary active states
            if (isSubsystemAvailable(defaultState, subsystems)) {
                primaryNextActiveStates.add(defaultState);
            }
        }

        // Add all primaryNextActiveStates to set of states that will be active this frame
        nextActiveStates.addAll(primaryNextActiveStates);

        // Create a new list of subsystems to determine which subsystems are still available since the sequencer state only runs one state at a time
        subsystems = new LinkedHashSet<>(fAllSubsystemNames);

        // For each primary active state add it and its sub states to the set of states that will be active this frame
        // For sequences this may be only a subset of the subsystems used by this sequence
        for (State primaryNextActiveState : primaryNextActiveStates) {
            for (State subState : primaryNextActiveState.getSubStates()) {
                nextActiveStates.add(subState);
                if (subState instanceof SingleState) {
                    isSubsystemAvailable(subState, subsystems);
                }
            }
        }

        // Add any states that are currently active whose subsystem(s) are still available
        for (State state : fActiveStates) {

            // If the state is a single state and its subsystem is available add it to the set of primary active states
            // This is done because we want the state to persist after their primary state has been disposed but
            // not sequences or parallels after their primary state has been disposed
            if (state instanceof SingleState && isSubsystemAvailable(state, subsystems)) {
                nextActiveStates.add(state);
            }
        }

        // Store the currently active primary states for use in the next frame.
        fPrimaryActiveStates = primaryNextActiveStates;

        // Store a list of currently active states in the SharedInputValues so it can be displayed on the web dashboard for debugging
        String activeStatesList = nextActiveStates.toString();

        fSharedInputValues.setString("active states", activeStatesList);

        return nextActiveStates;
    }

    /**
     * Tracks the available subsystems
     * If the subsystem requested by the state is available, removes the subsystem from the list of available subsystems
     *
     * @param state      the state that is requesting a subsystem
     * @param subsystems the list of subsystems to check
     * @return whether the requested subsystem is available
     */
    private boolean isSubsystemAvailable(State state, Set<String> subsystems) {

        // Check if the required subsystems for this state is available
        boolean valid = true;
        for (String subsystemName : state.getSubsystems()) {
            if (!subsystems.contains(subsystemName)) {
                valid = false;
                break;
            }
        }

        // If the subsystems are available then remove them from the list so they can not be used again
        if (valid) {
            for (String subsystemName : state.getSubsystems()) {
                subsystems.remove(subsystemName);
            }
        }

        return valid;
    }

    /**
     * Loops through all states listed in robotconfigruation.ymal and calls initialize on any states that are becoming active this frame
     *
     * @param nextActiveStates the list of states that are active this frame
     */
    private void initializeNewlyActiveStates(Set<State> nextActiveStates) {
        for (String name : fAllStateNames) {
            State state = fSharedObjectsDirectory.getStateObject(name);

            boolean isInCurrent = fActiveStates.contains(state);
            boolean isInNext = nextActiveStates.contains(state);

            if (isInNext && !isInCurrent) {
                state.initialize();
            }
        }
    }

    /**
     * Loops through all states listed in robotconfigruation.ymal and calls dispose on any states that are becoming inactive this frame
     *
     * @param nextActiveStates the list of states that are active this frame
     */
    private void disposeInactiveStates(Set<State> nextActiveStates) {
        for (String name : fAllStateNames) {
            State state = fSharedObjectsDirectory.getStateObject(name);

            boolean isInCurrent = fActiveStates.contains(state);
            boolean isInNext = nextActiveStates.contains(state);

            if (isInCurrent && !isInNext) {
                state.dispose();
            }
        }
    }


    /**
     * @return the list of states that are active this frame
     */
    public Set<State> getCurrentActiveStates() {
        return fActiveStates;
    }
}
