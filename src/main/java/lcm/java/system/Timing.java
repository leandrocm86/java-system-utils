package lcm.java.system;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * Profiling utility that abstracts the manipulation of {@link Stopwatch} instances to easily measure time in different parts of the application.
 * It supports multi-threading, and each thread has its own independent measurements and results, but each must end making its own {@link #getResultsAsString()} call.
 * A Timing thread is seen as a tree, where each node is a clock and its children are its subclocks (that start and stop while the parent is running).
 */
public class Timing {
	
	// Fatal inconsistencies can easily happen by wrong usage of Timing. When they happen, all the measurements are compromised and the result cannot be trusted.
	static final String FATAL_INCONSISTENCY_NAME_ALREADY_STARTED = "There's already a stopwatch named '%s' running on the current thread!";
	static final String FATAL_INCONSISTENCY_NO_CLOCK_TO_STOP = "There's no clock running on this thread named '%s' to stop!";
	static final String FATAL_INCONSISTENCY_WRONG_STOP_ORDER = "The last clock started was '%s'. It must stop before parent '%s'!";
	
	// Result inconsistencies are checked when the result is called. They may be consequences of fatal inconsistencies or unexpected internal behavior (bug).
	static final String RESULT_INCONSISTENCY_CLOCKS_RUNNING = "There are still clocks that started but were not stopped, so it shouldn't be time for results. Clocks that are still running: %s";
	static final String RESULT_INCONSISTENCY_TOTAL_TIME = "Something seems wrong with clock '%s'. Its total time (%,dms) is inferior to its children totals (%,dms).";
	static final String RESULT_INCONSISTENCY_TREE = "Something seems wrong with clock '%s'. It was referenced twice in the execution tree.";
	
	private List<String> log = new ArrayList<>();

	private TimeNode root = new TimeNode("_ROOT_", null);
	private Map<String, List<TimeNode>> nodesByName = new HashMap<>(); // Some clocks have the same name, but are different nodes (with different parents).
	private Deque<TimeNode> runningClocks = new ArrayDeque<>();
	
	private Exception fatalError = null;
	
	private Timing() {}

	/**
	 * Represents a node in the execution tree while timing a thread.
	 * Each node is a clock with its own total time, and it might have children (other "subclocks" with their own timers).
	 * A child clock is started after its parent, and it must be finished before it too.
	 */
	public class TimeNode extends Stopwatch {
		TimeNode parent;
		List<TimeNode> children = new ArrayList<>();

		TimeNode(String name, TimeNode parent) {
			super(name);
			this.parent = parent;
			if (parent != null)
				parent.children.add(this);
		}
		public TimeNode getParent() {
			return parent;
		}
		public List<TimeNode> getChildren() {
			return children;
		}
		public List<TimeNode> getAllDescendants() {
			List<TimeNode> allDescendants = new ArrayList<>(children);
			children.forEach(child -> allDescendants.addAll(child.getAllDescendants()));
			return allDescendants;
		}

		private Function<TimeNode, String> defaultToStringFunction = node -> {
			String clockName = node != root ? node.getName() : "Thread " + Thread.currentThread().getName();
			if (node != root && nodesByName.get(clockName).size() > 1)
				clockName += " (" + (nodesByName.get(clockName).indexOf(node) + 1) + ")";
			if (runningClocks.contains(node)) // Inconsistency that should be visible with '*'.
				clockName += "*";
			if (node.children.stream().mapToLong(c -> c.getTotalTime()).sum() > node.getTotalTime()) // Inconsistency that should be visible with '!'.
				clockName += "!";
			return String.format("%,dms [%s] {calls: %d}", node.getTotalTime(), clockName, node.getCheckCount());
		};

		/**
		 * Returns the default string representation of this tree node.
		 * For a more customized representation, use {@link #toString(Function, String, String)}.
		 * The default representation shows all the clock attributes (name, total time and number of calls) of a node and its children.
		 * Each new level of children is indented by an additional '\n\t', which is the default level separator.
		 * Some signals are also added by default:
		 * 	- '*' means that the clock was not stopped after its last run, which means the result might be inconsistent.
		 * 	- '!' means that the clock has a shorter total time than the sum of the times of its children, which is also an inconsistency.
		 * 	- '(1..n)' means that the clock name was referenced more than once in the execution tree, but with different parents.
		 *			Each clock with the same name but different parent has a node for itself in the tree, and a number is added to its name to distinguish them.
		 *
		 * @return the default string representation of TimingNode.
		 */
		@Override
		public String toString() {
			return toString(defaultToStringFunction, "\n", "\t");
		}

		/**
		 * Generates a string representation of this tree node using the provided separators.
		 *
		 * @param  clockSeparator    the separator to be used between each clock, no matter if children or not.
		 * @param  levelSeparator    the separator to be used between levels of the tree, which will be added to the children nodes.
		 * @return                   the customized string representation of TimeNode.
		 */
		public String toString(String clockSeparator, String levelSeparator) {
			return toString(defaultToStringFunction, clockSeparator, levelSeparator);
		}

		/**
		 * Generates a string representation of this tree node using the provided function and level separator.
		 *
		 * @param  toStringFunction  the function to convert a single TimeNode to a string.
		 * @param  clockSeparator    the separator to be used between each clock, no matter if children or not.
		 * @param  levelSeparator    the separator to be used between levels of the tree, which will be added to the children nodes.
		 * @return                   the customized string representation of TimeNode.
		 */
		public String toString(Function<TimeNode, String> toStringFunction, String clockSeparator, String levelSeparator) {
			StringBuilder result = new StringBuilder(toStringFunction.apply(this));
			for (TimeNode child : children) {
				String childToString = child.toString(toStringFunction, clockSeparator, levelSeparator);
				// Adds one level of indentation for all the other lines (grandchildren clocks).
				childToString = childToString.replace(clockSeparator, clockSeparator + levelSeparator);
				result.append(clockSeparator + levelSeparator + childToString);
			}
			return result.toString();
		}
	}
	
	private void startInstance(String clockName) {
		runningClocks.stream().filter(clock -> clock.getName().equals(clockName)).findAny()
			.ifPresent(k -> {throw generateFatalError(IllegalArgumentException.class, FATAL_INCONSISTENCY_NAME_ALREADY_STARTED, clockName);});
		
		TimeNode clock = this.getOrCreateNode(clockName);
		runningClocks.push(clock);
		clock.restartCheck();
	}
	
	private TimeNode getOrCreateNode(String clockName) {
		List<TimeNode> clocksWithGivenName = nodesByName.get(clockName);
		TimeNode currentParentClock = runningClocks.isEmpty() ? root : runningClocks.peek();
		
		// If there's already a clock with that name, we have to check if it's in the same workflow (same parent or root) as the current one.
		// If so, it means it's just another call on the same clock. I not, it's another clock with the same name.
		if (clocksWithGivenName != null) {
			
			// If an existing clock with that name has the last running clock as parent, it's the clock we want.
			for (TimeNode clock : clocksWithGivenName) {
				if (clock.getParent() == currentParentClock) {
					return clock;
				}
			}
		}
			
		TimeNode newClock = new TimeNode(clockName, currentParentClock);
		nodesByName.computeIfAbsent(clockName, k -> new ArrayList<>()).add(newClock);
		return newClock;
	}
	
	private void stopInstance(String clockName) {
		TimeNode clock = this.getRunningClock(clockName);
		
		clock.check();
		
		TimeNode lastClock = runningClocks.peek();
		if (lastClock != clock)
			throw generateFatalError(IllegalStateException.class, FATAL_INCONSISTENCY_WRONG_STOP_ORDER, lastClock.getName(), clock.getName());
		
		runningClocks.pop();
	}

	private TimeNode getRunningClock(String clockName) {
		return runningClocks.stream().filter(c -> c.getName().equals(clockName))
			.findAny().orElseThrow(() -> generateFatalError(IllegalArgumentException.class, FATAL_INCONSISTENCY_NO_CLOCK_TO_STOP, clockName));
	}
	
	private String getInstanceResultsAsString(String clockSeparator, String levelSeparator) {
		root.check();
		checkTreeConsistency(); // The only check we need in a string result. The other inconsistencies are highlighted in the string.
		return root.toString(root.defaultToStringFunction, clockSeparator, levelSeparator) + printStats(root);
	}

	public static String printStats(TimeNode startingNode) {
		List<TimeNode> allNodes = startingNode.getAllDescendants();
		List<TimeNode> leafNodes = allNodes.stream().filter(node -> node.children.isEmpty()).collect(Collectors.toList());
		List<TimeNode> nonLeafNodes = allNodes.stream().filter(node -> !node.children.isEmpty()).collect(Collectors.toList());
		long totalTime = startingNode.getTotalTime();
		StringBuilder result = new StringBuilder();

		if (totalTime > 0) {
			result.append("\n\nLeaf nodes ordered by total time:\n");
			leafNodes.sort(Comparator.comparingLong(TimeNode::getTotalTime).reversed());
			leafNodes.forEach(node -> {
				int percentageOfTotalTime = (int) (100 * node.getTotalTime() / totalTime);
				if (percentageOfTotalTime > 0)
					result.append(String.format("%s: %,dms (%d%%)%n", node.getName(), node.getTotalTime(), percentageOfTotalTime));
			});
		}

		nonLeafNodes.removeIf(node -> node.getTotalTime() <= 2 * node.getChildren().stream().mapToLong(Stopwatch::getTotalTime).sum());
		if (!nonLeafNodes.isEmpty()) {
			result.append("\nParent nodes with total time way bigger than their children's sum:\n");
			nonLeafNodes.forEach(node ->
				result.append(String.format("%s: %,dms >> %,dms%n", node.getName(), node.getTotalTime(),
							node.getChildren().stream().mapToLong(Stopwatch::getTotalTime).sum()))
			);
		}

		return result.toString();
	}
	
	private void checkNoClockIsRunning() {
		if (!runningClocks.isEmpty()) {
			String errorMsg = String.format(RESULT_INCONSISTENCY_CLOCKS_RUNNING, runningClocks.stream().map(Stopwatch::getName).collect(Collectors.joining(", ")));
			throw new IllegalStateException(errorMsg);
		}
	}
	
	private void checkTotalTimesConsistency() {
		root.getAllDescendants().forEach(node -> {
			long childrenTotalTime = node.children.stream().mapToLong(Stopwatch::getTotalTime).sum();
			if (childrenTotalTime > node.getTotalTime())
				throw new IllegalStateException(String.format(RESULT_INCONSISTENCY_TOTAL_TIME,
						node.getName(), node.getTotalTime(), childrenTotalTime));
		});
	}
	
	private void checkTreeConsistency() {
		List<TimeNode> ancestors = new ArrayList<>();
		root.children.forEach(node -> checkTreeConsistency(ancestors, node));
	}
	
	private void checkTreeConsistency(List<TimeNode> ancestors, TimeNode descendant) {
		if (ancestors.contains(descendant))
			throw new IllegalStateException(String.format(RESULT_INCONSISTENCY_TREE, descendant.getName()));
		ancestors.add(descendant);
		for (TimeNode child : descendant.children)
			checkTreeConsistency(ancestors, child);
	}
	
	private TimeNode getInstanceResults() {
		root.check();
		checkNoClockIsRunning(); // There can be no clock still running when the results are called.
		checkTotalTimesConsistency(); // The total time measured for each clock can't be greater than the sum of their respective children.
		checkTreeConsistency(); // Ensures that each instance of Stopwatch has only one parent (appearing only once in the tree).
		return root;
	}
	
	private RuntimeException generateFatalError(Class<? extends RuntimeException> type, String messageTemplate, Object... args) {
		String formattedMsg = String.format(messageTemplate, args);
		log.add("GENERATING FATAL ERROR: " + formattedMsg);
		RuntimeException newFatalError;
		try {
			newFatalError = type.getConstructor(String.class).newInstance(formattedMsg);
		} catch (Exception e) {
			newFatalError = new IllegalStateException(formattedMsg);
		}
		if (this.fatalError == null) { // If there's more than one fatal error, only the first is preserved.
			this.fatalError = newFatalError;
			log.add(
				"nodes: " + root + "\n" +
				"nodesByName: " + nodesByName + "\n" +
				"runningClocks: " + runningClocks
			);
		}
		return newFatalError;
	}
	
	// ==================================================== static methods for concurrency/thread handling ====================================================
	
	private static ThreadLocal<Timing> instancesByThread = ThreadLocal.withInitial(Timing::new);

	private static void logException(Exception e, String operation) {
		Timing timing = instancesByThread.get();
		timing.log.add("EXCEPTION THROWN PROCESSING " + operation + ". " + e.getClass().getName() + ": " + e.getMessage());
	}

	/**
	 * Starts a time counter (clock) for a specific section of code that will be labeled with a given name.
	 * This name shouldn't be repeated if another clock with the same name hasn't been stopped yet.
	 * @param clockName - The (preferably unique) identifier for the code to be measured.
	 */
	public static void start(String clockName) {
		try { instancesByThread.get().startInstance(clockName); } catch (Exception e) { logException(e, "START"); }
	}
	
	/**
	 * Stops the time counter (clock) of a specific section of code that was labeled with the given name in the current thread.
	 * @param clockName - The identifier of the code that is being finished in the current thread.
	 */
	public static void stop(String clockName) {
		try { instancesByThread.get().stopInstance(clockName); } catch (Exception e) { logException(e, "STOP"); }
	}
	
	/**
	 * Stops all the time counters (clocks) still running in the current thread (all started but not stopped).
	 * This should be used carrefully, preferably on situations we can't tell what clocks are still running,
	 * like on error handlings (inside catch blocks).
	 */
	public static void stopAll() {
		Deque<TimeNode> runningClocks = instancesByThread.get().runningClocks;
		int totalToStop = runningClocks.size();
		while (totalToStop > 0) {
			try { instancesByThread.get().stopInstance(runningClocks.peek().getName()); } catch (Exception e) { logException(e, "STOPALL"); }
			totalToStop--;
		}
	}
	
	/**
	 * Stops all the time counters (clocks) still running in the current thread after a given clock (inclusive or exclusive).
	 * This should be used carrefully, preferably on situations we can't tell what clocks are still running,
	 * like on error handlings (stopping clocks declared in a try block, from inside a catch block).
	 * 
	 * @param clockName - The identifier of the clock after which all clocks will be stopped.
	 * @param inclusive - If true, the clock with the given name will also be stopped.
	 */
	public static void stopAllAfter(String clockName, boolean inclusive) {
		try {
			instancesByThread.get().getRunningClock(clockName); // Checks if there's really a clock with that name.
			Deque<TimeNode> runningClocks = instancesByThread.get().runningClocks;
			while (!runningClocks.isEmpty()) {
				String lastRunningClock = runningClocks.peek().getName();
				if (inclusive || !lastRunningClock.equals(clockName))
					instancesByThread.get().stopInstance(lastRunningClock);
				if (lastRunningClock.equals(clockName))
					break;
			}
		} catch (Exception e) {
			logException(e, "STOPAFTER");
		}
	}
	
	/**
	 * Returns the results of all clocks run during the current thread in a String format.
	 * This should be invoked at the end of the thread's workflow, preferably inside a finally statement,
	 * because it will check if all clocks were stopped and will also clear the thread's timing objects afterwards.
	 * 
	 * This format aims to map everything to String, even inconsistency errors, so the application is never stopped by Timing. 
	 * 
	 * @param  clockSeparator    the separator to be used between each clock, no matter if children or not.
	 * @param  levelSeparator    the separator to be used between levels of the tree, which will be added to the children nodes.
	 * 
	 * @return string with the results of all clocks run during the current thread.
	 */
	public static String getResultsAsString(String clockSeparator, String levelSeparator) {
		Timing currentTiming = instancesByThread.get();
		try {
			if (currentTiming.fatalError != null) {
				currentTiming.fatalError.printStackTrace();
				return "!!! There has been a fatal inconsistency while running Timing. Error message: " + currentTiming.fatalError.getMessage()
					+ "\n" + String.join("\n", currentTiming.log);
			}
			else {
				return instancesByThread.get().getInstanceResultsAsString(clockSeparator, levelSeparator);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			return "!!! There has been an exception while trying to get the Timing results. Message: " + e.getMessage()
				+ "\n" + String.join("\n", currentTiming.log);
		}
		finally {
			instancesByThread.remove(); // Clean the thread timing objects.
		}
	}

	/**
	 * Default format of results in String.
	 * Calls {@link #getResultsAsString(String, String)} with "\n" as clock separator and "\t" as level separator.
	 *
	 * @return string with the results of all clocks run during the current thread.
	 * @see #getResultsAsString(String, String)
	 */
	public static String getResultsAsString() {
		return getResultsAsString("\n", "\t");
	}
	
	/**
	 * Returns the root node of the execution tree with all clocks run during the current thread, each with their respective children.
	 * This should be invoked at the end of the thread's workflow, preferably inside a finally statement,
	 * because it will check if all clocks were stopped and will also clear the thread's timing objects afterwards.
	 * 
	 * This format of results must be used with caution, because it can raise exceptions in case of errors.
	 * For an easier approach, consider using {@link #getResultsAsString()}.
	 * 
	 * @return string with the results of all clocks run during the current thread.
	 * @throws IllegalStateException when some inconsistency is detected.
	 */
	public static TimeNode getResults() {
		try {
			Timing currentBenchmark = instancesByThread.get();
			if (currentBenchmark.fatalError != null)
				throw new IllegalStateException("!!! There has been a fatal inconsistency while running Timing. Error message: " + currentBenchmark.fatalError.getMessage(), currentBenchmark.fatalError);
			return instancesByThread.get().getInstanceResults();
		} finally {
			instancesByThread.remove(); // Clean the thread timing objects.
		}
	}
	
}

