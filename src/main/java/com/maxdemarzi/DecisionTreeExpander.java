package com.maxdemarzi;

import com.maxdemarzi.schema.Labels;
import com.maxdemarzi.schema.RelationshipTypes;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.BranchState;
import org.neo4j.logging.Log;

import java.util.Collections;
import java.util.Map;

import static com.maxdemarzi.DecisionTree.*;

public class DecisionTreeExpander implements PathExpander {
    private Map<String, String> facts;

    DecisionTreeExpander(Map<String, String> facts) {
        this.facts = facts;
    }

    @Override
    public Iterable<Relationship> expand(Path path, BranchState branchState) {
        Node last = path.endNode();
        // If we have Rules to evaluate, go do that.
        if (last.hasRelationship(Direction.OUTGOING, RelationshipTypes.HAS)) {
            return last.getRelationships(Direction.OUTGOING, RelationshipTypes.HAS);
        }

        if (last.hasLabel(Labels.Rule)) {
            try {
                if (last.hasProperty("expression")) {
                    return last.getRelationships(Direction.OUTGOING, trueOrFalse(last, facts));
                } else {
                    return last.getRelationships(Direction.OUTGOING, choosePath(last, facts));
                }
            } catch (Exception ignored) {
            }
            return last.getRelationships(Direction.OUTGOING, RelationshipTypes.REQUIRES);
        }
        // Otherwise, not sure what to do really.
        return Collections.emptyList();
    }

    @Override
    public PathExpander reverse() {
        return null;
    }
}