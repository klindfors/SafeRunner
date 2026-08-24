package com.example.saferunner.datastructures;

// Feel free to use packages in your own environment, but remember to remove when handing it in

//RedBlackTree class
//
//CONSTRUCTION: with no parameters
//
//******************PUBLIC OPERATIONS*********************
//void insert( x )       --> Insert x
//void remove( x )       --> Remove x (unimplemented)
//boolean contains( x )  --> Return true if x is found
//Comparable findMin( )  --> Return smallest item
//Comparable findMax( )  --> Return largest item
//boolean isEmpty( )     --> Return true if empty; else false
//void makeEmpty( )      --> Remove all items
//void printTree( )      --> Print all items
//******************ERRORS********************************
//Throws UnderflowException as appropriate

import com.example.saferunner.data.Activity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a red-black tree. Note that all "matching" is based on the
 * compareTo method.
 * <p>
 * Modified with a working, but extremely inefficient implementation of remove,
 * and following Checkstyles styleguide.
 *
 * @author Mark Allen Weiss
 */
public class ActivityTree {
    private static final int BLACK = 1; // BLACK must be 1
    private static final int RED = 0;

    private RedBlackNode header;
    private RedBlackNode nullNode;

    // Used in insert routine and its helpers
    private RedBlackNode current;
    private RedBlackNode parent;
    private RedBlackNode grand;
    private RedBlackNode great;
    private List<RedBlackNode> removalPath;
    private int removalParentIndex;

    /**
     * Construct the tree.
     */
    public ActivityTree() {
        nullNode = new RedBlackNode(null);
        nullNode.left = nullNode.right = nullNode;
        header = new RedBlackNode(null);
        header.left = header.right = nullNode;
    }

    /**
     * Compare item and t.element, using compareTo, with caveat that if t is header,
     * then item is always larger. This routine is called if is possible that t is
     * header. If it is not possible for t to be header, use compareTo directly.
     */
    private int compare(LocalDate date, RedBlackNode t) {
        if (t == header)
            return 1;
        else
            return date.compareTo(t.date);
    }

    /**
     * Insert into the tree.
     *
     * @param item the item to insert.
     */
    public void insert(Activity item) {
        LocalDate date = getDate(item.getStartTime());

        current = parent = grand = header;
        nullNode.date = date;

        while (compare(date, current) != 0) {
            great = grand;
            grand = parent;
            parent = current;
            current = compare(date, current) < 0 ? current.left : current.right;

            // Check if two red children; fix if so
            if (current.left.color == RED && current.right.color == RED)
                handleReorient(date);
        }

        if (current != nullNode) {
            current.activities.add(item);
            return;
        }

        current = new RedBlackNode(date, nullNode, nullNode);
        current.activities.add(item);

        // Attach to parent
        if (compare(date, parent) < 0)
            parent.left = current;
        else
            parent.right = current;
        handleReorient(date);
    }

    /**
     * Remove from the tree.
     * <p>
     * The current version works, but is extremely inefficient since it copies the
     * entire content for each remove operation. Your job is to replace it with an
     * efficient version that works on the tree directly.
     *
     * @param x the item to remove.
     */
    public void remove(Activity x) {
        LocalDate date = getDate(x.getStartTime());

        removalPath = locate(date);

        RedBlackNode node = removalPath.get(removalPath.size() - 1);

        if (node == nullNode || !node.activities.remove(x))
            return;

        if (!node.activities.isEmpty())
            return;

        node = promoteSuccessor(node);

        int nodeColor = node.color;
        RedBlackNode child = detach(node);

        if (nodeColor == RED) {
            header.right.color = BLACK;
            return;
        }

        if (child.color == RED) {
            child.color = BLACK;
            header.right.color = BLACK;
            return;
        }

        rebalance(child);
    }

    private List<RedBlackNode> locate(LocalDate date) {
        List<RedBlackNode> path = new ArrayList<>();
        path.add(header);

        RedBlackNode node = header.right;
        while (node != nullNode) {
            path.add(node);

            int comparison = date.compareTo(node.date);
            if (comparison == 0) {
                break;
            }

            node = comparison < 0 ? node.left : node.right;
        }

        return path;
    }

    private RedBlackNode promoteSuccessor(RedBlackNode node) {
        if (node.left == nullNode || node.right == nullNode) {
            return node;
        }

        RedBlackNode successor = node.right;
        removalPath.add(successor);
        while (successor.left != nullNode) {
            successor = successor.left;
            removalPath.add(successor);
        }

        node.date = successor.date;
        node.activities = successor.activities;
        return successor;
    }

    private RedBlackNode detach(RedBlackNode node) {
        RedBlackNode child = node.left != nullNode ? node.left : node.right;

        removalParentIndex = removalPath.size() - 2;
        RedBlackNode nodeParent = removalPath.get(removalParentIndex);

        if (nodeParent == header) {
            header.right = child;
        } else if (nodeParent.left == node) {
            nodeParent.left = child;
        } else {
            nodeParent.right = child;
        }

        return child;
    }

    private void rebalance(RedBlackNode child) {
        RedBlackNode currentNode = child;

        while (currentNode != header.right && currentNode.color == BLACK) {
            RedBlackNode nodeParent = removalPath.get(removalParentIndex);
            if (nodeParent == header) {
                break;
            }

            currentNode = currentNode == nodeParent.left
                    ? fixLeft(nodeParent)
                    : fixRight(nodeParent);
        }

        currentNode.color = BLACK;
        header.right.color = BLACK;
    }

    private RedBlackNode fixLeft(RedBlackNode nodeParent) {
        RedBlackNode sibling = nodeParent.right;

        if (sibling.color == RED) {
            sibling.color = BLACK;
            nodeParent.color = RED;
            reattach(nodeParent, rotateWithRightChild(nodeParent));
            sibling = nodeParent.right;
        }

        if (sibling.left.color == BLACK && sibling.right.color == BLACK) {
            sibling.color = RED;
            removalParentIndex--;
            return nodeParent;
        }

        if (sibling.right.color == BLACK) {
            sibling.left.color = BLACK;
            sibling.color = RED;
            nodeParent.right = rotateWithLeftChild(sibling);
            sibling = nodeParent.right;
        }

        sibling.color = nodeParent.color;
        nodeParent.color = BLACK;
        sibling.right.color = BLACK;
        reattach(nodeParent, rotateWithRightChild(nodeParent));

        return header.right;
    }

    private RedBlackNode fixRight(RedBlackNode nodeParent) {
        RedBlackNode sibling = nodeParent.left;

        if (sibling.color == RED) {
            sibling.color = BLACK;
            nodeParent.color = RED;
            reattach(nodeParent, rotateWithLeftChild(nodeParent));
            sibling = nodeParent.left;
        }

        if (sibling.left.color == BLACK && sibling.right.color == BLACK) {
            sibling.color = RED;
            removalParentIndex--;
            return nodeParent;
        }

        if (sibling.left.color == BLACK) {
            sibling.right.color = BLACK;
            sibling.color = RED;
            nodeParent.left = rotateWithRightChild(sibling);
            sibling = nodeParent.left;
        }
        sibling.color = nodeParent.color;
        nodeParent.color = BLACK;
        sibling.left.color = BLACK;
        reattach(nodeParent, rotateWithLeftChild(nodeParent));
        return header.right;
    }

    private void reattach(RedBlackNode oldSubtreeRoot, RedBlackNode newSubtreeRoot) {
        RedBlackNode grandParent = removalPath.get(removalParentIndex - 1);
        if (grandParent == header) {
            header.right = newSubtreeRoot;
        } else if (grandParent.left == oldSubtreeRoot) {
            grandParent.left = newSubtreeRoot;
        } else {
            grandParent.right = newSubtreeRoot;
        }

        removalPath.set(removalParentIndex - 1, newSubtreeRoot);
    }

    /**
     * Find the smallest item the tree.
     *
     * @return the smallest item or throw UnderflowExcepton if empty.
     */
    public LocalDate findMin() {
        if (isEmpty())
            throw new UnderflowException();

        RedBlackNode itr = header.right;

        while (itr.left != nullNode)
            itr = itr.left;

        return itr.date;
    }

    /**
     * Find the largest item in the tree.
     *
     * @return the largest item or throw UnderflowExcepton if empty.
     */
    public LocalDate findMax() {
        if (isEmpty())
            throw new UnderflowException();

        RedBlackNode itr = header.right;

        while (itr.right != nullNode)
            itr = itr.right;

        return itr.date;
    }

    /**
     * Find an item in the tree.
     *
     * @param x the item to search for.
     * @return true if x is found; otherwise false.
     */
    public boolean contains(Activity x) {
        LocalDate date = getDate(x.getStartTime());

        RedBlackNode node = header.right;

        while (node != nullNode) {
            int comparison = date.compareTo(node.date);

            if (comparison < 0)
                node = node.left;
            else if (comparison > 0)
                node = node.right;
            else
                return node.activities.contains(x);
        }

        return false;
    }

    /**
     * Make the tree logically empty.
     */
    public void makeEmpty() {
        header.right = nullNode;
    }

    /**
     * Print the tree contents in sorted order.
     */
    public void printTree() {
        if (isEmpty())
            System.out.println("Empty tree");
        else
            printTree(header.right);
    }

    /**
     * Internal method to print a subtree in sorted order.
     *
     * @param t the node that roots the subtree.
     */
    private void printTree(RedBlackNode t) {
        if (t != nullNode) {
            printTree(t.left);
            System.out.println(t.date + ": " + t.activities);
            printTree(t.right);
        }
    }

    /**
     * Test if the tree is logically empty.
     *
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty() {
        return header.right == nullNode;
    }

    /**
     * Get all activities stored on a date.
     *
     * @param date the date to search for.
     * @return all activities stored on the date.
     */
    public List<Activity> getActivitiesOnDate(LocalDate date) {
        RedBlackNode node = header.right;

        while (node != nullNode) {
            int comparison = date.compareTo(node.date);

            if (comparison < 0)
                node = node.left;
            else if (comparison > 0)
                node = node.right;
            else
                return new ArrayList<>(node.activities);
        }

        return new ArrayList<>();
    }

    /**
     * Get all activities stored between two dates.
     *
     * @param start the first date.
     * @param end the last date.
     * @return all activities stored between the dates.
     */
    public List<Activity> getActivitiesBetween(
            LocalDate start,
            LocalDate end) {

        List<Activity> activities = new ArrayList<>();
        collectBetween(header.right, start, end, activities);
        return activities;
    }

    private void collectBetween(
            RedBlackNode node,
            LocalDate start,
            LocalDate end,
            List<Activity> activities) {

        if (node == nullNode)
            return;

        if (node.date.isAfter(start))
            collectBetween(node.left, start, end, activities);

        if (!node.date.isBefore(start) && !node.date.isAfter(end))
            activities.addAll(node.activities);

        if (node.date.isBefore(end))
            collectBetween(node.right, start, end, activities);
    }

    /**
     * Internal routine that is called during an insertion if a node has two red
     * children. Performs flip and rotations.
     *
     * @param item the item being inserted.
     */
    private void handleReorient(LocalDate item) {
        // Do the color flip
        current.color = RED;
        current.left.color = BLACK;
        current.right.color = BLACK;

        if (parent.color == RED) // Have to rotate
        {
            grand.color = RED;
            if ((compare(item, grand) < 0) != (compare(item, parent) < 0))
                parent = rotate(item, grand); // Start dbl rotate
            current = rotate(item, great);
            current.color = BLACK;
        }
        header.right.color = BLACK; // Make root black
    }

    /**
     * Internal routine that performs a single or double rotation. Because the
     * result is attached to the parent, there are four cases. Called by
     * handleReorient.
     *
     * @param item   the item in handleReorient.
     * @param parent the parent of the root of the rotated subtree.
     * @return the root of the rotated subtree.
     */
    private RedBlackNode rotate(LocalDate item, RedBlackNode parent) {
        if (compare(item, parent) < 0)
            return parent.left = compare(item, parent.left) < 0 ? rotateWithLeftChild(parent.left) : // LL
                    rotateWithRightChild(parent.left); // LR
        else
            return parent.right = compare(item, parent.right) < 0 ? rotateWithLeftChild(parent.right) : // RL
                    rotateWithRightChild(parent.right); // RR
    }

    /**
     * Rotate binary tree node with left child.
     */
    private RedBlackNode rotateWithLeftChild(RedBlackNode node) {
        RedBlackNode leftChild = node.left;
        node.left = leftChild.right;
        leftChild.right = node;
        return leftChild;
    }

    /**
     * Rotate binary tree node with right child.
     */
    private RedBlackNode rotateWithRightChild(RedBlackNode node) {
        RedBlackNode rightChild = node.right;
        node.right = rightChild.left;
        rightChild.left = node;
        return rightChild;
    }

    private LocalDate getDate(Instant startTime) {
        return startTime.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static class RedBlackNode {
        private LocalDate date;
        private List<Activity> activities;
        private RedBlackNode left; // Left child
        private RedBlackNode right; // Right child
        private int color; // Color

        // Constructors
        RedBlackNode(LocalDate date) {
            this(date, null, null);
        }

        RedBlackNode(LocalDate date, RedBlackNode lt, RedBlackNode rt) {
            this.date = date;
            activities = new ArrayList<>();
            left = lt;
            right = rt;
            color = ActivityTree.BLACK;
        }

        @Override
        public String toString() {
            return date == null ? "NULL" : date.toString();
        }
    }
}