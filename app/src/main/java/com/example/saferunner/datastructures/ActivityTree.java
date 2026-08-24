package com.example.saferunner.datastructures;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.saferunner.data.Activity;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

//CONSTRUCTION: with no parameters
//
//******************PUBLIC OPERATIONS*********************
//void insert( x )       --> insert x
//void remove( x )       --> remove x
//boolean contains( x )  --> return true if x is found
//comparable findMin( )  --> return smallest item
//comparable findMax( )  --> return largest item
//boolean isEmpty( )     --> return true if empty; else false
//void makeEmpty( )      --> remove all items
//void printTree( )      --> print all items
//******************ERRORS********************************
//throws UnderflowException as appropriate

/**
 * implements a red-black tree. note that all "matching" is based on the
 * compareTo method.
 * <p>
 * modified with a working, but extremely inefficient implementation of remove,
 * and following checkstyles styleguide.
 *
 * @author mark allen weiss
 */
public class ActivityTree {
    private static final int BLACK = 1; // black must be 1
    private static final int RED = 0;

    private RedBlackNode header;
    private RedBlackNode nullNode;

    // used in insert routine and its helpers
    private RedBlackNode current;
    private RedBlackNode parent;
    private RedBlackNode grand;
    private RedBlackNode great;
    private List<RedBlackNode> removalPath;
    private int removalParentIndex;

    /**
     * construct the tree.
     */
    public ActivityTree() {
        nullNode = new RedBlackNode(null);
        nullNode.left = nullNode.right = nullNode;
        header = new RedBlackNode(null);
        header.left = header.right = nullNode;
    }

    /**
     * compare item and t.element, using compareTo, with caveat that if t is header,
     * then item is always larger. this routine is called if is possible that t is
     * header. if it is not possible for t to be header, use compareTo directly.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private int compare(Activity item, RedBlackNode t) {
        if (t == header)
            return 1;
        else
            return item.compareTo(t.element);
    }

    /**
     * insert into the tree.
     *
     * @param item the item to insert.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void insert(Activity item) {
        current = parent = grand = header;
        nullNode.element = item;

        while (compare(item, current) != 0) {
            great = grand;
            grand = parent;
            parent = current;
            current = compare(item, current) < 0 ? current.left : current.right;

            // check if two red children; fix if so
            if (current.left.color == RED && current.right.color == RED)
                handleReorient(item);
        }

        // insertion fails if already present
        if (current != nullNode)
            return;
        current = new RedBlackNode(item, nullNode, nullNode);

        // attach to parent
        if (compare(item, parent) < 0)
            parent.left = current;
        else
            parent.right = current;
        handleReorient(item);
    }

    /**
     * remove from the tree.
     * <p>
     * the current version works, but is extremely inefficient since it copies the
     * entire content for each remove operation. your job is to replace it with an
     * efficient version that works on the tree directly.
     *
     * @param x the item to remove.
     */

    @RequiresApi(Build.VERSION_CODES.O)
    public void remove(Activity x) {
        if (!contains(x)) {
            return;
        }

        removalPath = locate(x);
        RedBlackNode node =
                promoteSuccessor(removalPath.get(removalPath.size() - 1));

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private List<RedBlackNode> locate(Activity x) {
        List<RedBlackNode> path = new ArrayList<>();
        path.add(header);

        RedBlackNode node = header.right;
        while (node != nullNode) {
            path.add(node);

            int comparison = x.compareTo(node.element);
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

        node.element = successor.element;
        return successor;
    }

    private RedBlackNode detach(RedBlackNode node) {
        RedBlackNode child =
                node.left != nullNode ? node.left : node.right;

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

    private void reattach(
            RedBlackNode oldSubtreeRoot,
            RedBlackNode newSubtreeRoot) {

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
     * find the smallest item the tree.
     *
     * @return the smallest item or throw UnderflowExcepton if empty.
     */
    public Activity findMin() {
        if (isEmpty())
            throw new UnderflowException();

        RedBlackNode itr = header.right;

        while (itr.left != nullNode)
            itr = itr.left;

        return itr.element;
    }

    /**
     * find the largest item the tree.
     *
     * @return the largest item or throw UnderflowExcepton if empty.
     */
    public Activity findMax() {
        if (isEmpty())
            throw new UnderflowException();

        RedBlackNode itr = header.right;

        while (itr.right != nullNode)
            itr = itr.right;

        return itr.element;
    }

    /**
     * find an item in the tree.
     *
     * @param x the item to search for.
     * @return true if x is found; otherwise false.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    public boolean contains(Activity x) {
        nullNode.element = x;
        current = header.right;

        for (;;) {
            if (x.compareTo(current.element) < 0)
                current = current.left;
            else if (x.compareTo(current.element) > 0)
                current = current.right;
            else
                return current != nullNode;
        }
    }

    /**
     * make the tree logically empty.
     */
    public void makeEmpty() {
        header.right = nullNode;
    }

    /**
     * print the tree contents in sorted order.
     */
    public void printTree() {
        if (isEmpty())
            System.out.println("Empty tree");
        else
            printTree(header.right);
    }

    /**
     * internal method to print a subtree in sorted order.
     *
     * @param t the node that roots the subtree.
     */
    private void printTree(RedBlackNode t) {
        if (t != nullNode) {
            printTree(t.left);
            System.out.println(t.element);
            printTree(t.right);
        }
    }

    /**
     * get all activities within a given date range.
     *
     * @param start the beginning of the date range.
     * @param end the end of the date range.
     * @return all activities within the given date range.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<Activity> getActivitiesBetween(Instant start, Instant end) {
        List<Activity> activities = new ArrayList<>();
        collectBetween(header.right, start, end, activities);
        return activities;
    }

    /**
     * collect activities within a given date range.
     *
     * @param node the current node.
     * @param start the beginning of the date range.
     * @param end the end of the date range.
     * @param activities the list to add matching activities to.
     */

    @RequiresApi(Build.VERSION_CODES.O)
    private void collectBetween(
            RedBlackNode node,
            Instant start,
            Instant end,
            List<Activity> activities) {

        if (node == nullNode) {
            return;
        }

        Instant activityTime = node.element.getStartTime();

        if (activityTime.isAfter(start)) {
            collectBetween(node.left, start, end, activities);
        }

        if (!activityTime.isBefore(start)
                && !activityTime.isAfter(end)) {
            activities.add(node.element);
        }

        if (activityTime.isBefore(end)) {
            collectBetween(node.right, start, end, activities);
        }
    }

    /**
     * test if the tree is logically empty.
     *
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty() {
        return header.right == nullNode;
    }

    /**
     * internal routine that is called during an insertion if a node has two red
     * children. performs flip and rotations.
     *
     * @param item the item being inserted.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void handleReorient(Activity item) {
        // do the color flip
        current.color = RED;
        current.left.color = BLACK;
        current.right.color = BLACK;

        if (parent.color == RED) // have to rotate
        {
            grand.color = RED;
            if ((compare(item, grand) < 0) != (compare(item, parent) < 0))
                parent = rotate(item, grand);
            current = rotate(item, great);
            current.color = BLACK;
        }
        header.right.color = BLACK; // make root black
    }

    /**
     * internal routine that performs a single or double rotation. because the
     * result is attached to the parent, there are four cases. called by
     * handleReorient.
     *
     * @param item   the item in handleReorient.
     * @param parent the parent of the root of the rotated subtree.
     * @return the root of the rotated subtree.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private RedBlackNode rotate(Activity item, RedBlackNode parent) {
        if (compare(item, parent) < 0)
            return parent.left = compare(item, parent.left) < 0
                    ? rotateWithLeftChild(parent.left)
                    : rotateWithRightChild(parent.left);
        else
            return parent.right = compare(item, parent.right) < 0
                    ? rotateWithLeftChild(parent.right)
                    : rotateWithRightChild(parent.right);
    }

    /**
     * rotate binary tree node with left child.
     */
    private RedBlackNode rotateWithLeftChild(RedBlackNode node) {
        RedBlackNode leftChild = node.left;
        node.left = leftChild.right;
        leftChild.right = node;
        return leftChild;
    }

    /**
     * rotate binary tree node with right child.
     */
    private RedBlackNode rotateWithRightChild(RedBlackNode node) {
        RedBlackNode rightChild = node.right;
        node.right = rightChild.left;
        rightChild.left = node;
        return rightChild;
    }

    private static class RedBlackNode {
        private Activity element;
        private RedBlackNode left;
        private RedBlackNode right;
        private int color;

        // constructors
        RedBlackNode(Activity theElement) {
            this(theElement, null, null);
        }

        RedBlackNode(
                Activity theElement,
                RedBlackNode lt,
                RedBlackNode rt) {

            element = theElement;
            left = lt;
            right = rt;
            color = ActivityTree.BLACK;
        }

        @Override
        public String toString() {
            return element == null ? "NULL" : element.toString();
        }
    }
}