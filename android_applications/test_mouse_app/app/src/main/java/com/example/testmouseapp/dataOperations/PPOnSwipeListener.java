package com.example.testmouseapp.dataOperations;

import android.view.GestureDetector;
import android.view.MotionEvent;

//extend GestureDetector class so that only the subset of gestures we need can be worked with
//otherwise we'd need to have a function for EVERY supported gesture
public class PPOnSwipeListener extends GestureDetector.SimpleOnGestureListener {
    private static final String DEBUG_TAG = "PPGestureListener";

    //TODO: implement minimum distance for directional swipe
    private static final int FLING_DISTANCE_THRESHOLD = 0;

    /*
    onFling() override and helper functions
    This one takes special work, as the direction of the fling must be calculated
     */
    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float dist_x, float dist_y) {

            /*
            Gets two events location on plane at event1 = (x1, y1) and event2 = (x2, y2)
            event1 is the initial event
            Assume that event2 can be located at one of four different positions from event1
            (above, below, or to the right or left of event1)
            Consequently, we can determine which of the four desired directions our swipe falls into
             */

        float x1 = event1.getX();
        float y1 = event1.getY();

        float x2 = event2.getX();
        float y2 = event2.getY();

        Direction direction = getDirection(x1, y1, x2, y2);
        return onSwipe(direction);
    }

    //this method must be overridden
    public boolean onSwipe(Direction direction) {
        return false;
    }

    //returns direction that an arrow pointing from p1 = (x1, y1) to p2 (x2, y2) would have
    public Direction getDirection(float x1, float y1, float x2, float y2) {

        double angle = getAngle(x1, y1, x2, y2);
        return Direction.fromAngle(angle);
    }

    //find angle between two points
    public double getAngle(float x1, float y1, float x2, float y2) {

        double radians = Math.atan2(y1-y2, x2-x1) + Math.PI;
        return (radians * 180 / Math.PI + 180) % 360;
    }

    public enum  Direction {
        up,
        down,
        left,
        right;

        /**
         * Returns a direction given an angle
         * Directions defined as follows:
         *
         * up: [45, 135]
         * right: [0, 45] and [315, 360]
         * down: [225, 315]
         * left [135, 225]
         */

        public static Direction fromAngle(double angle) {
            if(inRange(angle, 45, 135)) {
                return Direction.up;
            }

            else if (inRange(angle, 0, 45) || inRange(angle, 315, 360)) {
                return Direction.right;
            }

            else if (inRange(angle, 225, 315)) {
                return Direction.down;
            }

            else {
                return Direction.left;
            }
        }

        //return true if given angle is in the interval [start, end)
        private static boolean inRange(double angle, float start, float end) {
            return (angle >= start) && (angle < end);
        }
    }
}

