package student;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import game.FindState;
import game.FleeState;
import game.Node;
import game.NodeStatus;
import game.SewerDiver;

public class DiverMin extends SewerDiver {

	/** Get to the ring in as few steps as possible. Once you get there, <br>
	 * you must return from this function in order to pick<br>
	 * it up. If you continue to move after finding the ring rather <br>
	 * than returning, it will not count.<br>
	 * If you return from this function while not standing on top of the ring, <br>
	 * it will count as a failure.
	 *
	 * There is no limit to how many steps you can take, but you will receive<br>
	 * a score bonus multiplier for finding the ring in fewer steps.
	 *
	 * At every step, you know only your current tile's ID and the ID of all<br>
	 * open neighbor tiles, as well as the distance to the ring at each of <br>
	 * these tiles (ignoring walls and obstacles).
	 *
	 * In order to get information about the current state, use functions<br>
	 * currentLocation(), neighbors(), and distanceToRing() in state.<br>
	 * You know you are standing on the ring when distanceToRing() is 0.
	 *
	 * Use function moveTo(long id) in state to move to a neighboring<br>
	 * tile by its ID. Doing this will change state to reflect your new position.
	 *
	 * A suggested first implementation that will always find the ring, but <br>
	 * likely won't receive a large bonus multiplier, is a depth-first walk. <br>
	 * Some modification is necessary to make the search better, in general. */
	@Override
	public void find(FindState state) {
		// TODO : Find the ring and return.
		// DO NOT WRITE ALL THE CODE HERE. DO NOT MAKE THIS METHOD RECURSIVE.
		// Instead, write your method elsewhere, with a good specification,
		// and call it from this one.
		List<Long> visit= new ArrayList<>();
		dfs(null, state, visit);
		return;
	}

	/** Visit every node that is reachable along a path of unvisited nodes from u using the depth-first
	 * search method until it finds the ring. Precondition: u has not been visited.
	 *
	 * @param previousState - the previous node
	 * @param state         - the current node
	 * @param               visit-contains all the nodes that are visited, including the start state */

	public void dfs(Long previousState, FindState state, List<Long> visit) {
		// Base case
		if (state.distanceToRing() == 0)
			return;
		long u= state.currentLocation();
		visit.add(u);
		List<NodeStatus> neighbors= new ArrayList<>(state.neighbors());
		Collections.sort(neighbors);
		for (NodeStatus node : neighbors) {
			if (!visit.contains(node.getId()) && state.distanceToRing() != 0) {
				state.moveTo(node.getId());
				dfs(u, state, visit);
			}
		}
		if (state.distanceToRing() != 0) {
			state.moveTo(previousState);
		}
	}

	/** Flee the sewer system before the steps are all used, trying to <br>
	 * collect as many coins as possible along the way. Your solution must ALWAYS <br>
	 * get out before the steps are all used, and this should be prioritized above<br>
	 * collecting coins.
	 *
	 * You now have access to the entire underlying graph, which can be accessed<br>
	 * through FleeState. currentNode() and getExit() will return Node objects<br>
	 * of interest, and getNodes() will return a collection of all nodes on the graph.
	 *
	 * You have to get out of the sewer system in the number of steps given by<br>
	 * getStepsRemaining(); for each move along an edge, this number is <br>
	 * decremented by the weight of the edge taken.
	 *
	 * Use moveTo(n) to move to a node n that is adjacent to the current node.<br>
	 * When n is moved-to, coins on node n are automatically picked up.
	 *
	 * You must return from this function while standing at the exit. Failing <br>
	 * to do so before steps run out or returning from the wrong node will be<br>
	 * considered a failed run.
	 *
	 * Initially, there are enough steps to get from the starting point to the<br>
	 * exit using the shortest path, although this will not collect many coins.<br>
	 * For this reason, a good starting solution is to use the shortest path to<br>
	 * the exit. */
	@Override
	public void flee(FleeState state) {
		// TODO: Get out of the sewer system before the steps are used up.
		// DO NOT WRITE ALL THE CODE HERE. Instead, write your method elsewhere,
		// with a good specification, and call it from this one.
		fleehelper(state);
		return;
	}

	/** This function is used to get as much coins as possible. It would try to head to the node with
	 * the maximum coins/distance value while the steps left are enough, and then go to the exit through
	 * the shortest path. */
	public void fleehelper(FleeState state) {
		Node exit= state.getExit();
		List<Node> path= Paths.shortest(state.currentNode(), exit);

		// lists the value of the (number of coins)/(the distance) ratio
		while (true) {
			ArrayList<Double> coinsInPath= new ArrayList<Double>();
			ArrayList<Node> nodes= new ArrayList<Node>();

			for (Node node : state.allNodes()) {
				if (node.getTile().coins() != 0) {
					nodes.add(node);
					List<Node> pathToCoins= Paths.shortest(state.currentNode(), node);
					double dist= Paths.pathSum(pathToCoins);
					double coin= node.getTile().coins();
					if (dist == 0)
						state.grabCoins();
					else {
						coinsInPath.add(coin / dist);
					}
				}
			}

			// check to see if there are no nodes with coins in them
			if (coinsInPath.isEmpty() == true)
				break;

			double m= Collections.max(coinsInPath);
			int n= coinsInPath.indexOf(m);
			Node max= nodes.get(n);

			// finds the nodes with the max coin/dist value
			List<Node> pathToMax= Paths.shortest(state.currentNode(), max);
			List<Node> pathToExit= Paths.shortest(max, exit);
			nodes.remove(n);
			coinsInPath.remove(n);

			// traverse maze if steps left are enough
			if (Paths.pathSum(pathToMax) + Paths.pathSum(pathToExit) <= state.stepsLeft()) {
				pathToMax.remove(0);
				for (Node node : pathToMax) {
					state.moveTo(node);
				}
				path= pathToExit;
			} else break;
		}

		// for any node in the path, if there's coins in its neighbor
		// then move to the neighbor and move back
		path.remove(0);
		for (Node n : path) {
			state.moveTo(n);
			for (Node node : state.currentNode().getNeighbors()) {
				int l= n.getEdge(node).length;
				int c= n.getTile().coins();
				int f= Paths.pathSum(path.subList(path.indexOf(n), path.size()));
				if (c != 0 && ((f + l * 2) <= state.stepsLeft())) {
					state.moveTo(node);
					state.moveTo(n);
				}
			}
		}
		return;
	}
}
