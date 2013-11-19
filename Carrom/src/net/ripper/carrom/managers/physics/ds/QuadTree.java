package net.ripper.carrom.managers.physics.ds;

import java.util.ArrayList;

import net.ripper.carrom.model.Piece;
import android.graphics.Rect;

/**
 * Stores objects in 2d based on their coordinates in the plane. Gives all
 * neighboring objects which might collide with a given object. Prevents a full
 * scan in the space to find possible collision pairs
 * 
 * @author TheRiPPer
 * 
 */
public class QuadTree {

	private static final int MAX_LEVELS = 4;
	private static final int MAX_OBJECTS = 5;

	private Rect bounds;
	private QuadTree[] nodes;
	private int level;
	private ArrayList<Piece> objects;

	public QuadTree(int level, Rect bounds) {
		this.bounds = bounds;
		this.level = level;
		// each node in quad tree has four child nodes
		this.nodes = new QuadTree[4];
		this.objects = new ArrayList<Piece>();
	}

	/**
	 * clear all objects in the current node and clear nodes in child nodes
	 * recursively
	 */
	public void clear() {
		objects.clear();

		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i] != null) {
				nodes[i].clear();
				nodes[i] = null;
			}
		}
	}

	/**
	 * Find a place in the tree for a new object
	 * 
	 * @param object
	 * @return index of node where the object should be inserted, -1 if the
	 *         object should be placed in the parent node
	 */
	private int getIndex(Piece object) {
		int index = -1;
		int xMidPoint = bounds.centerX();
		int yMidPoint = bounds.centerY();

		int objectX = (int) (object.region.x - object.region.radius);
		int objectY = (int) (object.region.y - object.region.radius);
		int objectHeight = (int) (2 * object.region.radius);
		int objectWidth = (int) (2 * object.region.radius);

		boolean atTopQuad = (objectY < yMidPoint)
				&& (objectY + objectHeight < yMidPoint);
		boolean atBottomQuad = (objectY > yMidPoint);

		if (objectX < xMidPoint && (objectX + objectWidth < xMidPoint)) {
			if (atTopQuad) {
				index = 0;
			} else if (atBottomQuad) {
				index = 2;
			}
		} else if (objectX > xMidPoint) {
			if (atTopQuad) {
				index = 1;
			} else if (atBottomQuad) {
				index = 3;
			}
		}

		return index;
	}

	/**
	 * split the current node
	 */
	private void split() {
		int subWidth = bounds.width() / 2;
		int subHeight = bounds.height() / 2;
		int x = bounds.left;
		int y = bounds.top;

		nodes[0] = new QuadTree(level + 1, new Rect(x, y, subWidth, subHeight));
		nodes[1] = new QuadTree(level + 1, new Rect(x + subWidth, y, subWidth,
				subHeight));
		nodes[2] = new QuadTree(level + 1, new Rect(x, y + subHeight, subWidth,
				subHeight));
		nodes[3] = new QuadTree(level + 1, new Rect(x + subWidth,
				y + subHeight, subWidth, subHeight));
	}

	/**
	 * insert the object in the tree
	 * 
	 * @param object
	 */
	public void insert(Piece object) {
		if (nodes[0] != null) {
			int index = getIndex(object);
			if (index != -1) {
				nodes[index].insert(object);
				return;
			}
		}

		objects.add(object);

		// check if the node can be split
		if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
			if (nodes[0] == null) {
				split();
			}

			// remove objects from the current node and insert them again, so
			// that they can fit themselves in the child nodes
			int i = 0;
			while (i < objects.size()) {
				int index = getIndex(objects.get(i));
				if (index != -1) {
					nodes[index].insert(objects.remove(i));
				} else {
					// object doesn't fit in any of the sub nodes, keep it in
					// the parent node
					i++;
				}
			}
		}
	}

	/**
	 * Return all neighbors for the given object
	 * 
	 * @param object
	 * @return
	 */
	public ArrayList<Piece> getNeighbors(Piece object) {
		ArrayList<Piece> neighbors = new ArrayList<Piece>();
		int index = getIndex(object);
		if (index != -1 && nodes[0] != null) {
			neighbors.addAll(nodes[index].getNeighbors(object));
		}
		neighbors.addAll(this.objects);
		return neighbors;
	}

}
