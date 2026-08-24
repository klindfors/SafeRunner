package com.example.saferunner;

import com.example.saferunner.data.Activity;
import com.example.saferunner.data.ActivityType;
import com.example.saferunner.datastructures.ActivityTree;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.Instant;

public class TreeTest {

    private static final int BLACK = 1;
    private static final int RED = 0;

    private void assertIsRedBlackTree(ActivityTree tree) {
        try {
            Object header = getField(tree, "header");
            Object nullNode = getField(tree, "nullNode");

            Object root = getField(header, "right");

            int expectedBlackNodes = countExpectedBlackNodes(root, nullNode);

            assertIsRedBlackTree(
                    expectedBlackNodes,
                    0,
                    BLACK,
                    root,
                    nullNode
            );
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private void assertIsRedBlackTree(
            int expectedBlackNodes,
            int actualBlackNodes,
            int parentColor,
            Object node,
            Object nullNode) throws Exception {

        if (node == nullNode) {
            assertEquals(expectedBlackNodes, actualBlackNodes);
            return;
        }

        int color = getFieldInt(node, "color");

        switch (color) {
            case BLACK:
                actualBlackNodes++;
                break;

            case RED:
                assertEquals(
                        "A red node must have a black parent",
                        BLACK,
                        parentColor
                );
                break;

            default:
                fail("Unexpected color: " + color);
        }

        Object left = getField(node, "left");
        Object right = getField(node, "right");

        assertIsRedBlackTree(
                expectedBlackNodes,
                actualBlackNodes,
                color,
                left,
                nullNode
        );

        assertIsRedBlackTree(
                expectedBlackNodes,
                actualBlackNodes,
                color,
                right,
                nullNode
        );
    }

    private int countExpectedBlackNodes(
            Object root,
            Object nullNode) throws Exception {

        int count = 0;
        Object node = root;

        while (node != nullNode) {
            if (getFieldInt(node, "color") == BLACK) {
                count++;
            }

            node = getField(node, "left");
        }

        return count;
    }

    private Object getField(
            Object object,
            String fieldName) throws Exception {

        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(object);
    }

    private int getFieldInt(
            Object object,
            String fieldName) throws Exception {

        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getInt(object);
    }

    private Activity createActivity(long id, String name, String startTime) {
        return new Activity(
                id,
                name,
                Instant.parse(startTime),
                ActivityType.RUN,
                10000.0,
                Duration.ofMinutes(50),
                Duration.ofMinutes(55),
                100.0,
                3.3,
                4.5,
                150.0,
                175.0,
                600.0
        );
    }

    @Test
    public void testActivityTreeInsert() {
        ActivityTree tree = new ActivityTree();

        Activity activity = createActivity(
                1L,
                "morning run",
                "2026-08-01T08:00:00Z"
        );

        assertTrue(tree.isEmpty());

        tree.insert(activity);

        assertFalse(tree.isEmpty());
        assertTrue(tree.contains(activity));
    }

    @Test
    public void testRemoveActivity() {
        ActivityTree tree = new ActivityTree();

        Activity activity = createActivity(
                1L,
                "morning run",
                "2026-08-01T08:00:00Z"
        );

        tree.insert(activity);

        assertTrue(tree.contains(activity));

        tree.remove(activity);

        assertFalse(tree.contains(activity));
        assertTrue(tree.isEmpty());
    }

    @Test
    public void testRemoveOneOfMultipleActivitiesOnSameDate() {
        ActivityTree tree = new ActivityTree();

        Activity run = createActivity(
                1L,
                "morning run",
                "2026-08-01T08:00:00Z"
        );

        Activity walk = createActivity(
                2L,
                "evening walk",
                "2026-08-01T18:00:00Z"
        );

        tree.insert(run);
        tree.insert(walk);

        assertTrue(tree.contains(run));
        assertTrue(tree.contains(walk));
        assertFalse(tree.isEmpty());

        tree.remove(run);

        assertFalse(tree.contains(run));
        assertTrue(tree.contains(walk));
        assertFalse(tree.isEmpty());
    }

    @Test
    public void testRemoveLastActivityFromDate() {
        ActivityTree tree = new ActivityTree();

        Activity run = createActivity(
                1L,
                "morning run",
                "2026-08-01T08:00:00Z"
        );

        Activity walk = createActivity(
                2L,
                "evening walk",
                "2026-08-01T18:00:00Z"
        );

        tree.insert(run);
        tree.insert(walk);

        tree.remove(run);
        tree.remove(walk);

        assertFalse(tree.contains(run));
        assertFalse(tree.contains(walk));
        assertTrue(tree.isEmpty());
    }

    @Test
    public void testRemoveLeaf() {
        ActivityTree tree = new ActivityTree();

        Activity middle = createActivity(
                1L,
                "middle",
                "2026-08-10T08:00:00Z"
        );

        Activity earlier = createActivity(
                2L,
                "earlier",
                "2026-08-05T08:00:00Z"
        );

        Activity later = createActivity(
                3L,
                "later",
                "2026-08-15T08:00:00Z"
        );

        tree.insert(middle);
        tree.insert(earlier);
        tree.insert(later);

        tree.remove(earlier);

        assertFalse(tree.contains(earlier));
        assertTrue(tree.contains(middle));
        assertTrue(tree.contains(later));
    }

    @Test
    public void testRemoveNodeWithTwoChildren() {
        Activity middle = createActivity(
                1L,
                "middle",
                "2026-08-10T08:00:00Z"
        );

        Activity earlier = createActivity(
                2L,
                "earlier",
                "2026-08-05T08:00:00Z"
        );

        Activity later = createActivity(
                3L,
                "later",
                "2026-08-15T08:00:00Z"
        );

        Activity earliest = createActivity(
                4L,
                "earliest",
                "2026-08-02T08:00:00Z"
        );

        Activity latest = createActivity(
                5L,
                "latest",
                "2026-08-20T08:00:00Z"
        );

        ActivityTree tree = new ActivityTree();

        tree.insert(middle);
        tree.insert(earlier);
        tree.insert(later);
        tree.insert(earliest);
        tree.insert(latest);

        tree.remove(middle);

        assertFalse(tree.contains(middle));
        assertTrue(tree.contains(earlier));
        assertTrue(tree.contains(later));
        assertTrue(tree.contains(earliest));
        assertTrue(tree.contains(latest));
    }

    @Test
    public void testRemoveNonExistingActivity() {
        ActivityTree tree = new ActivityTree();

        Activity existing = createActivity(
                1L,
                "existing",
                "2026-08-10T08:00:00Z"
        );

        Activity notExisting = createActivity(
                2L,
                "not existing",
                "2026-08-15T08:00:00Z"
        );

        tree.insert(existing);

        tree.remove(notExisting);

        assertTrue(tree.contains(existing));
        assertFalse(tree.contains(notExisting));
        assertFalse(tree.isEmpty());
    }

    @Test
    public void testRemoveManyActivities() {
        ActivityTree tree = new ActivityTree();

        Activity a1 = createActivity(
                1L, "activity 1", "2026-08-01T08:00:00Z"
        );

        Activity a2 = createActivity(
                2L, "activity 2", "2026-08-02T08:00:00Z"
        );

        Activity a3 = createActivity(
                3L, "activity 3", "2026-08-03T08:00:00Z"
        );

        Activity a4 = createActivity(
                4L, "activity 4", "2026-08-04T08:00:00Z"
        );

        Activity a5 = createActivity(
                5L, "activity 5", "2026-08-05T08:00:00Z"
        );

        tree.insert(a1);
        tree.insert(a2);
        tree.insert(a3);
        tree.insert(a4);
        tree.insert(a5);

        tree.remove(a3);
        assertFalse(tree.contains(a3));

        tree.remove(a1);
        assertFalse(tree.contains(a1));

        tree.remove(a5);
        assertFalse(tree.contains(a5));

        assertTrue(tree.contains(a2));
        assertTrue(tree.contains(a4));
        assertFalse(tree.isEmpty());
    }

    @Test
    public void testRedBlackPropertiesAfterDeletion() {
        ActivityTree tree = new ActivityTree();

        Activity[] activities = new Activity[20];

        for (int i = 0; i < 20; i++) {
            activities[i] = createActivity(
                    i,
                    "activity " + i,
                    "2026-08-" + String.format("%02d", i + 1) + "T08:00:00Z"
            );

            tree.insert(activities[i]);
            assertIsRedBlackTree(tree);
        }

        for (int i = 0; i < 20; i++) {
            tree.remove(activities[i]);
            assertIsRedBlackTree(tree);
        }

        assertTrue(tree.isEmpty());
    }
}