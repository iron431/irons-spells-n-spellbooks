package io.redspace.ironsspellbooks.datafix;

import java.util.List;

public class ParallelMatcher implements AutoCloseable {
    private int[] matcherPositions;
    private final List<byte[]> matchTargets;

    public ParallelMatcher(List<byte[]> matchTargets) {
        this.matchTargets = matchTargets;
        this.matcherPositions = new int[matchTargets.size()];
    }

    private void reset() {
        matcherPositions = new int[matcherPositions.length];
    }

    public boolean pushValue(int nextToken) {
        for (int matcherIndex = 0; matcherIndex < matchTargets.size(); matcherIndex++) {
            var matchTarget = matchTargets.get(matcherIndex);

            if (matchTarget[matcherPositions[matcherIndex]] == nextToken) {

                matcherPositions[matcherIndex]++;

                if (matcherPositions[matcherIndex] == matchTarget.length) {
                    //If any matcher has a full match return true
                    reset();
                    return true;
                }
            } else {
                //Reset the match index tracker for the current matcher on any missed token
                matcherPositions[matcherIndex] = 0;
            }
        }

        //If we don't have a full match on any item return false
        return false;
    }

    @Override
    public void close() throws Exception {
        reset();
    }
}
