package com.example.saferunner;

import com.example.saferunner.data.Activity;
import com.example.saferunner.data.ActivityType;
import com.example.saferunner.datastructures.ActivityTree;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.time.Duration;
import java.time.Instant;

public class TreeTest {

    @Test
    public void testActivityTreeInsert() {
        ActivityTree tree = new ActivityTree();

        Activity activity = new Activity(
                1L,
                "morning run",
                Instant.parse("2026-08-01T08:00:00Z"),
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

        assertTrue(tree.isEmpty());

        tree.insert(activity);

        assertFalse(tree.isEmpty());
        assertTrue(tree.contains(activity));
    }
}