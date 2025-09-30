package org.zerock.triplet.domain.draft;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonMergeUtil {
    public static <T> T merge(T base, Object patch, Class<T> type, ObjectMapper om){
        ObjectNode baseNode = om.valueToTree(base);
        ObjectNode patchNode = om.valueToTree(patch);
        baseNode.setAll(patchNode);
        try{return om.treeToValue(baseNode, type);}
        catch(Exception e) {throw new RuntimeException(e);}
    }
}
