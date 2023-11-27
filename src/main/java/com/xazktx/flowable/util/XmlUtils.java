package com.xazktx.flowable.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;

public class XmlUtils {

    public static byte[] jsonToXml(String json) throws JsonProcessingException {
        return new BpmnXMLConverter()
                .convertToXML(new BpmnJsonConverter()
                        .convertToBpmnModel(new ObjectMapper()
                                .readTree(json)
                        )
                );
    }

}
